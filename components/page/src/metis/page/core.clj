(ns metis.page.core
  (:require [hiccup.form :as hf]
            [hiccup.page :as hp]
            [clojure.string :as string]))

(defn index
  [{conf :ui} req]
  (hp/html5 [:head
             [:title "metis"]
             [:meta {:charset "utf-8"}]
             [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
             (hp/include-css "css/uikit.min.css")]
            [:body
             [:div {:uk-sticky "media: 960" :style "width:1628px"}
               [:nav.uk-navbar-container
                [:div.uk-navbar-left
                 [:ul.uk-navbar-nav
                  [:li [:a {:href ""} "redis"]]
                  [:li [:a {:href ""} "elasticsearch"]]
                  [:li [:a {:href ""} "devproxy"]]]]]]
             
             (hp/include-js "js/uikit.min.js")
             (hp/include-js "js/uikit-icons.min.js")]))
