(ns metis.config.interface
  (:require [metis.config.core :as core]
            [clojure.edn :as edn]
            [clojure.java.io :as io]))

(def config core/config)

(defn mpd-ref [] (-> (io/file (:ref-mpd config)) slurp edn/read-string))