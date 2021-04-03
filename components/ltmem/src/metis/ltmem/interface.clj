(ns metis.ltmem.interface
  (:require [metis.ltmem.core :as core]))

(defn get-doc [id] (core/get-doc id))

(defn exist? [id] (core/exist? id))

(defn rev-refresh [doc] (core/rev-refresh doc))

(defn put-doc [doc] (core/put-doc doc))

(defn all-tasks [] (core/all-tasks))

(defn all-mpds [] (core/all-mpds))
