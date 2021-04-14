(ns metis.model.interface
  (:require [metis.model.core :as core]))

(defn build-mpd [m] (core/build-mpd m))

(defn build-tasks [v] (core/build-tasks v))

(defn clear-mpd [m] (core/clear-mpd m))

(defn clear-tasks [] (core/clear-tasks))
