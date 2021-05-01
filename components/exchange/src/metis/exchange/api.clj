(ns metis.exchange.api
  ^{:author "wactbprot"
    :doc "Handles the access to the exchange interface."}
  (:require [metis.exchange.core :as core]
            [com.brunobonacci.mulog :as mu]
            [metis.stmem.interface :as stmem]))

(defn all [{mp-id :mp-id}]
  (into {} (map
            (fn [m] {(:no-idx m) (:value m)})
            (stmem/get-maps {:mp-id mp-id :struct :exch :no-idx :*}))))

(defn to
  ([m]
   (to (all m) m))
  ([a m]
  (map stmem/set-val (core/to-vec a m))))

(defn from
  "Builds a map by replacing the values of the input map `m`.
  The replacements are gathered from `a` the complete exchange interface
  
  Example:
  ```clojure
  (from {\"A\" {:Type \"ref\", :Unit \"Pa\", :Value 100.0},
         \"B\" \"token\",
         \"Target_pressure\" {:Selected 1, :Unit \"Pa\"}} {:%check A})
  ;; =>
  ;; {:%check {:Type \"ref\" :Unit \"Pa\" :Value 100.0}}
  ```"
  ([m]
   (from (all m) m))
  ([a m]
  (when (and (map? a) (map? m))
    (into {} (map (fn [[k p]] {k (core/get-val a p)}) m)))))
