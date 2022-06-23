(ns metis.page.nav
  ^{:author "Thomas Bock thomas.bock@ptb.de"
    :doc "Provides navigation elements for all pages."}
  (:require [metis.page.utils :as u]))


(defn ids-list [conf data] [:ul.uk-navbar-nav {:id "doc-ids"}])

(defn mpd-descr [conf data]
  [:div.uk-navbar-item
   [:a.uk-logo
    {:href (str "http://localhost:5984/_utils/#database/vl_db/"(:mp-id data))
     :target "_blank"}
    (:mp-id data) "&nbsp;" [:sup (if (:running data)
                                   [:span.uk-badge "active"]
                                   [:span.uk-badge "stopped"])]
    [:div.uk-navbar-subtitle (:descr data)]]])

(defn links [conf data]
  [:div.uk-navbar-container
   {:uk-navbar ""}
   [:div.uk-navbar-center
     [:ul.uk-navbar-nav
      [:li [:a {:uk-icon "icon: github-alt"
                :target "_blank"
                :href "https://github.com/wactbprot/metis"}]]
      [:li [:a {:target "_blank"
                :href "http://localhost:8081/"} "redis"]]
      [:li [:a {:target "_blank"
                :href "http://a75438:5601/app/discover"} "elasticsearch"]]
      [:li [:a {:target "_blank"
                :href "http://localhost:8009/"} "devproxy"]]
      (when (:mp-id data)
        [:li [:a {:href (str "/cont/" (:mp-id data))} "Container"]])
      (when (:mp-id data)
        [:li [:a {:href (str "/elem/" (:mp-id data))} "Inputs"]])
      [:li [:a {:uk-icon "icon: list"
                :target "_blank"
                :href "/"}]]]]])

(defn mpd [conf data]
  [:div.uk-navbar-container.uk-sticky.uk-sticky-fixed.uk-sticky-below
   {:uk-sticky ""
    :uk-navbar ""}
   [:div.uk-navbar-left
    (mpd-descr conf data)]
   [:div.uk-navbar-right
    (ids-list conf data)]])
