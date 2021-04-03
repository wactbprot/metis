(ns metis.cli.core
  (:require [metis.ltmem.interface :as ltmem]))

(defn ml [id] (ltmem/get-doc id))

(defn lm [] (ltmem/all-mpds))