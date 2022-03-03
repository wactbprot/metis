(ns metis.ltmem.core
  (:require [metis.config.interface :as c]
            [cheshire.core :as che]
            [com.ashafa.clutch :as couch]
            [com.brunobonacci.mulog :as µ]
            [clojure.string :as string]))

(defn safe
  "Replaces all of the `@`-signs (if followed by letters 1)
  by a `%`-sign  because `:%kw` is a valid keyword but `:@kw` not
  (or at least problematic).
  
  1) There are devices annotating channels by `(@101:105)`.
  This expressions should remain as they are."
  ([m]
   (safe c/config m))
  ([{a :at-replace} m]
  (che/decode (string/replace (che/encode m) #"(@)([a-zA-Z])" (str a "$2")) true)))

;;------------------------------
;; get doc
;;------------------------------
(defn get-doc 
  "Gets a document from the db."
  ([id]
   (get-doc c/config id))
  ([{conn :ltmem-conn} id]
   (µ/log ::get-doc :message "try to get document" :doc-id id)
   (try
     (couch/get-document conn id)
     (catch Exception e (µ/log ::get-doc :error (.getMessage e) :doc-id id)))))
  
;;------------------------------
;; revision refresh
;;------------------------------
(defn rev-refresh
  "Refreshs the revision `_rev` of the document if it exist."
  ([doc]
   (rev-refresh c/config doc))
  ([conf doc]
   (if-let [db-doc (get-doc conf (:_id doc))]
     (assoc doc :_rev (:_rev db-doc))
     doc)))

;;------------------------------
;; doc exist?
;;------------------------------
(defn exist?
  "Returns `true` if a document with the `id` exists.

  TODO: HEAD request not entire doc
  
  Example:
  ```clojure
  (exist? \"foo-bar\")
  ;; =>
  ;; false
  ```"
  ([id]
   (exist? c/config id))
  ([conf id]
   (map? (get-doc conf id))))

;;------------------------------
;; put doc
;;------------------------------
(defn put-doc
  "Puts a document to the long term memory."
  ([doc]
   (put-doc c/config doc))
  ([{conn :ltmem-conn :as conf} doc]
   (µ/log ::put-doc :message "try to put document" :doc-id (:_id doc))
   (try
     (couch/put-document conn (rev-refresh conf doc))
     (catch Exception e (µ/log ::put-doc :error (.getMessage e) :doc-id (:_id doc))))))
  
;;------------------------------
;; tasks
;;------------------------------
(defn get-task
  "Returns the task with the `task-name`."
  ([task-name]
   (get-task c/config task-name))
  ([{conn :ltmem-conn design :ltmem-task-design view :ltmem-task-view} task-name]
   (try
     (-> (couch/get-view conn design view {:key task-name})
         first
         :value
         safe)
     (catch Exception e (µ/log ::get-task :error (.getMessage e))))))

;;------------------------------
;; mpds
;;------------------------------
(defn all-mpds
  "Returns all measurement program definitions from the long term
  memory."
  ([]
   (all-mpds c/config))
  ([{conn :ltmem-conn design :ltmem-mpds-design view :ltmem-mpds-view}]
   (µ/log ::all-mpds :message "get mpds from ltm")
  (try
    (mapv :value (couch/get-view conn design view))
    (catch Exception e (µ/log ::all-mpds :error (.getMessage e))))))
