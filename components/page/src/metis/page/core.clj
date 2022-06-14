(ns metis.page.core
  (:require [hiccup.form :as hf]
            [hiccup.page :as hp]
            [metis.page.utils :as u]
            [metis.page.nav :as nav]
            [metis.page.home :as home]
            [metis.page.head :as head]
            [metis.page.elements :as elem]
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
   [:td (u/task-info (:task m))]
   [:td [:a {:href "#" :uk-totop "" :uk-scroll ""}]]])

(defn gen-msg-btn [{mp-id :mp-id struct :struct no-idx :no-idx} s]
  [:button.uk-button.uk-button-default.uk-modal-close.msg-btn
   {:type "button"
    :data-mp-id mp-id
    :data-struct struct
    :data-no-idx no-idx
    :data-func "msg"
    :data-value s} s])

(defn gen-msg-modal [m]
  [:div.uk-flex-top {:id (u/gen-msg-elem-id m) :uk-modal=""}
   [:div.uk-modal-dialog.uk-modal-body.uk-margin-auto-vertical
    [:h3.uk-modal-title (str "container " (:no-idx m) " message")]
    [:p {:id (u/gen-msg-data-id m)}]
     [:p.uk-text-right
      (gen-msg-btn m "ok")]]])

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
             (gen-ctrl-btn m "cycle")
             (gen-ctrl-btn m "suspend")
             (gen-ctrl-btn m "reset")
             (gen-msg-modal m)
           [:table.uk-table.uk-table-hover.uk-table-striped
            [:thead [:tr
                     [:th.uk-width-small "status"]
                     [:th.uk-width-medium "ctrls"]
                     [:th.uk-table-shrink "seq-idx"]
                     [:th.uk-table-shrink "par-idx"]
                     [:th.uk-width-small "task name"]
                     [:th "task action"]
                     [:th.uk-width-medium "task info"]
                     [:th]]]
            (into [:tbody] (map table-row v))]])))

(defn elems [{es :value :as m} e]
  (into [:div.uk-accordion-content]
        (mapv #(elem/card m % (get e % :not-found)) es)))

(defn li-title [m s]
  [:a.uk-accordion-title {:href "#"}
   [:span.uk-text-capitalize (:title m)]
   [:span.uk-text-light.uk-align-right (:no-idx m)]
   [:span.uk-text-light.uk-align-right.uk-text-uppercase.uk-text-muted
    {:id (u/gen-ctrl-id m)} s]])

(defn all-li [m a]
  [:li
   (when (or (= (str (:no-idx m)) (str a)) (= (:title m) a))
     {:class "uk-open"})])

(defn ctrl-li [m a] (into (all-li m a) [(li-title m (:value m)) (state-li (:states m))]))

(defn cont-content [conf data]
  [:div.uk-container.uk-container-large.uk-padding-large
   (into [:ul.uk-accordion {:uk-accordion "multiple: false"}]
         (map #(ctrl-li % (:active data)) (:data data)))])

(defn elem-li [m a e]
  (let [n (count (:value m))]
    (into (all-li m a)
          [(li-title m (when (pos? n)
                         (str n " input" (when (< 1 n) "s"))))
           (elems m e) ])))

(defn elem-content [conf data]
  [:div.uk-container.uk-container-large.uk-padding-large
   (into [:ul.uk-accordion {:uk-accordion "multiple: false"}]
         (map #(elem-li % (:active data) (:all-exch data)) (:data data) ))])

(defn body [conf data f]
  [:body#body {:data-mp-id (:mp-id data)}
   (nav/links conf data)
   (nav/mpd conf data)
   (f conf data)
   (hp/include-js "/js/jquery.js")
   (hp/include-js "/js/uikit.js")
   (hp/include-js "/js/uikit-icons.js")
   (hp/include-js "/js/ws.js")])

(defn cont [conf data] (hp/html5 (head/head conf data) (body conf data cont-content)))

(defn elem [conf data] (hp/html5 (head/head conf data) (body conf data elem-content)))

(defn home [conf data] (hp/html5 (head/head conf data) (home/body conf data)))
