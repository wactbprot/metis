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

(defmethod task-info :select [{cls :DefinitionClass descr :Comment}]
  [:span.uk-text-light
   [:div descr]
   [:div [:i cls]]])

(defmethod task-info :TCP [{port :Port value :Value descr :Comment}]
  [:span.uk-text-light
   [:div descr]
   [:div port]
   [:div [:i value]]])

(defmethod task-info :VXI11 [{device :Device value :Value descr :Comment}]
  [:span.uk-text-light
   [:div descr]
   [:div device]
   [:div [:i value]]])

(defmethod task-info :default [{descr :Comment}] [:span.uk-text-light descr])
 
(defn val-type [x]
  (cond
    (string? x) :string
    (int? x) :int
    (number? x) :float
    (boolean? x) :bool
    (nil? x) :nil))

(defn li-title [{:keys [title no-idx] :as m} s]
  [:a.uk-accordion-title {:href "#"}
   [:span.uk-text-capitalize title]
   [:span.uk-text-light.uk-align-right no-idx]
   [:span.uk-text-light.uk-align-right.uk-text-uppercase.uk-text-muted
    {:id (gen-ctrl-id m)} s]])

(defn li-all [m a]
  [:li
   (when (or (= (str (:no-idx m)) (str a)) (= (:title m) a))
     {:class "uk-open"})])
