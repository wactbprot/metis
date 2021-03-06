(ns metis.stmem.flow-control
  (:require [metis.stmem.api :as stmem]
            [com.brunobonacci.mulog :as µ]))

;;------------------------------
;; ctrl
;;------------------------------
(defn set-ctrl
  "Sets the `ctrl` interface to `(:value m)`."
  [m]
  (let [kw  (:value m)
        msg (:message m)]
    (if (= kw :error )
      (µ/log ::set-ctrl :error "will set ctrl interface to error" :m m)
      (µ/log ::set-ctrl :message (str "will set ctrl interface to " kw) :m m))
    (stmem/set-val (assoc (dissoc m :par-idx :seq-idx) :func :ctrl :value (name kw)))))

;;------------------------------
;; state
;;------------------------------
(defn set-states 
  "Sets all states (the state interface) to almost ready."
  [m]
  (let [kw  (:value m)
        msg (:message m)]
    (µ/log ::set-states :message (or msg  "will set all states") :m m)
    (stmem/set-vals (assoc m :func :state :seq-idx :* :par-idx :* :value (name kw)))))

(defn set-state
  "Sets the state to `(:value m)`."
  [m] 
  (let [kw  (:value m)
        msg (:message m)]
    (if (= kw :error )
      (µ/log ::set-state :error (or msg "will set stat interface to error") :m m)
      (µ/log ::set-state :message (or msg (str "will set state interface to " kw)) :m m))
    (stmem/set-val (assoc m :func :state :value (name kw)))))
