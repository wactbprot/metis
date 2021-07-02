(ns metis.cli.core
  (:require [metis.config.interface :as c]
            [metis.document.interface :as document]
            [metis.exchange.interface :as exchange]
            [metis.model.interface :as model]
            [metis.log.interface :as log]
            [metis.ltmem.interface :as ltmem]
            [com.brunobonacci.mulog :as µ]
            [clojure.pprint :as pp]
            [metis.stmem.interface :as stmem]
            [metis.scheduler.interface :as scheduler]
            [clojure.string :as string]
            [metis.utils.interface :as utils]
            [metis.worker.interface :as worker])
  (:use [clojure.repl]))

;;------------------------------
;; log system
;;------------------------------
(defn log-start [] (log/start))

(defn log-stop [] (log/stop))

;;------------------------------
;; m- mpd commands
;;------------------------------
(defn m-get
  "Get the measurement program definition (mpd) from the longterm memory (ltm)."
  [mp-id]
  (ltmem/get-doc mp-id))

(defn ms-list-ltmem
  "Returns a list of maps of the `mpd`s available at ltmem. Filters
  the ids by the optional given substring."
  ([]
   (ms-list-ltmem ""))
  ([s]
   (filter
    #(when (map? %) (string/includes? (:id %) s))
    (ltmem/all-mpds))))

(defn ms-table-ltmem
  "Prints a table version of [[ms-list]]."
  ([]
   (ms-table-ltmem ""))
  ([s]
   (pp/print-table
    (mapv
     #(update % :display utils/short-string)
     (ms-list-ltmem s)))))


(defn ms-table-stmem
  "Prints a table of all mpds available at stmem."
  []
   (pp/print-table
    (mapv
     #(dissoc % :seq-idx :par-idx :func)
     (stmem/get-maps {:mp-id :* :struct :meta :metapath :descr}))))

(defn ms-active
  "Prints a table of all active mpds"
  []
  (pp/print-table (stmem/registered)))

(defn m-build
  "Builds the mpd with the given `mp-id`."
  [mp-id]
  (-> mp-id ltmem/get-doc utils/map->safe-map model/build-mpd))
 
(defn m-build-ref 
  "Builds the reference mpd `mpd-ref.edn`."
  []
  (model/build-mpd (c/mpd-ref)))

(defn m-clear [mp-id] (model/clear-mpd {:mp-id mp-id}))  

(defn m-start [mp-id] (scheduler/start {:mp-id mp-id}))

(defn m-stop [mp-id] (scheduler/stop {:mp-id mp-id}))

;;------------------------------
;; e- exchange commands
;;------------------------------
(defn e-all [mp-id] (exchange/all {:mp-id mp-id}))

;;------------------------------
;; c- container commands
;;------------------------------
(defn c-run
  "Runs `c`ontainer `i` of `mp-id`."
  [mp-id i]
  (stmem/set-val {:mp-id mp-id :struct :cont :no-idx i :func :ctrl :value :run}))

(defn c-mon
  "Monitors (cyclic runs) `c`ontainer `i` of `mp-id`."
  [mp-id i]
  (stmem/set-val {:mp-id mp-id :struct :cont :no-idx i :func :ctrl :value :mon}))

(defn c-stop
  "Stops `c`ontainer `i` of `mp-id`."
  [mp-id i]
  (stmem/set-val {:mp-id mp-id :struct :cont :no-idx i :func :ctrl :value :stop}))

(defn c-suspend
  "Suspends `c`ontainer `i` of `mp-id`."
  [mp-id i]
  (stmem/set-val {:mp-id mp-id :struct :cont :no-idx i :func :ctrl :value :suspend}))

(defn c-reset
  "Resets `c`ontainer `i` of `mp-id`."
  [mp-id i]
  (stmem/set-val {:mp-id mp-id :struct :cont :no-idx i :func :ctrl :value :reset}))

(defn c-maps
  "Prints the state table of `c`ontainer `i` of `mp-id`."
  [mp-id i]
  (stmem/get-maps {:mp-id mp-id :struct :cont :no-idx i :func :state :seq-idx :* :par-idx :* }))

(defn c-state
  "Prints the state table of `c`ontainer `i` of `mp-id`."
  [mp-id i]
  (pp/print-table (c-maps mp-id i)))

(defn c-ctrl
  "Prints the ctrl table of `c`ontainer `i` of `mp-id`."
  [mp-id i]
  (pp/print-table
   (stmem/get-maps {:mp-id mp-id :struct :cont :no-idx i :func :ctrl})))

(defn cs-title
  "Prints the title table of all `c`ontainer of `mp-id`."
  [mp-id]
  (pp/print-table
   (stmem/get-maps {:mp-id mp-id :struct :cont :func :title :no-idx :*  })))

;;------------------------------
;; t- task commands
;;------------------------------
(defn t-build [] (model/build-tasks (ltmem/all-tasks)))

(defn t-clear [] (model/clear-tasks))

(defn t-run
  "Runs task at position `m`. `m` may be provided by [[c-maps]]."
  [m]
  (worker/run m))

;;------------------------------
;; d- document commands
;;------------------------------
(defn d-add [mp-id doc-id] (document/add {:mp-id mp-id} doc-id))

(defn d-rm [mp-id doc-id] (document/rm {:mp-id mp-id} doc-id))

(defn d-ids [mp-id] (document/ids {:mp-id mp-id}))
