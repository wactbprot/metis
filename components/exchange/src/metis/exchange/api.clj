(ns metis.exchange.api
  ^{:author "wactbprot"
    :doc "Handles the access to the exchange interface."}
  (:require [metis.exchange.core :as core]
            [com.brunobonacci.mulog :as mu]
            [metis.stmem.interface :as stmem]))

(defn all [{mp-id :mp-id}]
  (into {} (map
            (fn [m] {(:no-idx m) (:value m)})
            (stmem/get-maps {:mp-id mp-id :struct :exch :exchpath :*}))))

(defn to
  ([m]
   (to (all m) m))
  ([a m]
   (doall
    (map stmem/set-val (core/to-vec a (assoc m :struct :exch))))))

(defn from
  "Builds a map by replacing the values of the input map `m`.
  The replacements are gathered from `a` the complete exchange interface

  Example:
  ```clojure
  (def all {\"A\" {:Type \"ref\", :Unit \"Pa\", :Value 100.0},
         \"B\" \"token\",
         \"Target_pressure\" {:Selected 1, :Unit \"Pa\"}})

  (def m {:%check A})
  
  (from all m)
  ;; =>
  ;; {:%check {:Type \"ref\" :Unit \"Pa\" :Value 100.0}}
  ```"
  ([m]
   (from (all m) m))
  ([a m]
  (when (and (map? a) (map? m))
    (into {} (map (fn [[k p]] {k (core/get-val a p)}) m)))))
