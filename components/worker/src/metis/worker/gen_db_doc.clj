(ns metis.worker.gen-db-doc
  ^{:author "wactbprot"
    :doc "Worker to create database documents."}
  (:require [metis.config.interface :as c]
            [cheshire.core :as che]
            [metis.document.interface :as doc]
            [clj-http.client :as http]
            [metis.ltmem.interface :as ltmem]
            [com.brunobonacci.mulog  :as µ]
            [metis.worker.resp :as resp]
            [metis.stmem.interface :as stmem]))

(defn url
  ([id]
   (url c/config id))
  ([{conn :ltmem-conn} id]
   (str conn "/" id)))

(defn req
  "`assoc` a json version of the doc (with updated revision) as `:body`"
  ([doc]
   (req c/config doc))
  ([{header :json-post-header} doc]
   (assoc header :body (che/encode (ltmem/rev-refresh doc)))))
  
(defn gen-db-doc!
  "Generates a `ltmem` document from the tasks `:Value` if it dont
  exist. Adds the `document` to the `stmem` doc interface."
  [{doc :Value :as task} m]
  (stmem/set-state-working m)
  (let [doc-id (:_id doc)]
    (when-not (ltmem/exist? doc-id)
      (try
        (resp/check (http/put (url doc-id) (req doc)) task m)
        (µ/log ::gen-db-doc! :message "add doc id endpoint and to ltmem" :m m)
        (catch Exception e (stmem/set-state-error (assoc m :mesage (.getMessage e))))))
    (doc/add m doc-id)
    (stmem/set-state-executed (assoc m :message "add doc id endpoint"))))

(comment
  (def m {:mp-id "test" :struct :cont :no-idx 0 :par-idx 0 :seq-idx 0 :func :resp})
  (def t {:Action "genDbDoc" :Value {:_id "gen-db-doc-test"}})
  (gen-db-doc! t m))
