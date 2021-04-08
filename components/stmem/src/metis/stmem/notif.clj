(ns metis.stmem.notif
  (:require [metis.config.interface :as c]
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
  ([{t :stmem-trans s :stmem-key-sep :as config} m]
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
   (let [pat (subs-pat config m)]
     (car/with-new-pubsub-listener conn {pat f} (car/psubscribe pat))))) 

(defn registered? [k] (contains? @listeners k))

(defn register
  "Generates and registers a listener in the `listeners` atom.  The cb!
  function dispatches depending on the result."
  ([m f]
   (register c/config m f))
  ([config m f]
   (let [reg-key (reg-key config m)]
     (if-not (registered? reg-key)
       {:ok (map? (swap! listeners assoc reg-key (gen-listener m f)))}
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
           (close-listener v)
           {:ok (map? (swap! listeners dissoc k))}))
       @listeners))

(defn de-register
  "De-registers the listener with the key `mp-id` in the `listeners`
  atom."
  [m]
  (let [k (reg-key m)]
    (when (registered? k)
      (mu/log ::de-register! :message "will de-register" :key k)
      (close-listener (@listeners k))
      {:ok (map? (swap! listeners dissoc k))})))
