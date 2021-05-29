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

(defn gen-url [id] (str (:ltmem-conn c/config) "/" id))

(defn gen-req
  "Assoc a json version of the doc (with updated revision) as `:body`"
  [doc]
  (assoc (:json-post-header c/config) :body (che/encode (ltmem/rev-refresh doc))))

(defn gen-db-doc!
  "Generates a couchdb document from the value."
  [{doc :Value :as task} m]
  (stmem/set-state-working m)
  (let [doc-id (:_id doc)
        url    (gen-url doc-id)
        req    (gen-req doc)]
    (when-not (ltmem/exist? doc-id)
      (try
        (resp/check (http/put url req) task m)
        (µ/log ::gen-db-doc! :message "add doc id endpoint and to ltmem" :m m)
        (catch Exception e (stmem/set-state-error (assoc m :mesage (.getMessage e))))))
    (doc/add m doc-id)
    (stmem/set-state-executed (assoc m :message "add doc id endpoint"))))

(comment
  (def m {:mp-id "test" :struct :cont :no-idx 0 :par-idx 0 :seq-idx 0 :func :resp})
  (def t {:Action "genDbDoc",
          :Comment "generates a state doc for storing results",
          :TaskName "SE3_state-gen_state_doc",
          :Value
           {:_id "gen-db-doc-test",
            :State
            {:Measurement
             {:Date [{:Type "generated", :Value "2020-09-23 10:37:28"}],
              :AuxValues {},
              :Values {}}}}}))
