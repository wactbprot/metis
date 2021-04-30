(ns metis.scheduler.interface
  [:require [metis.scheduler.core :as core]])

(defn start [mp-id] (core/start-ctrl mp-id))

(defn stop [mp-id] (core/stop-ctrl mp-id))
