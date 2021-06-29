(ns metis.page.core
  (:require [hiccup.form :as hf]
            [hiccup.page :as hp]
            [metis.page.utils :as u]
            [clojure.string :as string]))

(defn gen-state-btn [{mp-id :mp-id struct :struct no-idx :no-idx seq-idx :seq-idx par-idx :par-idx } s]
  [:button.uk-button.uk-button-default.state-btn
   {:data-mp-id mp-id
    :data-struct struct
    :data-no-idx no-idx
    :data-seq-idx seq-idx
    :data-par-idx par-idx
    :data-func "state"
    :data-value s}
   (condp = s
     "working" [:span {:uk-icon "icon: cog" :title "halt"}]
     "executed" [:span {:uk-icon "icon: check" :title "skip"}]
     "ready" [:span {:uk-icon "icon: play" :title "run"}])])

(defn table-row [m]
  [:tr
   [:td.uk-text-uppercase.uk-text-muted {:id (u/gen-state-id m)} (:value m)]
   [:td (gen-state-btn m "working") (gen-state-btn m "executed") (gen-state-btn m "ready")] 
    [:td (:seq-idx m)]
   [:td (:par-idx m)]
   [:td (:TaskName (:task m))]
   [:td (:Action (:task m))]
   [:td (u/task-info (:task m))]])

(defn gen-msg-ok-btn [{mp-id :mp-id struct :struct no-idx :no-idx} s]
  [:button.uk-button.uk-button-default.uk-modal-close.msg-ok-btn
   {:type "button"
    :data-mp-id mp-id
    :data-struct struct
    :data-no-idx no-idx
    :data-func "msg"
    :data-value s} s])

(defn gen-msg-modal [m]
  [:div {:id (u/gen-msg-elem-id m) :uk-modal=""}
   [:div.uk-modal-dialog.uk-modal-body
    [:h2.uk-modal-title (str "container " (:no-idx m) " message")]
    [:p {:id (u/gen-msg-data-id m)}]
     [:p.uk-text-right
      (gen-msg-ok-btn m "ok")]]])

(defn gen-ctrl-btn [{mp-id :mp-id struct :struct no-idx :no-idx} s]
  [:button.uk-button.uk-button-default.ctrl-btn
     {:data-mp-id mp-id
      :data-struct struct
      :data-no-idx no-idx
      :data-func "ctrl"
      :data-value (condp = s "cycle" "mon" s)} s])
  
(defn state-li [v]
  (let [m (first v)]
      (into [:div.uk-accordion-content
             [:p.uk-text-lighter (:descr m)]
             (gen-ctrl-btn m "run")
             (gen-ctrl-btn m "stop")
             (gen-ctrl-btn m "cycle")
             (gen-ctrl-btn m "suspend")
             (gen-ctrl-btn m "reset")
             (gen-msg-modal m)
           [:table.uk-table.uk-table-hover.uk-table-striped
            [:thead [:tr
                     [:th.uk-width-small "status"]
                     [:th "ctrls"]
                     [:th "seq-idx"]
                     [:th "par-idx"]
                     [:th "task name"]
                     [:th "task action"]
                     [:th.uk-width-medium "task info"]]]            
            (into [:tbody] (map table-row v))]])))

(defn e-input [m k v]
  (let [id (u/gen-exch-id m (name k))]
        [:div.uk-margin
         [:label.uk-form-label {:for id} k]
         [:div.uk-form-controls
          [:input.uk-input
           {:type "text"
            :id id
            :value v
            :data-mp-id (:mp-id m)
            :data-struct "exchange"
            :data-exchpath (:exchpath m)
            :data-no-idx (:no-idx m)}]]]))

(defn e-btn [m k v]
  [:button.uk-button.uk-button-default
   {:type "text"
    :data-mp-id (:mp-id m)
    :data-struct "exchange"
    :data-exchpath (:exchpath m)
    :data-no-idx (:no-idx m)}"ok"])
  
(defmulti e (fn [m k v] (keyword k)))

(defmethod e :Type [m k v] (e-input m k v))

(defmethod e :Unit [m k v] (e-input m k v))

