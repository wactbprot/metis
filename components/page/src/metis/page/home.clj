(ns metis.page.home
  ^{:author "Thomas Bock thomas.bock@ptb.de"
    :doc "Provides elements for the input page."}
  (:require [metis.page.utils :as u]
            [metis.page.nav :as nav]
            [hiccup.page :as hp]
            [clojure.string :as string]))


(defn deps-span [b] [:span {:uk-icon (if b "check" "warning")}])

(defn all-task-deps-ok? [v] (= (count v) (count (filter :available v))))

(defn all-mp-deps-ok? [v] (or (empty? v) (= (count v) (count (filter :running v)))))

(defn content [conf data]
  [:div.uk-container.uk-container-xsmall
   (into [:ul.uk-accordion {:uk-accordion "multiple: false" :duration 400}]
         (map (fn [m i]
                [:li.uk-background-muted #_(when (zero? i) {:class "uk-open"})
                 [:a.uk-accordion-title {:href "#"}
                  [:h3.uk-heading
                    (deps-span (all-task-deps-ok? (:task-deps m))) "&nbsp;"
                    (deps-span (all-mp-deps-ok? (:mp-deps m))) "&nbsp;"
                    [:span.uk-text-uppercase (:name m)]
                    [:div.uk-text-meta.uk-text-right (:mp-id m)]]
                  [:a.uk-link-text {:href (str "cont/"(:mp-id m))} [:span {:uk-icon "link"}] "&nbsp;&nbsp;" (:descr m)]]
                 [:div.uk-accordion-content
                   [:div.uk-grid
                    [:div
                     [:h3.uk-text-uppercase.uk-text-meta "task dependencies"]
                     (into [:p]
                           (mapv (fn [m]
                                  [:div (deps-span (:available m)) (str "&nbsp;&nbsp;"  (:task-name m))])
                                (:task-deps m)))]
                    [:div
                     [:h3.uk-text-uppercase.uk-text-meta "mpd dependencies"]
                     (if (empty? (:mp-deps m))
                       [:p "none"]
                       (into [:p]
                             (mapv (fn [m]
                                    [:div (deps-span (:running m)) (str "&nbsp;&nbsp;"  (:mp-id m))])
                                  (:mp-deps m))))]]]])
              data (range)))])


(defn body [conf data]
  [:body
   (nav/links conf data)
   (content conf data)
   (hp/include-js "/js/jquery.js")
   (hp/include-js "/js/uikit.js")
   (hp/include-js "/js/uikit-icons.js")])
