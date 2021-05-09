(ns metis.worker.resp
  ^{:author "wactbprot"
    :doc "Catches responses and dispatchs."}
  (:require [cheshire.core :as che]
            [metis.config.interface :as c]
            [metis.exchange.interface :as exch]
            [com.brunobonacci.mulog :as µ]
            [metis.stmem.interface :as stmem]
            [clojure.string :as string]
            [metis.utils.interface :as u]))
(comment
(defn do-retry
  ([m]
   (do-retry c/config m))
  ([{max-retry :max-retry} m]
   (let [m (assoc m :func :retry)
         n (stmem/get-val m)]
     (if (>= (u/ensure-int n) max-retry)
       (do
         (µ/log ::retry! :error "reached max-retry" :m m)
         (stmem/set-val (assoc m :value 0))
         {:error "max retry"})
       (do
         (µ/log ::retry! :message "retry" :m m)
         (stmem/set-val (assoc m :value (inc n)))
         {:ok "retry"})))))



(defn dispatch
  "Dispatches responds from outer space. Expected responses are:
  
  * Result ... data will be stored in long term memory 
  * ToExchange ... data goes to exchange interface
  * ids ... renew the active documents 
  * error ... state will be set to error, processing stops 
  
  It's maybe a good idea to save the respons body to a key associated
  to the state key (done).
  
  TODO: detect missing values that should be measured again.
  Solution: Missing or wrong values are detected at postscripts which
  leads to `:Retry true`. "
  [body task m]
  (µ/log ::dispatch :message "try to write response" :m m)
  (stmem/set-val (assoc :func :resp :value body))
  (if-let [err (:error body)]
    (stmem/set-state-error (assoc m :message err))
    (let [
          retry    (:Retry      body)
          to-exch  (:ToExchange body)
          results  (:Result     body)
          ids      (:ids        body)
          doc-path (:DocPath    task)
          mp-id    (:MpName     task)]
      
      (if retry
        (let [res (do-retry m)
              m   (assoc m :func :state)]
          (cond
            (:error res) (stmem/set-state-error m)
            (:ok    res) (stmem/set-state-ready m)))
        (let [res-ids   (doc/renew m ids)
              res-exch  (exch/to (exch/all m) to-exch)
              res-doc   (doc/store results doc-path m)
              m         (assoc m :func :state)]
          (cond
            (:error res-exch) (stmem/set-state-error (assoc m :message "error at exchange"))
            (:error res-doc)  (stmem/set-state-error (assoc m :message "error at document"))
            (and
             (:ok res-ids)
             (:ok res-exch)     
             (:ok res-doc))  (if (exch/stop-if (exch/all m) task)
                               (stmem/set-state-executed m)
                               (stmem/set-state-ready m))
            :unexpected (stmem/set-state-error (assoc m :message "unexpected response"))))))))

;;------------------------------
;; check
;;------------------------------
(defn check
  "Checks a response from outer space.  Lookes at the status, parses the
  body and dispathes."
  [res task m]
  (if-let [status (:status res)]
    (if-let [body (che/decode (:body res) true)]
      (if (< status 400) 
        (dispatch body task m) 
        (µ/log ::check :error "request for failed" :m m ))            
      (µ/log ::check :error "body can not be parsed" :m m))
    (µ/log ::check :error "no status in header" :m m)))
)
