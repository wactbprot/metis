(ns metis.stmem.trans
  (:require [metis.stmem.core :as core]
            [metis.config.interface :as c]
            [cheshire.core :as che]
            [clojure.string :as string]
            [metis.utils.interface :as utils]))

;;------------------------------
;; map to key utils
;;------------------------------
(defn pad-ok?
  "Checks if the padding of `i` is ok. `\"*\"` serves pattern matching."
  ([idx] (pad-ok? c/config idx))
  ([{n :stmem-key-pad-length} idx]
   (cond
     (and (string? idx)
          (= n (count idx))) true
     (= idx "*") true
     :else false)))

(defn lpad
  "Left pad the given number if it is not a string."
  ([idx] (lpad c/config idx))
  ([{n :stmem-key-pad-length} idx]
   (if (pad-ok? idx)
     idx
     (when idx
       (format (str "%0" n "d") (utils/ensure-int idx))))))

;;------------------------------
;; default  keys
;;------------------------------
(defn map->struct-part
  [{trans :stmem-trans s :stmem-key-sep} {mp-id :mp-id p :struct}]
  (when mp-id
    (when p
      (str (if (keyword? mp-id) (mp-id trans) mp-id) s (if (keyword? p) (p trans) p)))))

(defn map->no-idx-part [{trans :stmem-trans s :stmem-key-sep :as config} m]
  (when-let [no-idx (:no-idx m)]
    (str s (if (= :* no-idx) (:* trans) (lpad config (:no-idx m))))))

(defn map->func-part [{trans :stmem-trans s :stmem-key-sep} m]
  (when-let [p (:func m)] (str s (if (keyword? p) (p trans) p))))

(defn map->seq-par-idx-part [{trans :stmem-trans s :stmem-key-sep :as config} m]
  (let [sdx (:seq-idx m)
        pdx (:par-idx m)]
    (when sdx
    (str s (if (number? sdx) (lpad config sdx) (:* trans))
         (when pdx
           (str s (if (number? pdx) (lpad config pdx) (:* trans))))))))

(defn map->def-key [config m]
  (str
   (map->struct-part config m)
   (map->no-idx-part config m)
   (map->func-part config m)
   (map->seq-par-idx-part config m)))

;;------------------------------
;; msg keys
;;------------------------------
(defn map->msg-key [config m]
  (str
   (map->struct-part config m)
   (map->no-idx-part config m)
   (map->func-part config m)))

;;------------------------------
;; exchange keys
;;------------------------------
(defn map->exch-part [{trans :stmem-trans s :stmem-key-sep :as config} m]
  (when-let [p (:exchpath m)]
    (str s (if (keyword? p) (p trans) p) )))

(defn map->exch-key [config m]
  (str
   (map->struct-part config m)
   (map->exch-part config m)))

;;------------------------------
;; document id keys
;;------------------------------
(defn map->doc-id-part [{trans :stmem-trans s :stmem-key-sep :as config} m]
  (when-let [p (:doc-id m)] (str s (if (keyword? p) (p trans) p))))

(defn map->id-key [config m]
  (str
   (map->struct-part config m)
   (map->doc-id-part config m)))

;;------------------------------
;; meta keys
;;------------------------------
(defn map->metapath-part [{trans :stmem-trans s :stmem-key-sep} m]
  (when-let [p (:metapath m)] (str s (if (keyword? p) (p trans) p))))

(defn map->meta-key [config m]
  (str
   (map->struct-part config m)
   (map->metapath-part config m)))

;;------------------------------
;; find key type
;;------------------------------
(defn m->key-type [m]
  (cond
    (= (:struct m) :id) :id-key
    (= (:struct m) :meta) :meta-key
    (= (:struct m) :exch) :exch-key
    (= (:func m) :msg) :msg-key
    :default :default))

(defn map->key
  ([m]
   (map->key c/config m))
  ([config m]
   (when (and (map? m) (seq m))
     (condp = (m->key-type m)
       :exch-key (map->exch-key config m)
       :id-key   (map->id-key config m)
       :meta-key (map->meta-key config m)
       :msg-key  (map->msg-key config m)
       :default  (map->def-key config m)))))

;;------------------------------
;; key to map  utils
;;------------------------------
(defn care-no-idx [{:keys [stmem-retrans] :as config} v]
  (when-let [s (get v 2)]
    (cond
      (re-matches #"[0-9]*" s) (utils/ensure-int s)
      :default s)))

(defn care-struct [{:keys [stmem-retrans]} v] (get stmem-retrans (get v 1)))

(defn key->map
  ([k] (key->map c/config k))
  ([{:keys [re-sep stmem-retrans] :as config} k]
   (when k
     (let [v (string/split k re-sep)
           [mp-id struct no-idx func seq-idx par-idx] v]
       (cond-> {:mp-id mp-id}
         struct (assoc :struct (care-struct config v))
         no-idx (assoc (if (= struct "exchange") :exchpath :no-idx) (care-no-idx config v))
         func (assoc :func (get stmem-retrans func))
         seq-idx (assoc :seq-idx  (utils/ensure-int seq-idx))
         par-idx (assoc :par-idx (utils/ensure-int par-idx)))))))
