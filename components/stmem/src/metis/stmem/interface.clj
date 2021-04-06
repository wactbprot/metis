(ns metis.stmem.interface
  (:require [metis.stmem.trans :as trans]))

(defn set-val [m] (trans/set-val m))

(defn get-val [m] (trans/get-val m))

(defn del-val [m] (trans/del-val m))

(defn del-vals [m] (trans/del-vals m))
