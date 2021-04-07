(ns metis.stmem.trans
  (:require [metis.stmem.core :as core]
            [metis.config.interface :as c]
            [cheshire.core :as che]
            [clojure.string :as string]))

;;------------------------------
;; map to string
;;------------------------------
(defn pad-ok?
  "Checks if the padding of `i` is ok. `\"*\"` serves pattern matching."
  ([idx]
   (pad-ok? c/config idx))
  ([config idx]
   (cond
     (and (string? idx)
          (= (count idx)
             (:stmem-key-pad-length config))) true
     (= idx "*")                         true 
     :else                               false)))

(defn ensure-int
  "Ensures `i` to be integer. Returns 0 as default."
  [idx]
  (if (integer? idx)
    idx
    (try (Integer/parseInt idx) (catch Exception e 0))))

(defn lpad
  "Left pad the given number if it is not a string."
  ([idx]
   (lpad c/config idx))
  ([config idx]
   (if (pad-ok? idx)
     idx
     (format (str "%0" (:stmem-key-pad-length config) "d") (ensure-int idx)))))

(defn map->task-key
  [{trans :stmem-trans s :stmem-key-sep} m]
  (str (:tasks trans) s (:task-name m)))

(defn map->struct-part
  [{trans :stmem-trans s :stmem-key-sep} m]
  (when (and (:mp-id m) (:struct m))
    (str (:mp-id m) s ((:struct m) trans))))

(defn map->no-idx-part
  [{s :stmem-key-sep :as config} m]
  (when (:no-idx m)
    (str s (lpad config (:no-idx m)))))

(defn map->exch-part
  [{s :stmem-key-sep :as config} m]
  (when (:exch m)
    (str s (:exch m))))

(defn map->metapath-part
  [{trans :stmem-trans s :stmem-key-sep} m]
  (when (:metapath m)
    (str s ((:metapath m) trans))))

(defn map->func-part
  [{trans :stmem-trans s :stmem-key-sep} m]
  (when (:func m)
    (str s ((:func m) trans))))

(defn map->seq-par-idx-part
  [{s :stmem-key-sep :as config} m]
  (when (:seq-idx m)
    (str s (lpad config (:seq-idx m))
         (when (:par-idx m)
           (str s (lpad config (:par-idx m)))))))

(defn map->def-key
  [config m]
  (str (map->struct-part config m)
       (map->no-idx-part config m) (map->exch-part config m) (map->metapath-part config m) 
       (map->func-part config m)
       (map->seq-par-idx-part config m)))

(defn map->key
  ([m]
   (map->key c/config m))
  ([config m]
   (when (and (map? m) (seq m)) 
     (if (:task-name m)
         (map->task-key config m)
         (map->def-key config m)))))

(defn map->val
  ([m]
   (map->val c/config m))
  ([config m]
   (:value m)))

(defn set-val [m] (core/set-val (map->key m) (che/generate-string (map->val m))))

(defn get-val [m] (che/parse-string (core/get-val (map->key m)) true))

(defn del-val [m] (core/del-val (map->key m)))

(defn del-vals [m] (core/del-vals (core/pat->keys (map->key m))))