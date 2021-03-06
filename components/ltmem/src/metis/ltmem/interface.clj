(ns metis.ltmem.interface
  (:require [metis.ltmem.core :as core]))

(defn get-doc
  ([id] (core/get-doc id))
  ([conf id] (core/get-doc conf id)))

(defn get-safe-doc
  ([id] (core/safe (core/get-doc id)))
  ([conf id] (core/safe (core/get-doc conf id))))

(defn exist?
  ([id] (core/exist? id))
  ([conf id](core/exist? conf id)))

(defn rev-refresh
  ([doc] (core/rev-refresh doc))
  ([conf doc] (core/rev-refresh conf doc)))
  
(defn put-doc
  ([doc] (core/put-doc doc))
  ([conf doc] (core/put-doc conf doc)))
 
(defn get-task
  ([s] (core/get-task s))
  ([conf s] (core/get-task conf s)))
 
(defn all-mpds
  ([] (core/all-mpds))
  ([conf] (core/all-mpds conf)))