(defmethod e :Value [m k v] (e-input m k v))

(defmethod e :SdValue [m k v] (e-input m k v))

(defmethod e :N [m k v] (e-input m k v))

(defmethod e :Ready [m k v] (e-btn m k v))

(defmethod e :default [m k v] (e-input m k "not implemented yet"))

(defn elem-card [m k v]
  [:div.uk-card.uk-card-default.uk-card-body
   [:h3.uk-card-title k]
   (let [m (assoc m :exchpath k)]
     (cond
       (and (map? v)
            (contains? v :Type)
            (contains? v :Unit)) (into [:form.uk-form-horizontal.uk-margin-large] (mapv (fn [[k v]] (e m k v)) v))
       ;; select ...
       ;; dut ...
       ;; opk ...
       (string? v) [:p v]
       (boolean? v) [:p v]
       :else (str v)))])
   
(defn elems [{es :value :as m} e]
  (into [:div.uk-accordion-content] (mapv #(elem-card m % (get e % :not-found)) es)))

(defn all-li-title [m s]
  [:a.uk-accordion-title {:href "#"}
   [:span.uk-text-capitalize (:title m)]
   [:span.uk-text-light.uk-align-right (:no-idx m)]
   [:span.uk-text-light.uk-align-right.uk-text-uppercase.uk-text-muted {:id (u/gen-ctrl-id m)} s]])
  
(defn ctrl-li-title [m] (all-li-title m (:value m)))

(defn elem-li-title [m] (all-li-title m ""))

(defn all-li [m a]
  [:li
   (if (or (= (str (:no-idx m)) (str a)) (= (:title m) a))
     {:class "uk-background-muted uk-open"}
     {:class "uk-background-muted"})])

(defn ctrl-li [m a] (into (all-li m a) [(ctrl-li-title m) (state-li (:states m))]))

(defn elem-li [m a e] (into (all-li m a) [(elem-li-title m) (elems m e) ]))

(defn ids-list []
  [:ul.uk-breadcrumb {:id "doc-ids"}])

(defn cont-content [conf data]
  [:div.uk-container.uk-container-large.uk-padding-large
   (ids-list)
   (into [:ul.uk-accordion {:uk-accordion "multiple: false"}]
         (map #(ctrl-li % (:active data)) (:data data)))])

(defn elem-content [conf data]
  [:div.uk-container.uk-container-large.uk-padding-large
   (ids-list)
   (into [:ul.uk-accordion {:uk-accordion "multiple: false"}]
         (map #(elem-li % (:active data) (:all-exch data)) (:data data) ))])

(defn head [conf data]
  [:head [:title "metis"]
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
   (hp/include-css "/css/uikit.min.css")])

(defn nav [conf data]
   [:div {:class "uk-navbar-container uk-sticky uk-sticky-fixed uk-sticky-below"
          :uk-sticky ""
          :uk-navbar ""}
    [:div.uk-navbar-center
     [:ul.uk-navbar-nav
      [:li [:a {:target "_blank" :href "http://localhost:8081/"} "redis"]]
      [:li [:a {:target "_blank" :href "http://a75438:5601/app/discover"} "elasticsearch"]]
      [:li [:a {:target "_blank" :href "http://localhost:8009/"} "devproxy"]]
      [:li [:a {:uk-icon "icon: github" :target "_blank" :href "https://github.com/wactbprot/metis"}]]
      [:li [:a {:target "_blank" :href (str "/cont/" (:mp-id data))} "Container"]]
      [:li [:a {:target "_blank" :href (str "/elem/" (:mp-id data))} "Inputs"]]]]])
  
(defn body [conf data f]
  [:body#body {:data-mp-id (:mp-id data)} (nav conf data)
   (f conf data)
   (hp/include-js "/js/jquery-3.5.1.min.js")
   (hp/include-js "/js/uikit.min.js")
   (hp/include-js "/js/uikit-icons.min.js")
   (hp/include-js "/js/ws.js")])

(defn cont [conf data] (hp/html5 (head conf data) (body conf data cont-content)))


(defn elem [conf data] (hp/html5 (head conf data) (body conf data elem-content)))
