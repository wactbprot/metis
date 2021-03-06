(ns metis.stmem.notif
  (:require  [metis.stmem.api :as api]
             [metis.config.interface :as c]
             [metis.stmem.core :as core]
             [taoensso.carmine :as car :refer (wcar)]
             [com.brunobonacci.mulog :as µ]
             [clojure.string :as string]
             [metis.stmem.trans :as trans]))

;;------------------------------
;; listeners
;;------------------------------
(defonce listeners (atom {}))

;;------------------------------
;; register key utils
;;------------------------------
(defn m->struct [{t :stmem-trans} {struct :struct}] ((or struct :*) t))

(defn m->no-idx [{t :stmem-trans :as config} {no-idx :no-idx}]
  (if (number? no-idx) (trans/lpad config no-idx) (:* t)))

(defn m->func [{t :stmem-trans} {func :func}] ((or func :*) t))

(defn m->mp-id [{t :stmem-trans} {mp-id :mp-id}] (if (keyword? mp-id) (mp-id t) mp-id))

(defn reg-pat
  ([m]
   (reg-pat c/config m))
  ([{t :stmem-trans s :stmem-notif-sep :as config} {struct :struct :as m}]
   (str (m->mp-id config m) s
        (cond
          (= :id struct)   (str (m->struct config m) s(:* t))
          (= :exch struct) (str (m->struct config m) s(:* t))
          :default         (str (m->struct config m) s
                                (m->no-idx config m) s
                                (m->func config m)
                                (:* t))))))

(defn subs-pat
  "Generates subscribe patterns."
  ([m]
   (subs-pat c/config m))
  ([config m]
   (let [{t :stmem-trans s :stmem-notif-sep db :stmem-db} config]
     (str "__keyspace" s db (:* t) "__:" (reg-pat config m)))))

(defn reg-key
  "Generates a register key for the `listener` map."
  ([m]
   (reg-key c/config m))
  ([{t :stmem-trans s :stmem-notif-sep :as config} m]
   (when (and (map? m) (seq m))
     (str (reg-pat config m) s (trans/lpad config (or (:level m)  0))))))

;;------------------------------
;; message wraper
;;------------------------------
(defn wrap-skip-psubs
  "Ensures that f is only executed on `pmessage`s. Skips the subscribe
  event calls."
  [f]
  (fn [v] (when (= "pmessage" (first v)) (f v))))

(defn wrap-msg->key
  [f]
  (fn [[_ _ s _]] (f (second (string/split s #":")))))

(defn wrap-key->map
  ([f]
   (wrap-key->map c/config f))
  ([config f]
   (fn [k] (f (trans/key->map k)))))

(defn wrap-assoc-value
  ([f]
   (wrap-assoc-value c/config f))
  ([config f]
   (fn [m]  (f (assoc m :value (api/get-val m))))))

;;------------------------------
;; generate listener
;;------------------------------
(defn gen-listener
  "Returns a listener for published keyspace **notif**ications. Don't forget
  to [[close-listener]]

   Example:
  ```clojure
  ;; generate and close
  (close-listener! (gen-listener {:mp-id \"ref\"} msg->key))
  ```"
  ([m f]
   (gen-listener c/config m f))
  ([{{conn :spec} :stmem-conn :as config} m f]
   (let [pat (subs-pat config m)
         f (->> f
                wrap-assoc-value
                wrap-key->map
                wrap-msg->key
                wrap-skip-psubs)]
     (µ/log ::gen-listener :message (str "will subscribe to: " pat))
     (car/with-new-pubsub-listener conn {pat f} (car/psubscribe pat)))))

(defn registered? [k] (contains? @listeners k))

(defn registered
  "Retuns a vector of maps containing information about the registered
  listeners"
  []
  (mapv
   (fn [[k v ]] (merge {:reg-key k} (get-in v [:connection :spec])))
   @listeners))

(defn register
  "Generates a listener for the function `f`. Registers it at the
  `listeners` atom."
  ([m f]
   (register c/config m f))
  ([{relax :stmem-reg-relax :as config} m f]
   (let [reg-key (reg-key config m)]
     (if-not (registered? reg-key)
       (do
        (swap! listeners assoc reg-key (gen-listener m f))
        (Thread/sleep relax)
        {:ok true})
       {:ok true :warn "already registered"}))))

;;------------------------------
;; close listeners, deregister
;;------------------------------
(defn close-listener
  "Closes the given listener generated by [[gen-listener]].

  Example:
  ```clojure
  ;; generate
  (def l (gen-listener {:mp-id \"ref\"} msg->map))
  ;; close
  (close-listener! l)
  ```"
  [l]
  (car/close-listener l))

(defn clean-register
  "Closes and `de-registers` **all** `listeners` belonging to `mp-id` ."
  [{mp-id :mp-id}]
  (mapv (fn [[k v]]
          (when (string/starts-with? k mp-id)
            (µ/log ::clean-register :message "will clean" :key k)
            (close-listener v)
            (swap! listeners dissoc k)))
        @listeners))

(defn de-register
  "De-registers the listener with a key derived from `m`. `dissoc` it
  afterwards from the `listeners` atom."
  [m]
  (let [k (reg-key m)]
    (µ/log ::de-register :message "will de-register" :key k)
    (when (registered? k)
      (close-listener (@listeners k))
      {:ok (map? (swap! listeners dissoc k))})))
