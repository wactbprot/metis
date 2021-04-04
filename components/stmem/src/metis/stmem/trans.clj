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
  [k]
  (when (and (string? k) (not (empty? k)))
    (nth (string/split k re-sep) 0 nil)))

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
  * `meta`
  "
  [k]
  (when (string? k) (nth (string/split k re-sep) 1 nil)))

;;------------------------------
;; key at position 2
;;------------------------------
(defn key->no-idx
  "Returns the value of the key corresponding to the given key
  `container` or `definitions` index."
  [k]
  (when (string? k) (nth (string/split k re-sep) 2 nil)))

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
  [k]
  (when (string? k) (nth (string/split k re-sep) 3 nil)))

;;------------------------------
;; key at position 4
;;------------------------------
(defn key->seq-idx
  "Returns an integer corresponding to the givens key sequential index."
  [k]
  (when (string? k) (nth (string/split k re-sep) 4 nil)))

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
  [k]
  (when (string? k) (nth (string/split k re-sep) 5 nil)))

(defn map->key
  ([m]
   (map->key c/config m))
  ([config m]
   (when (and (map? m) (not (empty m))) 
     (let [s (:stmem-key-sep config)]
       (str (if (:task-name m) "tasks" (:mp-id m)) s
            (when (:struct m)
              (str s (:struct m))
              (when (:no-idx m)
                (str s (:no-idx m))
                (when (:func m)
                  (str s (:func m))
                  (when (:seq-idx m)
                    (str s (:seq-idx m))
                    (when (:par-idx m)
                      (str s (:par-idx m))))))))))))