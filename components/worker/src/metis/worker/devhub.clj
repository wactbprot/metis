(ns metis.worker.devhub
  ^{:author "wactbprot"
    :doc "The devhub worker."}
  (:require [cheshire.core :as che]
            [metis.config.interface :as c]
            [clj-http.client :as http]
            [com.brunobonacci.mulog :as µ]
            [metis.worker.resp :as resp]
            [metis.stmem.interface :as stmem]
            [metis.utils.interface :as u]))

(defn devhub!
  "Sends `:Value` to [devhub](https://wactbprot.github.io/devhub/) which
  resolves `PreScript` and `PostScript`.
  
  ```clojure
   (devhub! {:Action \"TCP\" :Port 23 :Host \"localhost\" :Value \"Hi!\"}
   {:mp-id \"test\" :struct :cont :no-idx 0 :par-idx 0 :seq-idx 0})
  ```"
  [task m]
  (stmem/set-state-working (assoc m :message "start devhub request"))
  (let [req (assoc (:json-post-header c/config) :body (che/encode task))
        url (:dev-hub-url c/config)]
    (stmem/set-val (assoc m :func :req :value task))
    (µ/log ::devhub! :message "stored task, send request" :url url :m m)
    (try
      (resp/check (http/post url req) task m)
      (catch Exception e (stmem/set-state-error (assoc m :message (.getMessage e)))))))


(comment
  (devhub! {:Action "TCP" :Port 23 :Host "localhost" :Value "Hi!"}
           {:mp-id "test" :struct :cont :no-idx 0 :par-idx 0 :seq-idx 0}))
