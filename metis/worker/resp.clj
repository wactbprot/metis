✔ (ns metis.worker.resp
?   ^{:author "wactbprot"
?     :doc "Catches responses and dispatchs."}
?   (:require [cheshire.core :as che]
?             [metis.config.interface :as c]
?             [metis.exchange.interface :as exch]
?             [metis.document.interface :as doc]
?             [com.brunobonacci.mulog :as µ]
?             [metis.stmem.interface :as stmem]
?             [clojure.string :as string]
?             [metis.utils.interface :as u]))
  
✔ (defn do-retry
?   ([m]
✘    (do-retry c/config m))
?   ([{max-retry :max-retry} m]
✘    (let [m (assoc m :func :retry)
✘          n (or (stmem/get-val m) 0)]
✘      (if (>= (u/ensure-int n) max-retry)
✘        (do
✘          (µ/log ::retry! :error "reached max-retry" :m m)
✘          (stmem/set-val (assoc m :value 0))
✘          {:error "max retry"})
✘        (do
✘          (µ/log ::retry! :message "retry" :m m)
✘          (stmem/set-val (assoc m :value (inc n)))
✘          {:ok "retry"})))))
  
✔ (defn dispatch
?   "Dispatches responds from outer space. Expected responses are:
?   
?   * Result ... data will be stored in long term memory 
?   * ToExchange ... data goes to exchange interface
?   * ids ... renew the active documents 
?   * error ... state will be set to error, processing stops 
?   
?   It's maybe a good idea to save the respons body to a key associated
?   to the state key (done)."
?   [body task m]
✘   (µ/log ::dispatch :message "try to write response" :m m)
✘   (stmem/set-val (assoc m :func :resp :value body))
✘   (if-let [err (:error body)]
✘     (stmem/set-state-error (assoc m :message err))
✘     (let [res-retry (if (contains? body :Retry) (do-retry m) {:ok true})
✘           res-exch  (exch/to (exch/all m) (:ToExchange body))
✘           res-ids   (doc/renew m (:ids body))
✘           res-doc   (doc/store-results m (:Result body) (:DocPath task))]
✘       (cond
✘         (:error res-retry) (stmem/set-state-error (assoc m :message "error at retry"))
✘         (:error res-exch)  (stmem/set-state-error (assoc m :message "error at exchange"))
✘         (:error res-doc)   (stmem/set-state-error (assoc m :message "error at document"))
✘         (and
✘          (:ok res-retry)
✘          (:ok res-ids)
✘          (:ok res-exch)
✘          (:ok res-doc)) (if (exch/stop-if (exch/all m) task)
✘                           (stmem/set-state-executed m)
✘                           (stmem/set-state-ready m))
✘         :unexpected (stmem/set-state-error (assoc m :message "unexpected response"))))))
  
? ;;------------------------------
? ;; check
? ;;------------------------------
✔ (defn check
?   "Checks a response from outer space.  Lookes at the status, parses the
?   body and dispathes."
?   [result task m]
✘   (if-let [status (:status result)]
✘     (if-let [body (che/decode (:body result) true)]
✘       (if (< status 400)
✘         (dispatch body task m)
✘         (µ/log ::check :error "request for failed" :m m ))            
✘       (µ/log ::check :error "body can not be parsed" :m m))
✘     (µ/log ::check :error "no status in header" :m m)))
  
✔ (comment)
