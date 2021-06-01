(ns metis.stmem.core
  (:require [metis.config.interface :as c]
            [taoensso.carmine :as car :refer (wcar)]
            [cheshire.core :as che]
            [com.brunobonacci.mulog :as mu]
            [clojure.string :as string]))

;;------------------------------
;; stmem set
;;------------------------------
(defn set-val
  "Sets the value `v` for the key `k`."
  ([k v]
   (set-val c/config k v))  
  ([{conn :stmem-conn} k v]
   (wcar conn (car/set k v))))

(defn set-vals
  "Sets the given `val` for all keys `ks` with the delay `mtp`."
  [ks v]
  (run! (fn [k] (set-val k v)) ks))

;;------------------------------
;; get keys
;;------------------------------
(defn pat->keys
  "Get all keys matching  the given pattern `pat`."
  ([pat]
   (pat->keys c/config pat))
  ([{conn :stmem-conn} pat]
   (sort (wcar conn (car/keys pat)))))
 
;;------------------------------
;; del
;;------------------------------
(defn del-val
  "Deletes the key `k`."
  ([k]
   (del-val c/config k))
  ([{conn :stmem-conn} k]
   (wcar conn (car/del k))))

(defn del-vals
  "Deletes all given keys (`ks`)."
  [ks]
  (run! del-val ks))

;;------------------------------
;; get value(s)
;;------------------------------
(defn get-val
  "Returns the value for the given key (`k`) and cast it to a clojure
  type."
  ([k]
   (get-val c/config k))
  ([{conn :stmem-conn} k]
   (wcar conn (car/get k))))

(defn get-vals
  "Returns a vector of the `vals` behind the keys `ks`."
  [ks]
  (mapv get-val ks))

(defn filter-keys-where-val
  "Returns a list of all keys belonging to the pattern `pat` where the
  value is equal to `x`.
  
  Example:
  ```clojure
  (filter-keys-where-val \"ref@definitions@*@class\" \"wait\")
  ;; (\"ref@definitions@0@class\"
  ;; \"ref@definitions@2@class\"
  ;; \"ref@definitions@1@class\")
  ```"
  [pat x]
  (filter (fn [k] (= x (get-val k))) (pat->keys pat)))
