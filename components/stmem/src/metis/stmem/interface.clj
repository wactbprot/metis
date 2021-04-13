(ns metis.stmem.interface
  (:require [metis.stmem.trans :as trans]
            [metis.stmem.notif :as notif]))

(defn set-val [m] (trans/set-val m))

(defn set-vals [m] (trans/set-vals m))

(defn get-val [m] (trans/get-val m))

(defn del-val [m] (trans/del-val m))

(defn del-vals [m] (trans/del-vals m))

(defn register [m f] (notif/register m f))

(defn de-register [m] (notif/de-register m))

(defn clean-register [m] (notif/clean-register m))
