(ns metis.exchange.interface
  (:require [metis.exchange.api :as api]))

(defn all [m] (api/all m))

(defn from [m] (api/from m))

(defn to [m] (api/to m))
