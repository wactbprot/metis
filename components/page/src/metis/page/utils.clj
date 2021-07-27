(ns metis.page.utils
  (:require [clojure.string :as string]))

(def s "_")

(defn gen-state-id [{a :mp-id b :struct c :no-idx  d :seq-idx e :par-idx}]
  (string/join s [a (name b) c "state" d e ]))

(defn gen-ctrl-id [{a :mp-id b :struct c :no-idx}]
  (string/join s [a (name b) c "ctrl"]))

(defn gen-exch-id [{a :mp-id b :struct c :no-idx d :exchpath} k]
  (string/join s [a (name b) c "exch" d k]))

(defn gen-msg-elem-id [{a :mp-id b :struct c :no-idx}]
  (string/join s [a (name b) c "msg-elem"]))

(defn gen-msg-data-id [{a :mp-id b :struct c :no-idx}]
  (string/join s [a (name b) c "msg-data"]))

(defmulti task-info  #(-> % :Action keyword))

(defmethod task-info :runMp [{mp :Mp title :ContainerTitle no-idx :Container}]
  [:a {:target "_blank"
                    :href (str "/cont/" mp "?active=" (or title no-idx))}
   [:div [:b mp] ]
   [:div (or title no-idx)]])
  
(defmethod task-info :default [{descr :Comment}] [:span.uk-text-light descr])
 
(defn val-type [x]
  (cond
    (string? x) :string
    (int? x) :int
    (number? x) :float
    (boolean? x) :bool
    (nil? x) :nil))
