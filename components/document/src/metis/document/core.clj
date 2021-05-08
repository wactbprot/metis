(ns metis.document.core
  ^{:author "wactbprot"
    :doc "Handles the documents in which the produced data is stored
          in.  This may be calibration documents but also measurement
          docs."}
  (:require [metis.config.interface :as c]
            [com.ashafa.clutch :as couch]
            [vl-data-insert.core :as insert]
            [com.brunobonacci.mulog :as µ]
            [metis.stmem.interface :as stmem]
            [clojure.string :as string]))

(defn doc->id [{id :_id}] id)

(defn doc->version
  "Returns the version of the document as an integer value:"
  [{rev :_rev}]
  (when-let [v (first (string/split rev  #"-"))] (Integer/parseInt v)))

;;------------------------------
;; extract doc info
;;------------------------------
(defn base-info
  "Returns a map with documents base info."
  [doc]
  {:doc-version (doc->version doc) :doc-id (doc->id doc)})

(defn doc-type
  "Returns the type of the document. Assumes the type of the document to
  be the first key hierarchy beside `:_id` and `:_rev`."
  [doc m]
  (first (filter #(not (or (= :_id %) (= :_rev %))) (keys doc))))

(defmulti doc-info
  "Extracts informations about a document depending on the type."
  doc-type)

(defmethod doc-info :Calibration [doc] (assoc (base-info doc) :doc-type "Calibration"))
(defmethod doc-info :Measurement [doc] (assoc (base-info doc) :doc-type "Measurement"))
(defmethod doc-info :State       [doc] (assoc (base-info doc) :doc-type "State"))
(defmethod doc-info :default     [doc] (assoc (base-info doc) :doc-type "default"))

;;------------------------------
;; add
;;------------------------------
(defn add
  "Adds a info map to the shortterm memory."
  [{mp-id :mp-id :as m} doc]
  (if-let [id (:_id doc)]
    (do
      (stmem/set-val {:mp-id mp-id :struct :id :no-idx id :value (doc-info doc)})
      (µ/log ::add :message "doc info added" :m m)
      {:ok true})
    (do
      (µ/log ::add :error "document contains no id" :m m)
      {:error "no document id"})))

;;------------------------------
;; rm
;;------------------------------
(defn rm
  "Removes the info map from the shortterm memory."
  [{mp-id :mp-id :as m} id]
  (µ/log ::rm :message "will rm doc info from st-mem" :doc-id id :m m)
  (stmem/del-val {:mp-id mp-id :struct :id :no-idx id}))

;;------------------------------
;; ids
;;------------------------------
(defn ids
  "Returns the vector of ids added.

  Example:
  ```clojure
  (add \"devs\" \"cal-2018-ce3-kk-75003_0002\")
  ;; hiob DEBUG [cmp.lt-mem:14] - try to get 
  ;; document with id: cal-2018-ce3-kk-75003_0002
  ;; OK
  (ids \"devs\")
  ;; [cal-2018-ce3-kk-75003_0002]
  ```"
  [{mp-id :mp-id :as m}]
  (mapv :no-idx (stmem/get-maps {:mp-id mp-id :struct :id :no-idx :*})))
;; --------------------------------------------------------------------------^untested v next

(comment
;;------------------------------
;; renew
;;------------------------------
(defn renew!
  "Renew the id interface with the give ids-vector `v`."
  [mpd-id v]
  (when (vector? v)
    (µ/log ::refresh :message "will refresh ids")
    (mapv (fn [id] (rm mpd-id id)) (ids mpd-id))
    (mapv (fn [id] (add mpd-id id)) v))
  {:ok true})


;;------------------------------
;; store with doc-lock
;;------------------------------
(def doc-lock (Object.))
(defn store!
  "Stores the `results` vector under the `doc-path` of every document
  loaded at the given `mp-id`. Checks if the version of each document
  is `+1`.  Returns `{:ok true}` or `{:error <problem>}`.

  Example:
  ```clojure
  (def results [{:Type \"cmp-test\" :Unit \"Pa\" :Value 1}
               {:Type \"cmp-test2\" :Unit \"Pa\" :Value 2}])

  (def doc-path  \"Calibration.Measurement.Values.Pressure\")  

  (store! \"ref\" results doc-path)
  ```"  
  [mp-id results doc-path]
  (if (and (string? mp-id) (vector? results) (string? doc-path))
    (let [ids (ids mp-id)]
      (if (empty? ids)
        {:ok true :warn "no documents loaded"}
        (let [res (map (fn [id]
                         (locking doc-lock
                           (µ/log ::store! :message "lock doc" :doc-id id)
                           (let [in-doc  (lt/get-doc id)
                                 doc     (insert/store-results in-doc results doc-path)
                                 out-doc (lt/put-doc doc)]
                             (µ/log ::store! :message "release lock" :doc-id id))))
                       ids)]
          (if-let [n-err (:error (frequencies res))]
            {:error "got " n-err " during attempt to store results"}
            {:ok true}))))
    {:ok true :warn "no doc-path or no mp-id or no results"}))
)
