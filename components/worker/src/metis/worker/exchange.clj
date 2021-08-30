(ns metis.worker.exchange
  ^{:author "wactbprot"
    :doc "Reads and writes from and to the exchange interface."}
  (:require [metis.config.interface :as c]
            [metis.document.interface :as doc]
            [metis.exchange.interface :as exch]
            [com.brunobonacci.mulog :as µ]
            [metis.stmem.interface :as stmem]))

(defn read!
  "Reads the `exch-val` from `:ExchangePath` and writes the result to
  `:DocPath`. The `exch-val` have to be turned into a vector, to fit
  the `doc/store!` function. No `StopIf` support needed."
  ([task m]
   (read! c/config task m))
  ([conf {doc-path :DocPath exch-path :ExchangePath :as task} m]
   (stmem/set-state-working m)
   (let [exch-val (:value (exch/from (exch/all m) {:value exch-path}))
         res-doc  (doc/store-results conf m [exch-val] doc-path)]
     (µ/log ::read! :message "read value from exchange" :m (assoc m :value exch-val))
     (if (and exch-val (:ok res-doc))
       (stmem/set-state-executed m)
       (stmem/set-state-error (assoc m :message "error on attempt to write exch value"))))))

(defn write!
  "Writes the `:Value` to the exchange. No `StopIf` support needed."
  [{value :Value exch-path :ExchangePath :as task} m]
  (let [exch-m (assoc m :value value :struct :exch :exchpath exch-path)
        ret    (exch/to (exch/all m) exch-m)]
    (µ/log ::write! :message "write value to exchange" :m exch-m)
    (if (empty? (filter :error ret))
      (stmem/set-state-executed m)
      (stmem/set-state-error (assoc m :message "error on attempt to write exchange")))))
