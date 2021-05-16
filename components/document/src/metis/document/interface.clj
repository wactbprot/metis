(ns metis.document.interface
  (:require [metis.document.core :as core]))

(defn add [m id] (core/add m id))

(defn rm [m id] (core/rm m id))

(defn store-results [m results doc-path] (core/store-results m results doc-path)) 

(defn renew [m v] (core/renew m v))

(defn ids [m] (core/ids m))
