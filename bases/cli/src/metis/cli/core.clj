(ns metis.cli.core
  (:require [metis.config.interface :as c]
            [metis.exchange.interface :as exchange]
            [metis.model.interface :as model]
            [metis.ltmem.interface :as ltmem]
            [clojure.pprint :as pp]
            [metis.stmem.interface :as stmem]
            [clojure.string :as string]
            [metis.utils.interface :as utils]))

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
