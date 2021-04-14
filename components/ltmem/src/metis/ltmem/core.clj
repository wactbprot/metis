(ns metis.ltmem.core
  (:require [metis.config.interface :as c]
            [com.ashafa.clutch :as couch]
            [com.brunobonacci.mulog :as mu]
            [clojure.string :as string]))

;;------------------------------
;; get doc
;;------------------------------
(defn get-doc 
  "Gets a document from the long term memory."
  [id]
  (mu/log ::get-doc :message "try to get document" :doc-id id)
  (try
    (couch/get-document (:ltmem-conn c/config) id)
    (catch Exception e (mu/log ::get-doc :error (.getMessage e) :doc-id id))))

;;------------------------------
;; put doc
;;------------------------------
(defn put-doc
  "Puts a document to the long term memory."
  [doc]
  (mu/log ::put-doc :message "try to put document" :doc-id (:_id doc))
  (try
    (couch/put-document (:ltmem-conn c/config) doc)
    (catch Exception e (mu/log ::put-doc :error (.getMessage e) :doc-id (:_id doc)))))

;;------------------------------
;; tasks
;;------------------------------
(defn all-tasks
  "Returns all tasks."
  ([]
   (all-tasks c/config))
  ([{conn :ltmem-conn design :ltmem-task-design view :ltmem-task-view}]
   (mu/log ::all-tasks :message "get tasks from ltm")
   (try
     (map :value (couch/get-view conn design view))
     (catch Exception e (mu/log ::all-tasks :error (.getMessage e))))))

;;------------------------------
;; tasks
;;------------------------------
(defn all-mpds
  "Returns all measurement program definitions in long term memory."
  ([]
   (all-mpds c/config))
  ([{conn :ltmem-conn design :ltmem-mpds-design view :ltmem-mpds-view}]
   (mu/log ::all-mpds :message "get mpds from ltm")
  (try
    (map :value (couch/get-view conn design view))
    (catch Exception e (mu/log ::all-mpds :error (.getMessage e))))))

;;------------------------------
;; utils
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
  [id]
  (map? (get-doc id)))
  
(defn rev-refresh
  "Refreshs the revision `_rev` of the document if it exist."
  [doc]
  (if-let [db-doc (get-doc (:_id doc))]
    (assoc doc :_rev (:_rev db-doc))
    doc))

