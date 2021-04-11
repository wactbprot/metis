(ns metis.cli.core
  (:require [metis.config.interface :as c]
            [metis.model.interface :as model]
            [metis.ltmem.interface :as ltmem]
            [metis.stmem.interface :as stmem]))

(defn m-get [id] (ltmem/get-doc id))

(defn ms-list [] (mapv :id (ltmem/all-mpds)))

(defn m-build [id] (-> id ltmem/get-doc model/build))

(defn m-build-ref [] (model/build (c/mpd-ref)))

(defn m-clear [id] (model/clear {:mp-id id}))  