(ns metis.scheduler.core
  ^{:author "wactbprot"
    :doc "Starts and stops a scheduler."}
  (:require [metis.config.interface :as c]
            [com.brunobonacci.mulog :as mu]
            [metis.scheduler.proc :as proc]
            [metis.stmem.interface :as stmem]))


;;------------------------------
;; stop state
;;------------------------------
(defn stop-state
  "Registers a stmem listener for the `state`-interface of a `container` or 
  `definitions` struct. [[start-next]] is the callback of this
  listener."
  [m]
    (mu/log ::stop-state :message "de-register")
    (stmem/de-register m))


(comment

(defn error!
  "Sets the `ctrl` interface to `\"error\"`. Function does not [[de-observe!]]."
  [k]
  (mu/log ::error! :error "will set ctrl interface to error" :key k)
  (st/set-val! (stu/key->ctrl-key k) "error"))

(defn nop! [k] (mu/log ::nop! :message "no operation" :key k))

(defn all-exec!
  "Handles the case where all `state` interfaces in a container are
  `:executed`. Proceeds depending on the old value of the `ctrl` interface."
  [k]
  (let [ctrl-k   (stu/key->ctrl-key k)
        old-cmd  (ctrl-key->cmd-kw ctrl-k)
        new-cmd  (if (= old-cmd :mon) "mon" "ready")]
    (mu/log ::all-exec! :message "all tasks executed, set new cmd" :key k :command new-cmd)
    (de-observe! ctrl-k)
    (ready! ctrl-k)
    (st/set-val! ctrl-k new-cmd)))
)

;;------------------------------
;; start-next
;;------------------------------
(defn start-next
  "`start-next` choose the `m` of the upcomming tasks.
  Then the `worker` set the state to `\"working\"` which triggers the
  next call to `start-next!`: parallel tasks are started this way."
  [m]
  (let [v (stmem/get-maps (assoc m :func :state :seq-idx :* :par-idx :*))
        m (next-map v)]
      (cond
        (errors? v) (error! m)
        (all-executed? v) (all-exec! m)
        (nil? m) (nop! m)
        (work/check m))))

;;------------------------------
;; set-states-ready
;;------------------------------
(defn set-states-ready 
  "Sets all states (the state interface) to ready."
  [m]
  (mu/log ::set-states-ready :message "will set all states ready")
  (stmem/set-vals (assoc m :func :state :seq-idx :* :par-idx :* :value "ready")))

;;------------------------------
;; state interface
;;------------------------------
;; start state
;;------------------------------
(defn start-state
  "Registers a stmem listener for the `state`-interface of a `container` or
  `definitions` struct. [[start-next]] is the callback of this
  listener."
  [m]
  (mu/log ::start-state :message "register, callback and start-next")
  (stmem/register m start-next)
  (mu/log ::start-state :message "will call start-next first trigger")
  (start-next m))
 
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
  (let [cmd (keyword (:value m))
        m (assoc m :func :state)]
    (condp = cmd
      :run (start-state m)
      :mon (start-state m)
      :stop (do
              (stop-state m)
              (set-states-ready m))
      :reset (do
               (stop-state m)
               (set-states-ready m))
      :suspend (stop-state m)
      :error (mu/log ::dispatch :error "at ctrl interface")
      (mu/log ::dispatch :message "default case ctrl dispach function" :command cmd))))

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
  (mu/log ::stop :message "de-register and clean all ctrl listener" :mp-id mp-id)
  (stmem/clean-register {:mp-id mp-id :struct :* :no-idx :* :func :ctrl}))

;;------------------------------
;; start-ctrl
;;------------------------------
(defn start-ctrl
  "Registers a listener for the `ctrl` interface of the entire
  `mp-id`. The [[dispatch]] function becomes the listeners `cb!`." 
  ([mp-id]
   (start c/config mp-id))
  ([config mp-id]
   (mu/log ::start :message "register ctrl listener" :mp-id mp-id)
   (stmem/register {:mp-id mp-id :struct :* :no-idx :* :func :ctrl} dispatch)))
