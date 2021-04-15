(ns metis.flow-control.interface
  (:require [metis.flow-control.core :as core]))


(defn set-state [m] (core/set-state [m]))

(defn set-states [m] (core/set-states [m]))

(defn set-ctrl [m] (core/set-ctrl [m]))

