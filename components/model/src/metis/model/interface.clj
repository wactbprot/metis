(ns metis.model.interface
  (:require [metis.model.core :as core]))

(defn build-mpd [m] (core/build-mpd m))

(defn clear-mpd [m] (core/clear-mpd m))
