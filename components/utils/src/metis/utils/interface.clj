(ns metis.utils.interface
  (:require [metis.utils.core :as core]))

(defn date-map [] (core/date-map))

(defn get-date [] (core/get-date))

(defn get-time [] (core/get-time))

(defn ensure-int [i] (core/ensure-int i))

(defn short-string [s] (core/short-string s))

(defn apply-to-map-values [f m] (core/apply-to-map-values f m)) 

(defn apply-to-map-keys [f m]  (core/apply-to-map-keys f m))
