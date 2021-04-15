(ns metis.utils.interface
  (:require [metis.utils.core :as core]))


(defn date-map [] (core/date-map))

(defn ensure-int [i] (core/ensure-int i))

(def ok-set core/ok-set)

(defn map->safe-map [m] (core/map->safe-map m))

(defn short-string [] (core/short-string))
