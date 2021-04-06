(ns metis.cli.core
  (:require [metis.model.interface :as model]
            [metis.ltmem.interface :as ltmem]
            [metis.stmem.interface :as stmem]))

(defn m-get [id] (ltmem/get-doc id))
 
(defn ms-list [] (ltmem/all-mpds))

(defn m-build [id] (-> id ltmem/get-doc model/build))

(defn m-clear [id] (model/clear {:mp-id id}))  