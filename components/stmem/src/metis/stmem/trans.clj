(ns metis.stmem.trans
  (:require [metis.config.interface :as c]
            [clojure.string :as string]))

;;------------------------------
;; key at position 0
;;------------------------------
(defn key->mp-id
  "Returns the name of the key space for the given key.

  May be:
  * `tasks`
  * `<mp-id>`"
  ([k]
   (key->mp-id c/config k))
  ([config k]
  (when (and (string? k) (not (empty? k)))
    (nth (string/split k (:re-sep config)) 0 nil))))

;;------------------------------
;; key at position 1
;;------------------------------
(defn key->struct
  "Returns the name of the `struct`ure for the given key.
  The structure is the name of the key at the second
  position. Possible values are:
  
  * `<taskname>`
  * `definitions`
  * `container`
  * `exchange`
  * `id`
  * `meta`"
  ([k]
   (key->struct c/config k))
  ([config k]
  (when (string? k) (nth (string/split k (:re-sep config)) 1 nil))))

;;------------------------------
;; key at position 2
;;------------------------------
(defn key->no-idx
  "Returns the value of the key corresponding to the given key
  `container` or `definitions` index."
  ([k]
   (key->no-idx c/config k))
  ([config k]
  (when (string? k) (nth (string/split k (:re-sep config)) 2 nil))))

;;------------------------------
;; key at position 3
;;------------------------------
(defn key->func
  "Returns the name of the `func`tion for the given key.
  Possible values are:

  * `ctrl`
  * `state`
  * `request`
  * `response`
  * `elem`
  * `decr`
  * `title`
  * `definition`
  * `messsage`"
  ([k]
   (key->func c/config k))
  ([config k]
  (when (string? k) (nth (string/split k (:re-sep config)) 3 nil))))

;;------------------------------
;; key at position 4
;;------------------------------
(defn key->seq-idx
  "Returns an integer corresponding to the givens key sequential index."
  ([k]
   (key->seq-idx c/config k))
  ([config k]
   (when (string? k) (nth (string/split k (:re-sep config)) 4 nil))))

(defn key->no-jdx
  "The 4th position at `definitions` keys."
  [k]
  (key->seq-idx k))

  (defn key->level
  "The 4th position at listener `reg` keys."
  [k]
  (key->seq-idx k))

;;------------------------------
;; key at position 5
;;------------------------------
(defn key->par-idx
  "Returns an integer corresponding to the givens key parallel index."
  ([k]
   (key->par-idx c/config k))
  ([config k]
   (when (string? k) (nth (string/split k (:re-sep config)) 5 nil))))

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

(defn map->func-part
  [{trans :stmem-trans s :stmem-key-sep} m]
  (when (:func m)
    (str s ((:func m) trans))))

(defn map->seq-par-idx-part
  [{s :stmem-key-sep :as config} m]
  (when (:seq-idx m)
    (str s (lpad config (:seq-idx m)))
    (when (:par-idx m)
      (str s (lpad config (:par-idx m))))))

(defn map->key
  ([m]
   (map->key c/config m))
  ([config m]
   (when (and (map? m) (seq m)) 
     (if (:task-name m)
         (map->task-key config m)
         (str (map->struct-part config m)
              (map->no-idx-part config m)
              (map->func-part config m)
              (map->seq-par-idx-part config m))))))