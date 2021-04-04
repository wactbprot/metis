(ns metis.config.core
  (:require [clojure.edn     :as edn]
            [clojure.java.io :as io]
            [clojure.string  :as string]))

(defn get-config
  "Reads a `edn` configuration in file `f`." 
  ([]
   (get-config (io/file "config.edn")))
  ([f]
   (-> f slurp edn/read-string)))

(defn ltmem-conn [c]
  (let [lt-srv (System/getenv "CMP_LT_SRV")
        usr    (System/getenv "CAL_USR")
        pwd    (System/getenv "CAL_PWD")]
    (str (:ltmem-prot c) "://"
         (when (and usr pwd) (str usr ":" pwd "@"))
         (or lt-srv (:ltmem-srv c)) ":"
         (:ltmem-port c) "/"
         (:ltmem-db c))))

(defn stmem-conn [c]
  {:pool {}
   :spec {:host (:stmem-srv c)
          :port (:stmem-port c)
          :db   (:stmem-db c)}})

(def config
  (let [c (get-config)]
    (assoc c 
           :ltmem-conn (ltmem-conn c)
           :stmem-conn (stmem-conn c)
           :re-sep (re-pattern (:stmem-key-sep c)))))
