(ns metis.scheduler.core
  ^{:author "wactbprot"
    :doc "Starts and stops a scheduler."}
  (:require [metis.config.interface :as c]
            [com.brunobonacci.mulog :as mu]
            [metis.scheduler.proc :as proc]
            [metis.stmem.interface :as stmem]
            [metis.worker.interface :as worker]))

;;------------------------------
;; stop state
;;------------------------------
(defn stop-state
  "Registers a stmem listener for the `state`-interface of a `container` or 
  `definitions` struct. [[start-next]] is the callback of this
  listener."
  [m]
  (mu/log ::stop-state :message "de-register")
  (stmem/de-register (assoc m :func :state :seq-idx :* :par-idx :*)))

(defn new-ctrl [m]
  (let [m   (assoc (dissoc m :par-idx :seq-idx) :func :ctrl)
        cmd (keyword (stmem/get-val m))
        cmd (if (= cmd :mon) :mon :ready)
        m   (assoc m :value cmd)]
    (mu/log ::handle-all-exec :message "set new ctrl" :m m)
    (stmem/set-ctrl m)))
  
(defn handle-all-exec
  [v]
  (let [m (first v)]
    (mu/log ::handle-all-exec :message "all tasks executed" :m m)
    (stop-state m)
    (stmem/set-states (assoc m :func :state :seq-idx :* :par-idx :* :value :ready))))
 
;;------------------------------
;; start-next
;;------------------------------
(defn start-next
  "`start-next` `proc`esses the `m`ap of the task to start `next`.
  
  Then the `worker` set the state to `\"working\"` which triggers the
  next call to `start-next!`: parallel tasks are started this way."
  [m]
  (let [v (stmem/get-maps (assoc m :func :state :seq-idx :* :par-idx :*))
        m (proc/next-map v)]
    (mu/log ::start-next :message "before cond" :m m)
    (cond
      (proc/errors? v) (stmem/set-ctrl (assoc (first v) :value :error))
      (proc/all-executed? v) (handle-all-exec v)
      (nil? m) (mu/log ::start-next :message "no operation")
      :else (worker/run m))))

;;------------------------------
;; start state
;;------------------------------
(defn start-state
  "Registers a stmem listener for the `state`-interface of a `container` or
  `definitions` struct. [[start-next]] is the callback of this
  listener."
  [m]
  (let [m (assoc m :func :state :seq-idx :* :par-idx :*)]
    (mu/log ::start-state :message "register, callback and start-next" :m m)
    (stmem/register m start-next)
    (mu/log ::start-state :message "will call start-next first trigger" :m m)
    (start-next m)))
 
;;------------------------------
;; dispatch
;;------------------------------
(defn dispatch
  "`start`s or `stop`s the state observation of container `(:no-idx m)`
  depending on `cmd`. If an `:error` occurs the system is kept
  running (no `stop` and `de-register`). So, no restart is necessary. Just fix the
  problem and set the corresponding state from `:error` to `ready` and
  the processing goes on."
  [m]
  (mu/log ::dispatch :message "start dispach" :m m)
  (let [cmd (keyword (:value m))]
    (condp = cmd
      :run (start-state m)
      :mon (start-state m)
      :stop (do
              (stop-state m)
              (stmem/set-states (assoc m :ready))
      :reset (do
               (stop-state m)
               (stmem/set-states (assoc m :ready)))
      :suspend (stop-state m)
      :error (mu/log ::dispatch :error "at ctrl interface" :m m)
      (mu/log ::dispatch :message "default case ctrl dispach function")))))

;;------------------------------
;; ctrl interface
;;------------------------------
;; stop-ctrl
;;------------------------------
(defn stop-ctrl
  "De-registers the listener for the `ctrl` interfaces of the
  `mp-id`. After stopping, the system will no longer react on
  changes (write events) at the `ctrl` interface."
  [mp-id]
  (let [m {:mp-id mp-id}]
    (mu/log ::stop-ctrl :message "de-register and clean all ctrl listener" :m m)
    (stmem/clean-register m)))

;;------------------------------
;; start-ctrl
;;------------------------------
(defn start-ctrl
  "Registers a listener for the `ctrl` interface of the entire
  `mp-id`. The [[dispatch]] function becomes the listeners `cb!`." 
  ([mp-id]
   (start-ctrl c/config mp-id))
  ([config mp-id]
   (prn "..:")
   (let [m {:mp-id mp-id :struct :* :no-idx :* :func :ctrl}]
     (mu/log ::start-ctrl :message "register ctrl listener" :m m)
     (stmem/register m dispatch))))
