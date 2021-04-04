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
  ([k c]
  (when (and (string? k) (not (empty? k)))
    (nth (string/split k (:re-sep c)) 0 nil))))

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
  ([k c]
  (when (string? k) (nth (string/split k (:re-sep c)) 1 nil))))

;;------------------------------
;; key at position 2
;;------------------------------
(defn key->no-idx
  "Returns the value of the key corresponding to the given key
  `container` or `definitions` index."
  ([k]
   (key->no-idx c/config k))
  ([k c]
  (when (string? k) (nth (string/split k (:re-sep c)) 2 nil))))

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
  ([k c]
  (when (string? k) (nth (string/split k (:re-sep c)) 3 nil))))

;;------------------------------
;; key at position 4
;;------------------------------
(defn key->seq-idx
  "Returns an integer corresponding to the givens key sequential index."
  ([k]
   (key->seq-idx c/config k))
  ([k c]
   (when (string? k) (nth (string/split k (:re-sep c)) 4 nil))))

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
  ([k c]
   (when (string? k) (nth (string/split k (:re-sep c)) 5 nil))))

;;------------------------------
;; map to string
;;------------------------------
(defn pad-ok?
  "Checks if the padding of `i` is ok. `\"*\"` serves pattern matching."
  ([i]
   (pad-ok? i (:stmem-key-pad-length c/config)))
  ([i n]
   (cond
     (and (string? i)
          (= n (count i))) true
     (= i "*")             true 
     :else                 false)))

(defn ensure-int
  "Ensures `i` to be integer. Returns 0 as default."
  [i]
  (if (integer? i) i (try (Integer/parseInt i) (catch Exception e 0))))

(defn lpad
  "Left pad the given number if it is not a string."
  ([i]
   (lpad i (:stmem-key-pad-length c/config)))
  ([i n]
   (if (pad-ok? i) i (format (str "%0" n "d") (ensure-int i)))))

(defn map->task-key
  [{{t :tasks} :stmem-trans s :stmem-key-sep} {n :task-name}]
  (str n s t))

(defn map->struct-key
  [{trans :stmem-trans s :stmem-key-sep} m]
  (str (:mp-id m) s ((:struct m) trans)))

(defn map->key
  ([m]
   (map->key c/config m))
  ([config m]
   (when (and (map? m) (not (empty? m))) 
     (let [sep   (:stmem-key-sep config)
           trans (:stmem-trans config)]
       (if (:task-name m)
         (map->task-key config m)
         (when (and (:mp-id m) (:struct m))
           (str (map->struct-key config m))
           (when (:no-idx m) (str sep (:no-idx m))
                  (when (:func m) (str sep (:func m))
                        (when (:seq-idx m) (str sep (:seq-idx m))
                              (when (:par-idx m) (str sep (:par-idx m))))))))))))