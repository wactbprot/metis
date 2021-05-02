(ns metis.cli.core
  (:require [metis.config.interface :as c]
            [metis.exchange.interface :as exchange]
            [metis.model.interface :as model]
            [metis.ltmem.interface :as ltmem]
            [com.brunobonacci.mulog :as mu]
            [clojure.pprint :as pp]
            [metis.stmem.interface :as stmem]
            [metis.scheduler.interface :as scheduler]
            [clojure.string :as string]
            [metis.utils.interface :as utils]))

;;------------------------------
;; logging system
;;------------------------------
(defonce logger (atom nil))

(defn init-log! [{conf :mulog }]
  (mu/set-global-context! {:app-name "cmp"})
  (mu/start-publisher! conf))

(defn stop-log! [conf]
  (mu/log ::stop)
  (@logger)
  (reset! logger nil))

(defn start-log! [conf]
  (mu/log ::start)
  (reset! logger (init-log! conf)))

(defn m-get [id] (ltmem/get-doc id))

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

(defn m-build [id] (-> id ltmem/get-doc model/build-mpd))

(defn m-build-ref [] (model/build-mpd (c/mpd-ref)))

(defn m-clear [id] (model/clear-mpd {:mp-id id}))  

(defn t-build [] (model/build-tasks (ltmem/all-tasks)))

(defn t-clear [] (model/clear-tasks))

(defn e-all [id] (exchange/all {:mp-id id}))

(defn m-start [id] (scheduler/start id))

(defn m-stop [id] (scheduler/stop id))

