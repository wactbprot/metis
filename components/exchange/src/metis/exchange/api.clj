(ns metis.exchange.api
  ^{:author "wactbprot"
    :doc "Handles the access to the exchange interface."}
  (:require [metis.exchange.core :as core]
            [com.brunobonacci.mulog :as µ]
            [metis.stmem.interface :as stmem]))

(defn all 
  "Returns a map of the entire exchange interface.
  Note: `get-maps` does not return a map with `:exchpath` but
  `:no-idx`instead."
  [{mp-id :mp-id}]
  (into {} (mapv
            (fn [{k :no-idx v :value}] {k v})
            (stmem/get-maps {:mp-id mp-id :struct :exch :exchpath :*}))))

(defn to
  "Writes the value (given by :value keyword in `m` to the exchange interface"
  ([m] (to (all m) m))
  ([a m]
   (µ/trace ::to [:function "exchange/to"]
            (let [res (mapv stmem/set-val
                            (core/to-vec a (assoc m :struct :exch)))
                  err (filter :error res)]
              (if (empty? err)
                {:ok true}
                {:error "on attempt writing to exchange"})))))

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
  ([m] (from (all m) m))
  ([a m]
   (when (and (map? a) (map? m))
     (into {} (mapv (fn [[k p]] {k (core/get-val a p)}) m)))))
