(ns metis.exchange.interface
  (:require [metis.exchange.api :as api]))

(defn all [m] (api/all m))

(defn from [a m] (api/from m))

(defn to [a m] (api/to m))

(defn run-if [a m] (api/run-if m))

(defn stop-if [a m] (api/stop-if m))

(defn only-if-not [a m] (api/only-if-not m))
