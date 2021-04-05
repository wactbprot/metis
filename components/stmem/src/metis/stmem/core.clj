(ns metis.stmem.core
  (:require [metis.config.interface :as c]
            [metis.stmem.trans :as trans]
            [taoensso.carmine :as car :refer (wcar)]
            [cheshire.core :as che]
            [com.brunobonacci.mulog :as mu]
            [clojure.string :as string]))

;;------------------------------
;; stmem set
;;------------------------------
(defn set-val
  "Sets the value `v` for the key `k`."
  [k v]
  (if (string? k)
    (if (some? v)
      (wcar (:stmem-conn c/config) (car/set k v))
      (mu/log ::set :error "no value given"))
    (mu/log ::set :error "no key given")))

(defn set-same-val
  "Sets the given `val` for all keys `ks` with the delay `mtp`."
  [ks v]
  (run! (fn [k] (set-val k v)) ks))

;;------------------------------
;; get keys
;;------------------------------
(defn pat->keys
  "Get all keys matching  the given pattern `pat`."
  [pat]
  (sort (wcar (:stmem-conn c/config) (car/keys pat))))
 
;;------------------------------
;; del
;;------------------------------
(defn del-val
  "Delets the key `k`."
  [k]
  (wcar (:stmem-conn c/config) (car/del k)))

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
  [k]
  (wcar (:stmem-conn c/config) (car/get k)))

(defn get-vals
  "Returns a vector of the `vals` behind the keys `ks`."
  [ks]
  (mapv get-val ks))

(defn filter-keys-where-val
  "Returns a list of all keys belonging to the pattern `pat` where the
  value is equal to`x`.
  
  Example:
  ```clojure
  (filter-keys-where-val \"ref@definitions@*@class\" \"wait\")
  ;; (\"ref@definitions@0@class\"
  ;; \"ref@definitions@2@class\"
  ;; \"ref@definitions@1@class\")
  ```"
  [pat x]
  (filter (fn [k] (= x (get-val k))) (pat->keys pat)))
