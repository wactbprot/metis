(ns metis.worker.replicate
  ^{:author "wactbprot"
    :doc "Worker to replicate a couchdb database."}
  (:require [metis.config.interface :as c]
            [cheshire.core :as che]
            [clj-http.client :as http]
            [com.brunobonacci.mulog :as µ]
            [metis.worker.resp :as resp]
            [metis.stmem.interface :as stmem]))

(defn url
  ([]
   (url c/config))
  ([{conn :ltmem-base-url}]
   (str conn "/_replicate")))

(defn req
  ([task]
   (req c/config task))
  ([{header :json-post-header} {s :SourceDB t :TargetDB}]
   (assoc header :body (che/encode {:source s :target t}))))

(defn replicate!
  "Replicate a database (CouchDB) by posting:

  ```json
  {
    \"_id\": \"my_rep\",
    \"source\": \"http://myserver.com/foo\",
    \"target\":  \"http://user:pass@localhost:5984/bar\"
  }
  ```
  to the `/_replicate` endpoint."
  [task m]
  (try
    (resp/check (http/post (url) (req task)) task m)
    (catch Exception e (stmem/set-state-error (assoc m :message (.getMessage e))))))
