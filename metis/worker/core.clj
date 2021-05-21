✔ (ns metis.worker.core
?   ^{:author "wactbprot"
?     :doc "Runs the upcomming tasks of a certain container."}
?   (:require [metis.exchange.interface :as exch]
?             [metis.config.interface :as c]
?             [com.brunobonacci.mulog :as mu]
?             [metis.stmem.interface :as stmem]
?             [metis.tasks.interface :as tasks]
?             [metis.worker.wait :refer [wait!]]
?             [metis.worker.select-definition :refer [select-definition!]]))
  
? ;;------------------------------
? ;;  future registry 
? ;;------------------------------
~ (defonce future-registry (atom {}))
?  
✔ (defn start!
?   "Starts the worker in a new threat. "
?   [worker task m]
✘   (swap! future-registry
✘          assoc (stmem/map->key task) (future (worker task m))))
  
? ;;------------------------------
? ;;  dispatch 
? ;;------------------------------
✔ (defn dispatch
?   "Dispatch depending on the `:Action`."
?   [task m]
✘   (mu/log ::dispatch :message "dispatch task" :m m)
✘   (condp = (keyword (:Action task))
✘     :wait   (start! wait!              task m)
✘     :select (start! select-definition! task m)
?     ;; :runMp          (start! run-mp!            task)
?     ;; :stopMp         (start! stop-mp!           task)
?     ;; :writeExchange  (start! write-exchange!    task)
?     ;; :readExchange   (start! read-exchange!     task)
?     ;; :getDate        (start! get-date!          task)
?     ;; :getTime        (start! get-time!          task)
?     ;; :message        (start! message!           task)
?     ;; :genDbDoc       (start! gen-db-doc!        task)
?     ;; :replicateDB    (start! replicate!         task)
?     ;; :Anselm         (start! devproxy!          task)
?     ;; :DevProxy       (start! devproxy!          task)
?     ;; :MODBUS         (start! devhub!            task)
?     ;; :TCP            (start! devhub!            task)
?     ;; :VXI11          (start! devhub!            task)
?     ;; :EXECUTE        (start! devhub!            task)
✘     (stmem/set-state-error (assoc task :message "No worker for action"))))
  
? ;;------------------------------
? ;; check-in
? ;;------------------------------
✔ (defn run
?   "Gets the `task` and calls the `dispach` function on
?   it. Handles the `:RunIf` case. The `:StopIf` case is handeled by the
?   `workers` after processing the task."  
?   ([m]
✘    (run c/config m))
?   ([{stop-if-delay :stop-if-delay} m]
✘    (let [task (tasks/get-task m)]
✘      (if (exch/run-if (exch/all m) task)
✘        (if (exch/only-if-not (exch/all m)task)
✘          (dispatch task m)
✘          (do
✘            (Thread/sleep stop-if-delay)
✘            (stmem/set-state-executed (assoc m :message "state set by only-if-not"))))
✘        (do
✘          (Thread/sleep stop-if-delay)
✘          (stmem/set-state-ready (assoc m  :message "state set by run-if")))))))
  
✔ (comment
?   (def m {:mp-id "mpd-ref" :struct :cont :no-idx 0 :par-idx 0 :seq-idx 0})
?   (run m)
?   (tasks/get-task m)
?   (dispatch {:WaitTime "100",
?              :TaskName "Common-wait",
?              :Comment "Ready in  100 ms",
?              :mp-id "mpd-ref",
?              :Replace {:%waittime 100},
?              :Action "wait",
?              :Use nil}
?             {:mp-id "mpd-ref"
?              :struct :cont
?              :no-idx 0
?              :par-idx 0
?              :seq-idx 0})
?   )
