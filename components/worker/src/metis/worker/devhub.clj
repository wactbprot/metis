(ns metis.worker.devhub
  ^{:author "wactbprot"
    :doc "The devhub worker."}
  (:require [cheshire.core :as che]
            [metis.config.interface :as c]
            [com.brunobonacci.mulog :as µ]
            [metis.worker.resp :as resp]
            [metis.stmem.interface :as stmem]
            [metis.utils.interface :as u]))

(defn devhub!
  "Sends `:Value` to [devhub](https://wactbprot.github.io/devhub/) which
  resolves `PreScript` and `PostScript`.
  
  ```clojure
   (devhub! {:Action \"TCP\" :Port 23 :Host \"localhost\" :Value \"Hi!\"})
  ```"
  [task m]
  (stmem/set-state-working (assoc m :message "start devhub request"))
  (let [req (assoc (:json-post-header c/config) :body (che/encode task))
        url (cfg/dev-hub-url (cfg/config))]
    (stmem/set-val (assoc m :func :req :value task))
    (µ/log ::devhub! :message "stored task, send request" :url url :m m)
    (try
      (resp/check (http/post url req) task m)
      (catch Exception e (stmem/set-state-error (assoc m :message (.getMessage e)))))))
