(ns metis.log.interface
  (:require [metis.log.core :as core]))


(defn start [] (core/start))

(defn stop [] (core/stop))
