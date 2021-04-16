(ns metis.exchange.interface
  (:require [metis.exchange.core :as core]))

(defn all [m] (core/all m))

(defn from [m] (core/from m))

(defn to [m] (core/to m))
