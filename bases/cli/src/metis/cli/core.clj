(ns metis.cli.core
  (:require [metis.ltmem.interface :as ltmem]
            [metis.stmem.interface :as stmem]))

(defn m-get [id] (ltmem/get-doc id))
 
(defn ms-list [] (ltmem/all-mpds))

(defn m-build [id] (-> id ltmem/get-doc stmem/build))  