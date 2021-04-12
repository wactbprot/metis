(ns metis.stmem.notif
  (:require [metis.config.interface :as c]
            [metis.stmem.core :as core]
            [taoensso.carmine :as car :refer (wcar)]
            [com.brunobonacci.mulog :as mu]
            [clojure.string :as string]
            [metis.stmem.trans :as trans]))

;;------------------------------
;; listeners 
;;------------------------------
(defonce listeners (atom {}))

;;------------------------------
;; register key utils 
;;------------------------------
(defn reg-pat
  ([m]
   (reg-pat c/config m))
  ([{t :stmem-trans s :stmem-notif-sep :as config} m]
   (str (:mp-id m) s
        ((or (:struct m) :*)  t) s
        (if (:no-idx m) (trans/lpad config (:no-idx m)) (:* t)) s
        ((or (:func m) :*) t) s)))
   
(defn subs-pat
  "Generates subscribe patterns."
  ([m]
   (subs-pat c/config m))
  ([config m]
   (let [{t :stmem-trans s :stmem-notif-sep db :stmem-db} config]
     (str "__keyspace" s db (:* t) "__:" (reg-pat config m) (:* t)))))

(defn reg-key
  ([m]
   (reg-key c/config m))
  ([{t :stmem-trans s :stmem-notif-sep :as config} m]
   (when (and (map? m) (seq m))
     (str (reg-pat config m) (or (:level m) (trans/lpad config 0))))))

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
  (fn [[_ _ s _]]
    (f (second (string/split s #":")))))

(defn wrap-key->map
  ([f]
   (wrap-key->map c/config f))
  ([{re-sep :re-sep} f]
   (fn [k] (let [v (string/split k re-sep)]
             (f {:mp-id (nth v 0 nil)
                 :struct (nth v 1 nil)
                 :no-idx (trans/ensure-int (nth v 2 nil))
                 :func (nth v 3 nil)
                 :seq-idx (trans/ensure-int (nth v 4 nil))
                 :par-idx (trans/ensure-int (nth v 5 nil))})))))
  
(defn wrap-assoc-value
  ([f]
   (wrap-assoc-value c/config f))
  ([config f]
   (fn [m] (f (assoc m :value (core/get-val (trans/map->key m)))))))

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
         f (->> f  wrap-assoc-value wrap-key->map wrap-msg->key wrap-skip-psubs)]
     (car/with-new-pubsub-listener conn {pat f} (car/psubscribe pat))))) 

(defn registered? [k] (contains? @listeners k))

(defn register
  "Generates a listener for the function `f`. Registers it at the
  `listeners` atom."
  ([m f]
   (register c/config m f))
  ([config m f]
   (let [reg-key (reg-key config m)]
     (if-not (registered? reg-key)
       (do
        (swap! listeners assoc reg-key (gen-listener m f))
        (Thread/sleep (:stmem-reg-relax config))
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
  "Closes and `de-registers` all `listeners` belonging to `mp-id` ."
  [m]
  (map (fn [[k v]]
         (when (string/starts-with? k (:mp-id m))
            (mu/log ::clean-register :message "will clean" :key k)
           (close-listener v)
           {:ok (map? (swap! listeners dissoc k))}))
       @listeners))

(defn de-register
  "De-registers the listener with the key `mp-id` in the `listeners`
  atom."
  [m]
  (let [k (reg-key m)]
    (when (registered? k)
      (mu/log ::de-register :message "will de-register" :key k)
      (close-listener (@listeners k))
      {:ok (map? (swap! listeners dissoc k))})))
