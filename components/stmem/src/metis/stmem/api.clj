(ns metis.stmem.api
  (:require [metis.stmem.core :as core]))

;; the idea is: no key operation outside stmem
;; the stmem api converts maps to keys and vice versa
;; this should keep everything outside mostly free of side effects

;;------------------------------
;; exchange
;;------------------------------
(defn build-exchange
  "Builds the exchange interface."
  [{mp-id :mp-id exch :Exchange}]
  (doseq [[k v] exch]
    (core/set-val (stu/exch-key p (name k)) v)))


(defn build
  [{mp-id :_id {exch :Exchange} :Mp}]
  (build-exchange {:mp-id mp-id :Exchange exch}))