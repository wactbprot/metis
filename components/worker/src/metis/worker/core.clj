(ns metis.worker.core
  ^{:author "wactbprot"
    :doc "Runs the upcomming tasks of a certain container."}
  (:require [metis.exchange.interface :as exch]
            [metis.config.interface :as c]
            [com.brunobonacci.mulog :as mu]
            [metis.stmem.interface :as stmem]
            [metis.tasks.interface :as tasks]
            [metis.worker.wait :refer [wait!]]))

;;------------------------------
;;  future registry 
;;------------------------------
(defonce future-registry (atom {}))
 
(defn start!
  "Starts the worker in a new threat. "
  [worker task]
  (swap! future-registry
         assoc (stmem/map->key task) (future (worker task))))

;;------------------------------
;;  dispatch 
;;------------------------------
(defn dispatch
  "Dispatch depending on the `:Action`."
  [task]
  (mu/log ::dispatch :message "dispatch task")
  (condp = (keyword (:Action task))
    :wait           (start! wait!              task)
    ;; :select         (start! select-definition! task)
    ;; :runMp          (start! run-mp!            task)
    ;; :stopMp         (start! stop-mp!           task)
    ;; :writeExchange  (start! write-exchange!    task)
    ;; :readExchange   (start! read-exchange!     task)
    ;; :getDate        (start! get-date!          task)
    ;; :getTime        (start! get-time!          task)
    ;; :message        (start! message!           task)
    ;; :genDbDoc       (start! gen-db-doc!        task)
    ;; :replicateDB    (start! replicate!         task)
    ;; :Anselm         (start! devproxy!          task)
    ;; :DevProxy       (start! devproxy!          task)
    ;; :MODBUS         (start! devhub!            task)
    ;; :TCP            (start! devhub!            task)
    ;; :VXI11          (start! devhub!            task)
    ;; :EXECUTE        (start! devhub!            task)
    (stmem/set-state (assoc task :value :error :message "No worker for action"))))

;;------------------------------
;; check-in
;;------------------------------
(defn run
  "Gets the `task` and calls the `dispach` function on
  it. Handles the `:RunIf` case. The `:StopIf` case is handeled by the
  `workers` after processing the task."  
  ([m]
   (run c/config m))
  ([{stop-if-delay :stop-if-delay} m]
   (let [task (tasks/get-task m)]
     (if (exch/run-if (exch/all m) task)
       (if (exch/only-if-not (exch/all m)task)
         (dispatch task)
         (do
           (Thread/sleep stop-if-delay)
           (stmem/set-state (assoc m :value :executed :message "state set by only-if-not"))))
       (do
         (Thread/sleep stop-if-delay)
         (stmem/set-state (assoc m :value :ready :message "state set by run-if")))))))

(comment
  (run {:mp-id "mpd-ref" :struct :cont :no-idx 0 :par-idx 0 :seq-idx 0})
  )
