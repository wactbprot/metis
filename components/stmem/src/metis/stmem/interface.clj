(ns metis.stmem.interface
  (:require [metis.stmem.api :as api]
            [metis.stmem.flow-control :as fc]
            [metis.stmem.notif :as notif]
            [metis.stmem.trans :as trans]))

(defn set-val [m] (api/set-val m))

(defn set-vals [m] (api/set-vals m))

(defn get-val [m] (api/get-val m))

(defn get-map [m] (api/get-map m))

(defn get-maps [m] (api/get-maps m))

(defn del-val [m] (api/del-val m))

(defn del-vals [m] (api/del-vals m))

(defn register [m f] (notif/register m f))

(defn de-register [m] (notif/de-register m))

(defn clean-register [m] (notif/clean-register m))

(defn set-state [m] (fc/set-state m))

(defn set-state-working [m] (fc/set-state (assoc m :value :working))) 

(defn set-state-executed [m] (fc/set-state (assoc m :value :executed)))

(defn set-state-error [m] (fc/set-state (assoc m :value :error))) 

(defn set-state-ready [m] (fc/set-state (assoc m :value :ready))) 

(defn set-states [m] (fc/set-states m))

(defn set-ctrl [m] (fc/set-ctrl m))

(defn map->key [m] (trans/map->key m))
