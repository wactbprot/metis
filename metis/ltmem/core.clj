✔ (ns metis.ltmem.core
?   (:require [metis.config.interface :as c]
?             [com.ashafa.clutch :as couch]
?             [com.brunobonacci.mulog :as mu]
?             [clojure.string :as string]))
  
? ;;------------------------------
? ;; get doc
? ;;------------------------------
✔ (defn get-doc 
?   "Gets a document from the db."
?   ([id]
✘    (get-doc c/config id))
?   ([{conn :ltmem-conn} id]
✔    (mu/log ::get-doc :message "try to get document" :doc-id id)
✔    (try
✔      (couch/get-document conn id)
✘      (catch Exception e (mu/log ::get-doc :error (.getMessage e) :doc-id id)))))
?   
? ;;------------------------------
? ;; put doc
? ;;------------------------------
✔ (defn put-doc
?   "Puts a document to the long term memory."
?   ([doc]
✘    (put-doc c/config doc))
?   ([{conn :ltmem-conn} doc]
✔    (mu/log ::put-doc :message "try to put document" :doc-id (:_id doc))
✔    (try
✔      (couch/put-document conn doc)
✘      (catch Exception e (mu/log ::put-doc :error (.getMessage e) :doc-id (:_id doc))))))
?   
? ;;------------------------------
? ;; tasks
? ;;------------------------------
✔ (defn all-tasks
?   "Returns all tasks."
?   ([]
✘    (all-tasks c/config))
?   ([{conn :ltmem-conn design :ltmem-task-design view :ltmem-task-view}]
✘    (mu/log ::all-tasks :message "get tasks from ltm")
✘    (try
✘      (mapv :value (couch/get-view conn design view))
✘      (catch Exception e (mu/log ::all-tasks :error (.getMessage e))))))
  
? ;;------------------------------
? ;; mpds
? ;;------------------------------
✔ (defn all-mpds
?   "Returns all measurement program definitions from the long term
?   memory."
?   ([]
✘    (all-mpds c/config))
?   ([{conn :ltmem-conn design :ltmem-mpds-design view :ltmem-mpds-view}]
✘    (mu/log ::all-mpds :message "get mpds from ltm")
✘   (try
✘     (mapv :value (couch/get-view conn design view))
✘     (catch Exception e (mu/log ::all-mpds :error (.getMessage e))))))
  
? ;;------------------------------
? ;; utils
? ;;------------------------------
✔ (defn exist?
?   "Returns `true` if a document with the `id` exists.
  
?   TODO: HEAD request not entire doc
?   
?   Example:
?   ```clojure
?   (exist? \"foo-bar\")
?   ;; =>
?   ;; false
?   ```"
?   ([id]
✘    (exist? c/config id))
?   ([conf id]
✘    (map? (get-doc conf id))))
?   
✔ (defn rev-refresh
?   "Refreshs the revision `_rev` of the document if it exist."
?   ([doc]
✘    (rev-refresh c/config doc))
?   ([conf doc]
✘    (if-let [db-doc (get-doc conf (:_id doc))]
✘      (assoc doc :_rev (:_rev db-doc))
✘      doc)))
  
  
