(ns metis.stmem.api
  (:require [metis.stmem.core :as core]
            [metis.config.interface :as c]
            [cheshire.core :as che]
            [clojure.string :as string]
            [metis.stmem.trans :as trans]))

(defn get-val
  ([m]
   (get-val c/config m))
  ([config m]
   (che/parse-string-strict (core/get-val (trans/map->key m)) true)))

(defn get-map
  ([m]
   (get-val c/config m))
  ([config m]
   (assoc m :value (get-val config m))))

(defn get-maps
  ([m]
   (get-maps c/config m))
  ([config m]
   (mapv (fn [k]
           (merge
            (assoc m :value (che/parse-string-strict (core/get-val k) true))
            (trans/key->map k)))
         (core/pat->keys (trans/map->key m)))))

(defn set-val
  ([m]
   (set-val c/config m))
  ([{relax :stmem-mod-relax} {value :value :as m}]
   (core/set-val (trans/map->key m) (che/generate-string value))
   (Thread/sleep relax)
   {:ok true}))

(defn set-vals
  ([m]
   (set-vals c/config m))
  ([{relax :stmem-mod-relax} {value :value :as m}]
   (core/set-vals (core/pat->keys (trans/map->key m)) (che/generate-string value))
   (Thread/sleep relax)
   {:ok true}))

(defn del-val
  ([m]
   (del-val c/config m))
  ([{relax :stmem-mod-relax} m]
   (core/del-val (trans/map->key m))
   (Thread/sleep relax)
   {:ok true}))

(defn del-vals
  ([m]
   (del-vals c/config m))
  ([config m]
   (core/del-vals (core/pat->keys (trans/map->key m)))
   {:ok true}))
