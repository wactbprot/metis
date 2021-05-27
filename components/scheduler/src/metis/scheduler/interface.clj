(ns metis.scheduler.interface
  [:require [metis.scheduler.core :as core]])

(defn start [mp-id] (core/start mp-id))

(defn stop [mp-id] (core/stop mp-id))

