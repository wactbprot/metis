(ns metis.ws.interface
  ^{:author "wactbprot"
    :doc "Web socket interface."}
  (:require [metis.ws.core :as core]))

(defn main [req] (core/main req))

(defn start [m] (core/start m))

(defn stop [m] (core/stop m))



