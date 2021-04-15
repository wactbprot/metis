(ns metis.flow-control.core
  (:require [metis.stmem.interface :as stmem]
            [com.brunobonacci.mulog :as mu]))

;;------------------------------
;; ctrl
;;------------------------------
(defn set-ctrl
  "Sets the `ctrl` interface to `(:value m)`."
  [m]
  (let [kw  (:value m)
        msg (:message m)]
    (if (= kw :error )
      (mu/log ::set-ctrl :error "will set ctrl interface to error")
      (mu/log ::set-ctrl :message (str "will set ctrl interface to " kw)))
    (stmem/set-val (assoc (dissoc m :par-idx :seq-idx) :func :ctrl :value (name kw)))))

;;------------------------------
;; state
;;------------------------------
(defn set-states 
  "Sets all states (the state interface) to almost ready."
  [m]
  (let [kw  (:value m)
        msg (:message m)]
    (mu/log ::set-states :message (or msg  "will set all states"))
    (stmem/set-vals (assoc m :func :state :seq-idx :* :par-idx :* :value (name kw)))))

(defn set-state
  "Sets the state to `(:value m)`."
  [m] 
  (let [kw  (:value m)
        msg (:message m)]
    (if (= kw :error )
      (mu/log ::set-state :error (or msg "will set stat interface to error"))
      (mu/log ::set-state :message (str (or msg "will set state interface to " kw))))
    (stmem/set-val (assoc m :func :state :value (name kw)))))
  
