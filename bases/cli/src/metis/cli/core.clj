(ns metis.cli.core
  (:require [metis.config.interface :as c]
            [metis.exchange.interface :as exchange]
            [metis.model.interface :as model]
            [metis.ltmem.interface :as ltmem]
            [metis.stmem.interface :as stmem]))

(defn m-get [id] (ltmem/get-doc id))

(defn ms-list [] (ltmem/all-mpds))

(defn m-build [id] (-> id ltmem/get-doc model/build-mpd))

(defn m-build-ref [] (model/build-mpd (c/mpd-ref)))

(defn m-clear [id] (model/clear-mpd {:mp-id id}))  

(defn t-build [] (model/build-tasks (ltmem/all-tasks)))

(defn t-clear [] (model/clear-tasks))

(defn e-all [id] (exchange/all {:mp-id id}))
