(ns metis.page.core
  (:require [hiccup.form :as hf]
            [hiccup.page :as hp]
            [clojure.string :as string]))

(defn table-row [m]
  [:tr
   [:td (:value m)]
   [:td [:button.uk-button.uk-button-default [:span {:uk-icon "icon: cog" :title "working"}]]
    [:button.uk-button.uk-button-default [:span {:uk-icon "icon: check" :title "executed"}]]
    [:button.uk-button.uk-button-default [:span {:uk-icon "icon: play" :title "ready"}]]]
   [:td (:seq-idx m)]
   [:td (:par-idx m)]
   [:td (:TaskName (:task m))]
   [:td (:Action (:task m))]
   [:td (:Action (:task m))]])

(defn state-li [v]
  (into [:div.uk-accordion-content
         [:button.uk-button.uk-button-default "run"]
         [:button.uk-button.uk-button-default "stop"]
         [:button.uk-button.uk-button-default "cycle"]
         [:button.uk-button.uk-button-default "suspend"] 
         [:table.uk-table.uk-table-hover.uk-table-striped
          [:thead [:tr
                   [:th "status"]
                   [:th "ctrls"]
                   [:th "seq-idx"]
                   [:th "par-idx"]
                   [:th "task name"]
                   [:th "task action"]
                   [:th "task info"]]]            
          (into [:tbody] (map table-row v))]]))

(defn ctrl-li-title [m]
  [:a.uk-accordion-title {:href "#"}
   [:span.uk-text-light (:no-idx m) " / "(:value m)]
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
  [:body  (nav conf)
   (content conf data)
   (hp/include-js "/js/uikit.min.js")
   (hp/include-js "/js/ws.js")
   (hp/include-js "/js/jquery-3.5.1.min.js")
   (hp/include-js "/js/uikit-icons.min.js")])

(defn index [conf data] (hp/html5 (head conf data) (body conf data)))
