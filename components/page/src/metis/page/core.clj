(ns metis.page.core
  (:require [hiccup.form :as hf]
            [hiccup.page :as hp]
            [clojure.string :as string]))

(defn nav [conf]
  [:div {:uk-sticky "media: 960" :style "width:1628px"}
   [:nav.uk-navbar-container
    [:div.uk-navbar-left
     [:ul.uk-navbar-nav
      [:li [:a {:href ""} "redis"]]
      [:li [:a {:href ""} "elasticsearch"]]
      [:li [:a {:href ""} "devproxy"]]]]]])

(defn content [conf data]
  [:div.uk-container.uk-container-large
   [:ul.uk-accordion {:uk-accordion ""}
    [:li.uk-open
     [:a.uk-accordion-title {:href "#"} "ctrl 1"]
     [:div.uk-accordion-content "state 3" ]]
    [:li
     [:a.uk-accordion-title {:href "#"} "ctrl 2"]
     [:div.uk-accordion-content "state 3" ]]
    [:li
     [:a.uk-accordion-title {:href "#"} "ctrl 3"]
     [:div.uk-accordion-content "state 3" ]]
    
    ]
   ])


  (defn index
    [conf req data]
    (hp/html5 [:head
             [:title "metis"]
             [:meta {:charset "utf-8"}]
             [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
             (hp/include-css "css/uikit.min.css")]
            [:body
             (nav conf)
             (content conf data)
             (hp/include-js "js/uikit.min.js")
             (hp/include-js "js/uikit-icons.min.js")]))
