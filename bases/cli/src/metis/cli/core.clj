(ns metis.cli.core
  (:require [metis.ltmem.interface :as ltmem]
            [metis.stmem.interface :as stmem]))


(defn ml [id] (ltmem/get-doc id))
 
(defn lm [] (ltmem/all-mpds))

(defn m-build [id]
 (-> id ltmem/get-doc stmem/build)  