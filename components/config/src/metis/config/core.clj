(ns metis.config.core
  (:require [clojure.edn     :as edn]
            [clojure.java.io :as io]
            [clojure.string  :as string]))

(defn get-config
  "Reads a `edn` configuration in file `f`." 
  ([]
   (get-config (io/resource "config/config.edn")))
  ([f]
   (-> f slurp edn/read-string)))

(defn ltmem-base-url [c]
  (let [lt-srv (System/getenv "CMP_LT_SRV")
        usr    (System/getenv "CAL_USR")
        pwd    (System/getenv "CAL_PWD")]
    (str (:ltmem-prot c) "://"
         (when (and usr pwd) (str usr ":" pwd "@"))
         (or lt-srv (:ltmem-srv c)) ":"
         (:ltmem-port c))))

(defn ltmem-conn [c] (str (ltmem-base-url c) "/" (:ltmem-db c)))

(defn stmem-conn [c]
  {:spec {:host (:stmem-srv c)
          :port (:stmem-port c)
          :db   (:stmem-db c)}
   :pool {}})

(def config
  (let [c (get-config)]
    (assoc c
           :ltmem-base-url (ltmem-base-url c)
           :ltmem-conn (ltmem-conn c)
           :stmem-conn (stmem-conn c)
           :stmem-retrans (into {} (map (fn [[k v]] {v k})) (:stmem-trans c))
           :re-sep (re-pattern (:stmem-key-sep c)))))
