(ns metis.stmem.interface
  (:require [metis.stmem.api :as api]
            [metis.stmem.trans :as trans]))

(defn build [doc] (api/build doc))

(defn set-val [m] (trans/set-val m))

(defn get-val [m] (trans/get-val m))