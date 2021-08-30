(ns metis.worker.devhub
  ^{:author "wactbprot"
    :doc "The devhub worker."}
  (:require [cheshire.core :as che]
            [metis.config.interface :as c]
            [clj-http.client :as http]
            [com.brunobonacci.mulog :as µ]
            [metis.worker.resp :as resp]
            [metis.stmem.interface :as stmem]))

(defn devhub!
  "Sends `:Value` to [devhub](https://wactbprot.github.io/devhub/) which
  resolves `PreScript` and `PostScript`.
  
  NOTE: `resp/check` is not invoked if a HTTP 500 error occurs on `http/post`."
  ([task m]
   (devhub! c/config task m))
  ([{header :json-post-header url :dev-hub-url} task m]
   (stmem/set-val (assoc m :func :req :value task))
   (µ/log ::devhub! :message "stored task at, send request" :url url :m m)
   (try
     (resp/check (http/post url (assoc header :body (che/encode task))) task m)
     (catch Exception e
       (stmem/set-state-error (assoc m :message (.getMessage e)))))))

(comment
  (def m {:mp-id "test" :struct :cont :no-idx 0 :par-idx 0 :seq-idx 0 :func :resp})
  (def t {:Action "VXI11" :Device "gpib0,4" :Host "e75416" :Value "MEAS:PRES?\n"})
  (devhub! t m)
  (stmem/get-val m))
