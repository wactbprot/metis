(ns metis.exchange.interface
  (:require [metis.exchange.api :as api]))

(defn all [m] (api/all m))

(defn from [a m] (api/from a m))

(defn to [a m] (api/to a m))
