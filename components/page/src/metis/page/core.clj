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
     "working" [:span {:uk-icon "icon: cog" :title s}]
     "executed" [:span {:uk-icon "icon: check" :title s}]
     "ready" [:span {:uk-icon "icon: play" :title s}])])

(defn table-row [m]
  [:tr
   [:td {:id (u/gen-state-id m)} (:value m)]
   [:td (gen-state-btn m "working") (gen-state-btn m "executed") (gen-state-btn m "ready")] 
    [:td (:seq-idx m)]
   [:td (:par-idx m)]
   [:td (:TaskName (:task m))]
   [:td (:Action (:task m))]
   [:td (:Action (:task m))]])

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
                     [:th "task info"]]]            
            (into [:tbody] (map table-row v))]])))
  
(defn ctrl-li-title [m]
  [:a.uk-accordion-title {:href "#"}
   [:span.uk-text-light (:no-idx m) " / "] [:span.uk-text-light.uk-width-expand {:id (u/gen-ctrl-id m)} (:value m)]
   [:span.uk-align-right (:descr m)]])

(defn ctrl-li [m] [:li (ctrl-li-title m) (state-li (:states m))])
  
(defn content [conf data]
  [:div.uk-container.uk-container-large.uk-padding-large
   (into [:ul.uk-accordion {:uk-accordion "multiple: false"}] (map (fn [m] (ctrl-li m)) (:data data)))])

(defn head [conf data]
  [:head [:title "metis"]
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
   (hp/include-css "/css/uikit.min.css")])

(defn nav [conf]
  [:div {:class "uk-navbar-container uk-sticky uk-sticky-fixed uk-sticky-below"
         :uk-sticky "media: 960"}
   [:nav.uk-navbar-container
    [:div.uk-navbar-left
     [:ul.uk-navbar-nav
      #_[:li [:a.uk-navbar-item.uk-logo {:href ""} [:img {:src"/img/logo.png"}]]]
      [:li [:a {:href ""} "redis"]]
      [:li [:a {:href ""} "elasticsearch"]]
      [:li [:a {:href ""} "devproxy"]]]]]])

(defn body [conf data]
  [:body#body {:data-mp-id (:mp-id data)} (nav conf)
   (content conf data)
   (hp/include-js "/js/jquery-3.5.1.min.js")
   (hp/include-js "/js/uikit.min.js")
   (hp/include-js "/js/uikit-icons.min.js")
   (hp/include-js "/js/ws.js")])

(defn index [conf data] (hp/html5 (head conf data) (body conf data)))
