(ns metis.exchange.interface
  (:require [metis.exchange.core :as core]))

(defn all [m] (core/all m))

(defn from [a m] (core/from a m))
