(ns metis.scheduler.core
  ^{:author "wactbprot"
    :doc "Starts and stops a scheduler."}
  (:require [metis.config.interface :as c]
            [com.brunobonacci.mulog :as mu]
            [metis.stmem.interface :as stmem]))
(comment
;;------------------------------
;; ready!
;;------------------------------
(defn ready! 
  "Sets all states (the state interface) to ready."
  [k]
  (mu/log ::ready! :message "all states ready" :key k)
  (st/set-same-val! (k->state-ks k) "ready"))

;;------------------------------
;; stop
;;------------------------------
(defn de-observe!
  "Opposite of [[observe!]]: De-registers the `state` listener.  The
  de-register pattern is derived from the key `k` (may be the
  `ctrl-key` or `state-key`).  Resets the state interface afterwards."
  [k]
  (mu/log ::de-observe! :message "de-observe" :key k)
  (st/de-register! (stu/key->mp-id k) (stu/key->struct k) (stu/key->no-idx k) "state"))

;;------------------------------
;; set value at ctrl-key 
;;------------------------------
(defn ctrl-key->cmd-kw
  "Gets the `cmd` from the `ctrl-k`. Extracts the `next-ctrl-cmd` and
  make a keyword out of it."
  [k]
  (->> k st/key->val u/next-ctrl-cmd keyword))

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

;;------------------------------
;; choose and start next task
;;------------------------------
(defn next-map
  "The `next-map` function returns a map containing the next step to
  start. See `cmp.state-test/next-map-i` for examples how `next-map`
  should work.

  Example:
  ```clojure
   (next-map [{:seq-idx 0 :par-idx 0 :state :executed}
              {:seq-idx 0 :par-idx 1 :state :executed}
              {:seq-idx 1 :par-idx 0 :state :executed}
              {:seq-idx 2 :par-idx 0 :state :executed}
              {:seq-idx 3 :par-idx 0 :state :working}
              {:seq-idx 3 :par-idx 1 :state :ready}])
  ;; =>
  ;; {:seq-idx 3 :par-idx 1 :state :ready}
  ```"
  [v]
  (when-let [next-m (next-ready v)]
    (when-let [i (:seq-idx next-m)]
      (when (or (zero? (u/ensure-int i))
                (predecessors-executed? v i))
        next-m))))

;;------------------------------
;; start-next(!)
;;------------------------------
(defn start-next
  "Side effect free. Makes [[start-next!]] testable.
  Gets the state vector `v` and picks the next thing to do.
  The `ctrl-k`ey is derived from the first map in the
  the `v`."
  [v]
  (let [m      (next-map v)
        ctrl-k (stu/info-map->ctrl-key (first v))
        defi-k (stu/info-map->definition-key m)]
    (cond
      (errors?       v) {:what :error    :key ctrl-k}
      (all-executed? v) {:what :all-exec :key ctrl-k}
      (nil?          m) {:what :nop      :key ctrl-k}
      :run-worker       {:what :work     :key defi-k})))

(defn start-next!
  "`start-next!` choose the `k` of the upcomming tasks.
  Then the `worker` set the state to `\"working\"` which triggers the
  next call to `start-next!`: parallel tasks are started this way.
  
  Side effects all around. "
  [v]
  (when (vector? v)
    (let [{what :what k :key} (start-next v)]
      (condp = what
        :error    (error!     k)
        :all-exec (all-exec!  k)
        :nop      (nop!       k)
        :work     (work/check k)))))

;;------------------------------
;; observe!
;;------------------------------
(defn observe!
  "Registers a listener for the `state`-interface of a `container` or
  `definitions` struct. [[start-next!]] is the callback of this
  listener. The register pattern is derived from the key
  `k` (`ctrl-key`)."
  [k]
  (mu/log ::observe! :message "register, callback and start-next!" :key k)
  (st/register! (stu/key->mp-id k) (stu/key->struct k) (stu/key->no-idx k) "state"
                (fn [msg]
                  (when-let [msg-k (st/msg->key msg)]                   
                    (start-next! (ks->state-vec (k->state-ks msg-k))))))
  (mu/log ::observe :message "will call start-next first trigger" :key k)
  (start-next! (ks->state-vec (k->state-ks k))))


;;------------------------------
;; ctrl interface
;;------------------------------

)

;;------------------------------
;; dispatch
;;------------------------------
(defn dispatch
  "`observe!`s or `de-observe!`s depending on `cmd`. If an `:error`
  occurs the system is kept running (no `de-observe`). So, no restart
  is necessary. Just fix the problem and set the corresponding state
  from `:error` to `ready` and the processing goes on."
  [m]
  (prn (:value m))
  (comment
    (condp = (keyword (stmem/get-val m))
    :run     (observe! k)
    :mon     (observe! k)
    :stop    (do
               (de-observe! k)
               (ready! k))
    :reset   (do
               (de-observe! k)
               (ready! k))
    :suspend (de-observe! k)
    :error   (mu/log ::dispatch :error "at ctrl interface" :key k :command cmd)
    (mu/log ::dispatch :message "default case ctrl dispach function" :key k :command cmd))))



;;------------------------------
;; stop
;;------------------------------
(defn stop
  "De-registers the listener for the `ctrl` interfaces of the
  `mp-id`. After stopping, the system will no longer react on
  changes (write events) at the `ctrl` interface."
  [mp-id]
  (mu/log ::stop :message "de-register and clean all ctrl listener" :mp-id mp-id)
  (stmem/clean-register {:mp-id mp-id :struct :* :no-idx :* :func :ctrl}))

;;------------------------------
;; start
;;------------------------------
(defn start
  "Registers a listener for the `ctrl` interface of the entire
  `mp-id`. The [[dispatch]] function becomes the listeners `cb!`." 
  ([mp-id]
   (start c/config mp-id))
  ([config mp-id]
   (mu/log ::start :message "register ctrl listener" :mp-id mp-id)
   (stmem/register {:mp-id mp-id :struct :* :no-idx :* :func :ctrl} dispatch)))
