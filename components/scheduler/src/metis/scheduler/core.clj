(ns metis.scheduler.core
  ^{:author "wactbprot"
    :doc "Starts and stops a scheduler."}
  (:require [metis.config.interface :as c]
            [com.brunobonacci.mulog :as µ]
            [metis.scheduler.proc :as proc]
            [metis.stmem.interface :as stmem]
            [metis.worker.interface :as worker]))

;;------------------------------
;; state of state interface 
;;------------------------------
(defn state-interface [m]
  (stmem/get-maps (assoc m :func :state :seq-idx :* :par-idx :*)))

(defn state
  "`state` `proc`esses on the state vector in order to find out whats
  next todo. The input `v`ector looks like this:

  Example:
  ```clojure
  [{:mp-id \"mpd-ref\",
  :struct :cont,
  :no-idx 0,
  :func :state,
  :seq-idx 0,
  :par-idx 0,
  :value \"ready\"}]
  ```"
  [v]
  (let [m (proc/next-map v)]
    (cond
      (proc/errors? v)       {:state :error    :m (first v)}
      (proc/all-executed? v) {:state :all-exec :m (first v)}
      (nil? m)               {:state :nop      :m (first v)}
      :else                  {:state :work     :m m})))

;;------------------------------
;; state of ctrl interface 
;;------------------------------
(defn ctrl [m]
  {:ctrl (keyword (stmem/get-val (assoc (dissoc m :seq-idx :par-idx) :func :ctrl)))})

;;------------------------------
;; check
;;------------------------------
(defn set-state-ctrl [m s c]
  (stmem/set-ctrl   (assoc m :value :check))
  (stmem/set-states (assoc m :value s))
  (stmem/set-ctrl   (assoc m :value c)))

(defn set-ctrl [m c] (stmem/set-ctrl (assoc m :value c)))
  
(defn check
  "Processes whats todo next depending on the state of state and ctrl
  interface."
  [m]
  (µ/trace ::check [:function "scheduler/check"]
           
           (let [s (merge (state (state-interface m)) (ctrl m))
                 m (:m s)]
             (when-not (= (:ctrl s) :check)
               (condp = (dissoc s :m)
                 ;; run
                 {:ctrl :run    :state :error}    (set-ctrl m :error)
                 {:ctrl :run    :state :all-exec} (set-state-ctrl m :ready :ready)
                 {:ctrl :run    :state :work}     (worker/run m)
                 ;; mon
                 {:ctrl :mon    :state :error}    (set-ctrl m :error) 
                 {:ctrl :mon    :state :all-exec} (set-state-ctrl m :ready :mon)
                 {:ctrl :mon    :state :work}     (worker/run m)
                 ;; stop
                 {:ctrl :stop   :state :error}    (set-state-ctrl m :ready :ready)
                 {:ctrl :stop   :state :all-exec} (set-state-ctrl m :ready :ready)
                 {:ctrl :stop   :state :work}     (set-state-ctrl m :ready :ready)
                 {:ctrl :stop   :state :nop}      (set-state-ctrl m :ready :ready)
                 ;; reset
                 {:ctrl :reset  :state :error}    (set-state-ctrl m :ready :ready)
                 {:ctrl :reset  :state :all-exec} (set-state-ctrl m :ready :ready)
                 {:ctrl :reset  :state :work}     (set-state-ctrl m :ready :ready)
                 {:ctrl :reset  :state :nop}      (set-state-ctrl m :ready :ready)
                 
                 (µ/log ::dispatch :message "state not handeled" :state s))))))

;;------------------------------
;; stop 
;;------------------------------
(defn stop
  "De-registers the listener for the `mp-id`. After stopping, the system
  will no longer react on changes (write events) at any interface."
  [{mp-id :mp-id}]
  (µ/log ::stop :message "clean mp listener")
  (stmem/clean-register {:mp-id mp-id}))

;;------------------------------
;; start-ctrl
;;------------------------------
(defn start
  "Registers a listener with the pattern `__keyspace@<stmem-db>*__:<mp-id>@*@*@*`.
  The [[check]] function becomes the listeners `callback`." 
  ([m]
   (start c/config m))
  ([config {mp-id :mp-id}]
   (µ/log ::start :message "register mp listener check callback")
   (stmem/register {:mp-id mp-id :struct :* :no-idx :* :func :*} check)))
