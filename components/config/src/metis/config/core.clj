(ns metis.config.core
  (:require [clojure.edn     :as edn]
            [clojure.java.io :as io]
            [clojure.string  :as string]))

(defn get-config
  "Reads a `edn` configuration in file `f`." 
  ([]
   (get-config (io/resource "config.edn")))
  ([f]
   (-> f slurp edn/read-string)))

(defn- ltmem-url [c]
  (let [lt-srv (System/getenv "CMP_LT_SRV")
        usr    (System/getenv "CAL_USR")
        pwd    (System/getenv "CAL_PWD")
        cred   (when (and usr pwd) (str usr ":" pwd "@"))]
        (str (:lt-prot c) "://" cred  (or lt-srv (:lt-srv c)) ":"(:lt-port c))))

(def config
  (let [c (get-config)]
    {:ltmem-conn (str (ltmem-url c) "/"(:lt-db c))}))
