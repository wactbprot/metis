(ns metis.worker.date-time
  ^{:author "wactbprot"
    :doc "Worker to create a date entry in documents."}
  (:require [metis.config.interface :as c]
            [metis.document.interface :as doc]
            [metis.exchange.interface :as exch]
            [com.brunobonacci.mulog  :as µ]
            [metis.stmem.interface :as stmem]
            [metis.utils.interface :as u]))

(defn write
  [conf {type :Type doc-path :DocPath exch-path :ExchangePath} value m]
  (let [val-m {:Type type :Value value}]
    (when exch-path
      (prn "..")
      (exch/to (exch/all m) (assoc m :exchpath exch-path :value val-m)))
    (if (:ok (doc/store-results conf m [val-m] doc-path))
      (stmem/set-state-executed (assoc m :message "get time executed"))
      (stmem/set-state-error (assoc m :message "unexpected return value")))))
  
(defn store-date!
  "Generates this date object: `[{:Type <type> :Value (u/get-date)}]`
  and stores it under  `DocPath`."
  ([task m]
   (store-date! c/config task m))
  ([conf task m]
   (stmem/set-state-working m)
   (write conf task (u/get-date) m)))

(defn store-time!
  "Generates a timestamp object `{:Type <type> :Value (u/get-time)}` and
  stores it under `:DocPath`."
  ([task m]
   (store-time! c/config task m))
  ([conf task m]
   (stmem/set-state-working m)
   (write conf task (u/get-time) m)))

(comment
  {:TaskName "Common-get_date"
   :Action "getDate"
   :DocPath "Calibration.Measurement.Date"
   :Type "%type"})
