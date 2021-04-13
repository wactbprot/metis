(ns metis.stmem.trans
  (:require [metis.stmem.core :as core]
            [metis.config.interface :as c]
            [cheshire.core :as che]
            [clojure.string :as string]))

;;------------------------------
;; map to key utils
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
  (cond
    (integer? idx) idx
    (string? idx) (try (Integer/parseInt idx) (catch Exception e 0))))

(defn lpad
  "Left pad the given number if it is not a string."
  ([idx]
   (lpad c/config idx))
  ([config idx]
   (if (pad-ok? idx)
     idx
     (when idx (format (str "%0" (:stmem-key-pad-length config) "d") (ensure-int idx))))))

(defn map->task-key
  [{trans :stmem-trans s :stmem-key-sep} m]
  (str (:tasks trans) s (:task-name m)))

(defn map->struct-part
  [{trans :stmem-trans s :stmem-key-sep} m]
  (when  (:mp-id m)
    (when-let [p (:struct m)]
      (str (:mp-id m) s (if (keyword? p) (p trans) p)))))

(defn map->no-idx-part
  [{trans :stmem-trans s :stmem-key-sep :as config} m]
  (when-let[no-idx (:no-idx m)]
    (str s (if (= :* no-idx) (:* trans) (lpad config (:no-idx m))))))

(defn map->exch-part
  [{s :stmem-key-sep :as config} m]
  (when-let [p (:exch m)] (str s p)))

(defn map->metapath-part
  [{trans :stmem-trans s :stmem-key-sep} m]
  (when-let [p (:metapath m)] (str s (if (keyword? p) (p trans) p))))

(defn map->func-part
  [{trans :stmem-trans s :stmem-key-sep} m]
  (when-let [p (:func m)] (str s (if (keyword? p) (p trans) p))))

(defn map->seq-par-idx-part
  [{trans :stmem-trans s :stmem-key-sep :as config} m]
  (let [sdx (:seq-idx m)
        pdx (:par-idx m)]
    (when sdx
    (str s (if (number? sdx) (lpad config sdx) (:* trans))
         (when pdx
           (str s (if (number? pdx) (lpad config pdx) (:* trans))))))))

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

(defn get-val
  ([m]
   (get-val c/config m))
  ([config m]
   (che/parse-string (core/get-val (map->key m)) true)))

(defn set-val
  ([m]
   (set-val c/config m))
  ([{relax :stmem-mod-relax} m]
   (if-let [val (:value m)]
     (do 
       (core/set-val (map->key m) (che/generate-string val))
       (Thread/sleep relax)
       {:ok true})
     {:error "no value given"})))


(defn set-vals
  ([m]
   (set-vals c/config m))
  ([{relax :stmem-mod-relax} m]
   (if-let [val (:value m)]
     (do 
       (core/set-vals (core/pat->keys (map->key m)) (che/generate-string val))
       (Thread/sleep relax)
       {:ok true})
     {:error "no value given"})))

(defn del-val
  ([m]
   (del-val c/config m))
  ([{relax :stmem-mod-relax} m]
   (core/del-val (map->key m))
   (Thread/sleep relax)
   {:ok true}))

(defn del-vals
  ([m]
   (del-vals c/config m))
  ([config m]
   (core/del-vals (core/pat->keys (map->key m)))
   {:ok true}))
