(ns metis.worker.core
  ^{:author "wactbprot"
    :doc "Runs the upcomming tasks of a certain container."}
  (:require [metis.exchange.interface :as exch]
            [metis.config.interface :as c]
            [metis.worker.exchange :refer [read! write!]]
            [com.brunobonacci.mulog :as mu]
            [metis.stmem.interface :as stmem]
            [metis.tasks.interface :as tasks]
            [metis.worker.message :refer [message!]]
            [metis.worker.wait :refer [wait!]]
            [metis.worker.db-doc :refer [gen-db-doc! rm-db-docs!]]
            [metis.worker.date-time :refer [store-date! store-time!]]
            [metis.worker.ctrl-mp :refer [run-mp! stop-mp!]]
            [metis.worker.devhub :refer [devhub!]]
            [metis.worker.devproxy :refer [devproxy!]]
            [metis.worker.replicate :refer [replicate!]]
            [metis.worker.select-definition :refer [select-definition!]]))

;;------------------------------
;;  future registry 
;;------------------------------
(defonce future-registry (atom {}))
 
(defn start!
  "Starts the worker in a new threat. "
  [worker {task-name :TaskName :as task} m]
  (swap! future-registry assoc task-name (future (worker task m))))

;;------------------------------
;;  dispatch 
;;------------------------------
(defn dispatch
  "Dispatch depending on the `:Action`."
  [task m]
  (mu/log ::dispatch :message "dispatch task" :m m)
  (condp = (keyword (:Action task))
    :wait          (start! wait!              task m)
    :select        (start! select-definition! task m)
    :MODBUS        (start! devhub!            task m)
    :TCP           (start! devhub!            task m)
    :VXI11         (start! devhub!            task m)
    :EXECUTE       (start! devhub!            task m)
    :runMp         (start! run-mp!            task m)
    :stopMp        (start! stop-mp!           task m)
    :getDate       (start! store-date!        task m)
    :getTime       (start! store-time!        task m)
    :Anselm        (start! devproxy!          task m)
    :DevProxy      (start! devproxy!          task m)
    :genDbDoc      (start! gen-db-doc!        task m)
    :rmDbDocs      (start! rm-db-docs!        task m)
    :replicateDB   (start! replicate!         task m)
    :writeExchange (start! write!             task m)
    :readExchange  (start! read!              task m)
    :message       (start! message!           task m)
    (stmem/set-state-error (assoc task :message "No worker for action"))))

;;------------------------------
;; check-in
;;------------------------------
(defn run
  "Gets the `task` and calls the `dispach` function on it. Handles the
  `:RunIf` case. The `:StopIf` case is handeled by the `workers` after
  processing the task."  
  ([m]
   (run c/config m))
  ([{stop-if-delay :stop-if-delay} m]
   ;; since the task is now fetched from ltmem,
   ;; setting the working state takes longer
   ;; test: set state to working before ltmem request.
   (stmem/set-state-working  (assoc m :message "pre working test")) 
   (let [task (tasks/get-task m)]
     (if (exch/run-if (exch/all m) task)
       (if (exch/only-if-not (exch/all m)task)
         (dispatch task m)
         (do
           (Thread/sleep stop-if-delay)
           (stmem/set-state-executed (assoc m :message "state set by only-if-not"))))
       (do
         (Thread/sleep stop-if-delay)
         (stmem/set-state-ready (assoc m  :message "state set by run-if")))))))

(comment
  (def m {:mp-id "mpd-ref" :struct :cont :no-idx 0 :par-idx 0 :seq-idx 0})
  (run m)
  (tasks/get-task m)
  (dispatch {:WaitTime "100",
             :TaskName "Common-wait",
             :Comment "Ready in  100 ms",
             :mp-id "mpd-ref",
             :Replace {:%waittime 100},
             :Action "wait",
             :Use nil}
            {:mp-id "mpd-ref"
             :struct :cont
             :no-idx 0
             :par-idx 0
             :seq-idx 0})
  )
