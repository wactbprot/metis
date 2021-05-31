(ns metis.worker.exchange
  ^{:author "wactbprot"
    :doc "Reads and writes from and to the exchange interface."}
  (:require [metis.document.interface :as doc]
            [metis.exchange.interface :as exch]
            [com.brunobonacci.mulog :as µ]
            [metis.stmem.interface :as stmem]))

(defn read!
  "Reads the `exch-val` from `:ExchangePath` and writes the result to
  `:DocPath`. The `exch-val` have to be turned into a vector, to fit
  the `doc/store!` function."
  [{doc-path :DocPath exch-path :ExchangePath :as task} m]
  (stmem/set-state-working m)
  (let [all      (exch/all m)
        value    (or (get all (keyword exch-path)) (get all exch-path))
        res-doc  (doc/store-results m [value] doc-path)]
    (µ/log ::read! :message "read value from exchange" :m (assoc m :value value))
    (if (and value (:ok res-doc))
      (if (exch/stop-if all task) (stmem/set-state-executed m) (stmem/set-state-ready m))
      (stmem/set-state-error (assoc m :message "error on attempt to write exch value")))))

(defn write!
  "Writes the `:Value` to the exchange."
  [{value :Value exch-path :ExchangePath :as task} m]
  (stmem/set-state-working m)
  (let [all    (exch/all m)
        exch-m (assoc m :value value :struct :exch :no-idx exch-path)
        ret    (exch/to (exch/all m) exch-m)]
    (µ/log ::write! :message "write value to exchange" :m exch-m)
    (if (:ok ret)
      (if (exch/stop-if all task) (stmem/set-state-executed m) (stmem/set-state-ready m))
      (stmem/set-state-error (assoc m :message "error on attempt to write exchange")))))
