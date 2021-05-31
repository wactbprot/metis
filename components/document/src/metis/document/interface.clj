(ns metis.document.interface
  (:require [metis.document.core :as core]))

(defn add
  ([m id]
   (core/add m id))
  ([conf m id]
   (core/add conf m id)))

(defn rm
  ([m id]
   (core/rm m id))
  ([conf m id]
   (core/rm conf m id)))

(defn store-results
  ([m results doc-path]
   (core/store-results m results doc-path)) 
  ([conf m results doc-path]
   (core/store-results conf m results doc-path)))

(defn renew
  ([m v]
   (core/renew m v))
  ([conf m v]
   (core/renew conf m v)))

(defn ids [m] (core/ids m))
