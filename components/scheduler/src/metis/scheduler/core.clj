(ns metis.scheduler.core
  ^{:author "wactbprot"
    :doc "Starts and stops a scheduler."}
  (:require [metis.config.interface :as c]
            [com.brunobonacci.mulog :as mu]
            [metis.scheduler.proc :as proc]
            [metis.stmem.interface :as stmem]
            [metis.worker.interface :as worker]))

;;------------------------------
;; state of state interface 
;;------------------------------
(defn state-interface [m]
  (stmem/get-maps (assoc m :func :state :seq-idx :* :par-idx :*)))

(defn state
  "`state` `proc`esses  on the state vector in order to find out whats next todo. The input `v`ector
  looks like this:

  ```clojure
  [{:mp-id \"mpd-ref\",
  :struct :cont,
  :no-idx 0,
  :func :state,
  :seq-idx 0,
  :par-idx 0,
  :value \"ready\"}
 {:mp-id \"mpd-ref\",
  :struct :cont,
  :no-idx 0,
  :func :state,
  :seq-idx 0,
  :par-idx 1,
  :value \"ready\"}
  ```"
  [v]
  (let [m (proc/next-map v)]
    (cond
      (proc/errors? v) {:state :error :m (first v)}
      (proc/all-executed? v) {:state :all-exec :m (first v)}
      (nil? m) {:state :nop :m (first v)}
      :else {:state :work :m m})))

;;------------------------------
;; state of ctrl interface 
;;------------------------------
(defn ctrl [m]
  {:ctrl (keyword (stmem/get-val (assoc (dissoc m :seq-idx :par-idx) :func :ctrl)))})

;;------------------------------
;; check
;;------------------------------
(defn check
  "Processes whats todo next depending on the state of state and ctrl interface."
  [m]
     
  (let [s (merge (state (state-interface m)) (ctrl m))
        m (:m s)]
   
    (when-not (= (:ctrl s) :check)
      (prn  s)
      (Thread/sleep 1000)
    
      (stmem/set-ctrl (assoc m :value :check))
      (condp = (dissoc s :m)
        {:ctrl :run    :state :error}    (stmem/set-ctrl (assoc m :value :error))
        {:ctrl :mon    :state :error}    (stmem/set-ctrl (assoc m :value :error)) 
        {:ctrl :stop   :state :error}    (stmem/set-ctrl (assoc m :value :error))
        {:ctrl :run    :state :all-exec} (do
                                           (stmem/set-states (assoc m :value :ready))
                                           (stmem/set-ctrl (assoc m :value :ready)))
        {:ctrl :mon    :state :all-exec} (do
                                           (stmem/set-states (assoc m :value :ready))
                                           (stmem/set-ctrl (assoc m :value :mon)))
        {:ctrl :stop   :state :all-exec} (do
                                           (stmem/set-states (assoc m :value :ready))
                                           (stmem/set-ctrl (assoc m :value :ready)))
        {:ctrl :reset  :state :nop}      (do
                                           (stmem/set-states (assoc m :value :ready))
                                           (stmem/set-ctrl (assoc m :value :ready)))
        {:ctrl :stop   :state :nop}      (do
                                           (stmem/set-states (assoc m :value :ready))
                                           (stmem/set-ctrl (assoc m :value :ready)))
        {:ctrl :run    :state :work}     (do
                                           (worker/run m)
                                           (stmem/set-ctrl (assoc m :value :run)))
        {:ctrl :mon    :state :work}     (do
                                           (worker/run m)
                                           (stmem/set-ctrl (assoc m :value :run)))
        (mu/log ::dispatch :message "what todo with state" :state s)))))

;;------------------------------
;; ctrl interface
;;------------------------------
;; stop-ctrl
;;------------------------------
(defn stop
  "De-registers the listener for the `mp-id`. After stopping, the system
  will no longer react on changes (write events) at any interface."
  [mp-id]
  (mu/log ::stop-ctrl :message "clean mp listener")
  (stmem/clean-register {:mp-id mp-id}))

;;------------------------------
;; start-ctrl
;;------------------------------
(defn start
  "Registers a listener with the pattern `__keyspace@0*__:<mp-id>@*@*@*`.
  The [[check]] function becomes the listeners `callback`." 
  ([mp-id]
   (start c/config mp-id))
  ([config mp-id]
   (mu/log ::start-ctrl :message "register mp listener check callback")
   (stmem/register {:mp-id mp-id :struct :* :no-idx :* :func :*} check)))
