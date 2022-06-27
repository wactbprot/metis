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
   (when (or #_(= (str (:no-idx m)) (str a))
             (= (:title m) a))
     {:class "uk-open"})])


;; ------------------------------------------------------------------------
;; input, button select,... elements
;; ------------------------------------------------------------------------
(defn input
  "Generates an html input field from the given params.

  ```clojure

  (def k :Unit)
  (def v \"Pa\") 
  (def m {:mp-id \"mpd-ref\",
          :struct :cont,
          :no-idx 1,
          :func :elem,
          :value [\"Target_pressure\"],
          :title \"container with single task\",
          :descr \"Container with one task only\",
          :exchpath \"Target_pressure\"}
  (input m k v)
  ;; =>
  ;; [:div.uk-margin
  ;;  [:label.uk-form-label
  ;;   {:for \"mpd-ref_cont_1_exch_Target_pressure_Unit\"}
  ;;   :Unit]
  ;;  [:div.uk-form-controls
  ;;   [:input.uk-input.exch-input
  ;;    {:data-mp-id \"mpd-ref\",
  ;;     :data-type :string,
  ;;     :data-exchpath \"Target_pressure\",
  ;;     :value \"Pa\",
  ;;     :data-exchkey :Unit,
  ;;     :type \"text\",
  ;;     :data-no-idx 1,
  ;;     :id \"mpd-ref_cont_1_exch_Target_pressure_Unit\",
  ;;     :data-struct \"exch\"}]]]
  ```"
  [{:keys [mp-id no-idx exchpath] :as m} k v]
  (let [id (gen-exch-id m (name k))]
    [:div.uk-margin
     [:label.uk-form-label {:for id} k]
     [:div.uk-form-controls
      [:input.uk-input.exch-input
       {:type "text"
        :id id
        :value v
        :data-type (val-type v)
        :data-mp-id mp-id
        :data-struct "exch"
        :data-exchpath exchpath
        :data-exchkey k
        :data-no-idx no-idx}]]]))

(defn btn [{:keys [mp-id no-idx exchpath]} k v]
  [:a.uk-button.uk-button-default.exch-btn
   {:data-mp-id mp-id
    :data-struct "exch"
    :data-type :bool
    :data-exchpath exchpath
    :data-no-idx no-idx} "ok"])

(defmulti element (fn [m k v] (keyword k)))

(defmethod element :Type [m k v] (input m k v))

(defmethod element :Position [m k v] (input m k v))

(defmethod element :Mode [m k v] (input m k v))

(defmethod element :Unit [m k v] (input m k v))

(defmethod element :Value [m k v] (input m k v))

(defmethod element :SdValue [m k v] (input m k v))

(defmethod element :N [m k v] (input m k v))

(defmethod element :Ready [m k v] (btn m k v))

(defmethod element :default [m k v]
  [:div.uk-margin
   [:label.uk-form-label k]
   [:div.uk-form-controls
    [:span v]]])

(defn select [{:keys [mp-id no-idx exchpath] :as m} k
              {:keys [Unit Selected Select Ready] :as v}]
  (let [id (gen-exch-id m (name k))]
    [:div.uk-margin
     (when Unit (element m :Unit Unit))
     [:label.uk-form-label {:for id} "Select"]
     [:div.uk-form-controls
      (into [:select.uk-select.exch-select
             {:id id
              :data-mp-id mp-id
              :data-struct "exch"
              :data-exchpath exchpath
              :data-no-idx no-idx
              :data-type (val-type Selected)}
             [:option {:value Selected} Selected]]
            (mapv (fn [{:keys [value display]}]
                    [:option {:value value} (or display value)])
                  Select))
      (when Ready (btn m k v))]]))
