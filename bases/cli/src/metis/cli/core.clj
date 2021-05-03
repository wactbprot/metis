(ns metis.cli.core
  (:require [metis.config.interface :as c]
            [metis.exchange.interface :as exchange]
            [metis.model.interface :as model]
            [metis.ltmem.interface :as ltmem]
            [com.brunobonacci.mulog :as µ]
            [clojure.pprint :as pp]
            [metis.stmem.interface :as stmem]
            [metis.scheduler.interface :as scheduler]
            [clojure.string :as string]
            [metis.utils.interface :as utils]))

;;------------------------------
;; logging system
;;------------------------------
(defonce logger (atom nil))

(defn log-stop
  ([]
   (log-stop c/config))
  ([conf]
   (µ/log ::stop)
   (@logger)
   (reset! logger nil)))

(defn log-start
   ([]
   (log-start c/config))
  ([{conf :mulog}]
   (µ/set-global-context! {:app-name "metis"})
   (reset! logger (µ/start-publisher! conf))
   conf))
  
;;------------------------------
;; m- mpd commands
;;------------------------------
(defn m-get [mp-id]
  "Get the measurement program definition (mpd) from the longterm memory (ltm)."
  (ltmem/get-doc mp-id))

(defn ms-list
  "Returns a list of maps of the `mpd`s available at ltmem. Filters
  the ids by the optional given substring."
  ([]
   (ms-list ""))
  ([s]
   (filter
    #(when (map? %) (string/includes? (:id %) s))
    (ltmem/all-mpds))))

(defn ms-table
  "Prints a table version of [[ms-list]]."
  ([]
   (ms-table ""))
  ([s]
   (pp/print-table
    (mapv
     #(update % :display utils/short-string)
     (ms-list s)))))

(defn m-build [mp-id]
  "Builds the mpd with the given `mp-id`."
  (-> mp-id ltmem/get-doc model/build-mpd))
 
(defn m-build-ref [] (model/build-mpd (c/mpd-ref)))

(defn m-clear [mp-id] (model/clear-mpd {:mp-id mp-id}))  

(defn e-all [mp-id] (exchange/all {:mp-id mp-id}))

(defn m-start [mp-id] (scheduler/start mp-id))

(defn m-stop [mp-id] (scheduler/stop mp-id))


;;------------------------------
;; c- container commands
;;------------------------------
(defn c-run [mp-id i] (stmem/set-val {:mp-id mp-id :struct :cont :no-idx i :func :ctrl :value :run}))

(defn c-mon [mp-id i] (stmem/set-val {:mp-id mp-id :struct :cont :no-idx i :func :ctrl :value :mon}))

(defn c-stop [mp-id i] (stmem/set-val {:mp-id mp-id :struct :cont :no-idx i :func :ctrl :value :stop}))

(defn c-suspend [mp-id i] (stmem/set-val {:mp-id mp-id :struct :cont :no-idx i :func :ctrl :value :suspend}))


;;------------------------------
;; t- task commands
;;------------------------------
(defn t-build [] (model/build-tasks (ltmem/all-tasks)))

(defn t-clear [] (model/clear-tasks))
