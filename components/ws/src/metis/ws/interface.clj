(ns metis.ws.interface
  ^{:author "wactbprot"
    :doc "Web socket interface."}
  (:require [metis.ws.core :as core]))

(defn main [req] (core/main req))

(defn start [] (core/start))

(defn stop [] (core/stop))



