(ns metis.page.container
  ^{:author "Thomas Bock thomas.bock@ptb.de"
    :doc "Provides elements for the container page."}
  (:require [metis.page.utils :as u]
            [clojure.string :as string]))

(defn gen-state-btn [{:keys [mp-id struct no-idx seq-idx par-idx]} s]
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

(defn gen-msg-btn [{:keys [mp-id struct no-idx]} s]
  [:button.uk-button.uk-button-default.uk-modal-close.msg-btn
   {:type "button"
    :data-mp-id mp-id
    :data-struct struct
    :data-no-idx no-idx
    :data-func "msg"
    :data-value s} s])

(defn gen-msg-modal [m]
  [:div.uk-flex-top {:id (u/gen-msg-elem-id m) :uk-modal ""}
   [:div.uk-modal-dialog.uk-modal-body.uk-margin-auto-vertical
    [:h3.uk-modal-title (str "container " (:no-idx m) " message")]
    [:p {:id (u/gen-msg-data-id m)}]
     [:p.uk-text-right
      (gen-msg-btn m "ok")]]])

(defn gen-ctrl-btn [{:keys [mp-id struct no-idx]} s]
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

(defn li-ctrl [{:keys [value states] :as m} a]
  (into (u/li-all m a)
        [(u/li-title m value)
         (state-li states)]))

(defn content [conf {:keys [active data] }]
  [:div.uk-container.uk-container-large.uk-padding-large
   (into [:ul.uk-accordion {:uk-accordion "multiple: false"}]
         (map #(li-ctrl % data) data))])
