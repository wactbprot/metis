(ns metis.config.core
  ^{:author "Thomas Bock <wactbprot@gmail.com>"
    :doc "Provides configuration data. Reads and assoc env vars."}
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as string]))

(defn get-config
  "Reads a `edn` configuration in file `f`." 
  ([] (get-config (io/resource "config/config.edn")))
  ([f] (-> f slurp edn/read-string)))

(defn ltmem-base-url [c]
  (let [lt-srv (System/getenv "METIS_LTMEM_HOST")
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

(defn build-on-start [c]
  (if-let [s (System/getenv "METIS_BUILD_ON_START")]
    (string/split s  #"[;,\s]")
    (:build-on-start c)))

(defn dev-hub-url [c]
   (if-let [url (System/getenv "METIS_DEVHUB_URL")]
     url
     (:dev-hub-url c)))

(defn dev-proxy-url [c]
   (if-let [url (System/getenv "METIS_DEVPROXY_URL")]
     url
     (:dev-proxy-url c)))

(defn global-log-context [{app-name :app-name}]
  {:facility (System/getenv "METIS_FACILITY")
   :app-name app-name})

(defn swap-key-val [m] (into {} (mapv (fn [[k v]] {v k}) m)))

(def config
  (let [c (get-config)]
    (assoc c
           :global-log-context (global-log-context c)
           :build-on-start (build-on-start c)
           :dev-hub-url (dev-hub-url c)
           :dev-proxy-url (dev-proxy-url c)
           :ltmem-base-url (ltmem-base-url c)
           :ltmem-conn (ltmem-conn c)
           :stmem-conn (stmem-conn c)
           :stmem-retrans  (swap-key-val (:stmem-trans c))
           :re-sep (re-pattern (:stmem-key-sep c)))))
