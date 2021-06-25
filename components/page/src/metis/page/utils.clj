(ns metis.page.utils
  (:require [clojure.string :as string]))

 (def s "_")

(defn gen-state-id [{a :mp-id b :struct c :no-idx  d :seq-idx e :par-idx}]
  (string/join s [a (name b) c "state" d e ]))

(defn gen-ctrl-id [{a :mp-id b :struct c :no-idx}]
  (string/join s [a (name b) c "ctrl"]))

(defn gen-msg-elem-id [{a :mp-id b :struct c :no-idx}]
  (string/join s [a (name b) c "msg-elem"]))

(defn gen-msg-data-id [{a :mp-id b :struct c :no-idx}]
  (string/join s [a (name b) c "msg-data"]))

(defmulti task-info  #(-> % :Action keyword))

(defmethod task-info :runMp [{mp :Mp title :ContainerTitle no-idx :Container}]
  [:a.uk-link-text {:target "_blank" :href (str "/cont/" mp "?active=" (or title no-idx))}
   [:span {:uk-icon "icon: link"}] mp])

(defmethod task-info :default [{descr :Comment}] [:span.uk-text-light descr])
  

