(ns metis.page.core
  (:require [hiccup.form :as hf]
            [hiccup.page :as hp]
            [metis.page.utils :as u]
            [clojure.string :as string]))

(defn ids-list [conf data] [:ul.uk-navbar-nav {:id "doc-ids"}])

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

(defn gen-msg-btn [{mp-id :mp-id struct :struct no-idx :no-idx} s]
  [:button.uk-button.uk-button-default.uk-modal-close.msg-btn
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
                     [:th "ctrls"]
                     [:th.uk-table-shrink "seq-idx"]
                     [:th.uk-table-shrink "par-idx"]
                     [:th "task name"]
                     [:th "task action"]
                     [:th.uk-width-medium "task info"]]]            
            (into [:tbody] (map table-row v))]])))

;; ------------------------------------------------------------------------
;; exch inputs
;; ------------------------------------------------------------------------
(defn e-btn [m k v]
  [:a.uk-button.uk-button-default.exch-btn
   {:data-mp-id (:mp-id m)
    :data-struct "exch"
    :data-type :bool
    :data-exchpath (:exchpath m)
    :data-no-idx (:no-idx m)} "ok"])

(defn e-input [m k v]
  (let [id (u/gen-exch-id m (name k))]
        [:div.uk-margin
         [:label.uk-form-label {:for id} k]
         [:div.uk-form-controls
          [:input.uk-input.exch-input
           {:type "text"
            :id id
            :value v
            :data-type (u/val-type v)
            :data-mp-id (:mp-id m)
            :data-struct "exch"
            :data-exchpath (:exchpath m)
            :data-exchkey k
            :data-no-idx (:no-idx m)}]]]))
  
(defmulti e (fn [m k v] (keyword k)))

(defmethod e :Type [m k v] (e-input m k v))

(defmethod e :Unit [m k v] (e-input m k v))

(defmethod e :Value [m k v] (e-input m k v))

(defmethod e :SdValue [m k v] (e-input m k v))

(defmethod e :N [m k v] (e-input m k v))

(defmethod e :Ready [m k v] (e-btn m k v))

(defmethod e :default [m k v] (e-input m k "not implemented yet"))

(defn s [m k v]
  (let [id (u/gen-exch-id m (name k))]
    [:div.uk-margin
     (when (contains? v :Unit)
       (e m :Unit (:Unit v)))
     [:label.uk-form-label {:for id} "Select"]
     [:div.uk-form-controls
      (into [:select.uk-select.exch-select
             {:id id
              :data-mp-id (:mp-id m)
              :data-struct "exch"
              :data-exchpath (:exchpath m)
              :data-no-idx (:no-idx m)
              :data-type (u/val-type (:Selected v))}
             [:option {:value (:Selected v)} (:Selected v)]]
            (mapv (fn [m] [:option {:value (:value m)} (or (:display m) (:value m))]) (:Select v)))
     (when (contains? v :Ready)
       (e-btn m k v))]]))

(defn elem-card [m k v]
  [:div.uk-card.uk-card-default.uk-card-body
   [:h3.uk-card-title k]
   (let [m (assoc m :exchpath k)]
     (cond
       (and (map? v)
            (contains? v :Type)
            (contains? v :Unit)) (into [:form.uk-form-horizontal.uk-margin-large]
                                  (mapv (fn [[k v]] (e m k v)) v))
       (and (map? v)
            (contains? v :Selected)) [:form.uk-form-horizontal.uk-margin-large (s m k v)]
       (string? v) [:p v]
       (boolean? v) [:p v]
       :else (str v)))])
   
(defn elems [{es :value :as m} e]
  (into [:div.uk-accordion-content]
        (mapv #(elem-card m % (get e % :not-found)) es)))

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

(defn mpd-descr [conf data]
  [:div.uk-navbar-item
   [:a.uk-logo
    {:href (str "http://localhost:5984/_utils/#database/vl_db/"(:mp-id data))
     :target "_blank"}
    (:mp-id data) "&nbsp;" [:sup (if (:running data)
                                   [:span.uk-badge "active"]
                                   [:span.uk-badge "stopped"])]
    [:div.uk-navbar-subtitle (:descr data)]]])

(defn ctrl-li [m a] (into (all-li m a) [(li-title m (:value m)) (state-li (:states m))]))

(defn cont-content [conf data]
  [:div.uk-container.uk-container-large.uk-padding-large
   (into [:ul.uk-accordion {:uk-accordion "multiple: false"}]
         (map #(ctrl-li % (:active data)) (:data data)))])

(defn elem-li [m a e]
  (let [n (count (:value m))]
    (into (all-li m a)
          [(li-title m (when (pos? n)
                         (str n " input" (when (< 1 n) "s") ))) (elems m e) ])))

(defn elem-content [conf data]
  [:div.uk-container.uk-container-large.uk-padding-large
   (into [:ul.uk-accordion {:uk-accordion "multiple: false"}]
         (map #(elem-li % (:active data) (:all-exch data)) (:data data) ))])

(defn head [conf data]
  [:head [:title "metis"]
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
   (hp/include-css "/css/uikit.css")])

(defn nav-mpd [conf data]
  [:div.uk-navbar-container.uk-sticky.uk-sticky-fixed.uk-sticky-below
   {:uk-sticky ""
    :uk-navbar ""}
   [:div.uk-navbar-left
     (mpd-descr conf data)]
   [:div.uk-navbar-right
    (ids-list conf data)]])

(defn nav-links [conf data]
  [:div.uk-navbar-container
   {:uk-navbar ""}
   [:div.uk-navbar-center
     [:ul.uk-navbar-nav
      [:li [:a {:target "_blank"
                :href "http://localhost:8081/"} "redis"]]
      [:li [:a {:target "_blank"
                :href "http://a75438:5601/app/discover"} "elasticsearch"]]
      [:li [:a {:target "_blank"
                :href "http://localhost:8009/"} "devproxy"]]
      [:li [:a {:uk-icon "icon: github"
                :target "_blank"
                :href "https://github.com/wactbprot/metis"}]]
      (when (:mp-id data)
        [:li [:a {:target "_blank"
                  :href (str "/cont/" (:mp-id data))} "Container"]])
      (when (:mp-id data)
        [:li [:a {:target "_blank"
                  :href (str "/elem/" (:mp-id data))} "Inputs"]])]]])
  
(defn body [conf data f]
  [:body#body {:data-mp-id (:mp-id data)}
   (nav-links conf data)
   (nav-mpd conf data)
   (f conf data)
   (hp/include-js "/js/jquery.js")
   (hp/include-js "/js/uikit.js")
   (hp/include-js "/js/uikit-icons.js")
   (hp/include-js "/js/ws.js")])

(defn cont [conf data] (hp/html5 (head conf data) (body conf data cont-content)))

(defn elem [conf data] (hp/html5 (head conf data) (body conf data elem-content)))

;; ------------------------------------------------------------------------
;; home
;; ------------------------------------------------------------------------
(defn deps-span [b] [:span {:uk-icon (if b "check" "warning")}])

(defn home-content [conf data]
  (prn data)
  (into [:div.uk-container.uk-padding-large]
        (map (fn [m]
               [:article 
                [:h2.uk-heading-divider.uk-text-center.uk-heading
                 [:a.uk-link-muted {:href (str "cont/"(:mp-id m))}(:mp-id m)]]
                [:p.uk-text-center (:descr m)]
                [:div {:uk-grid ""}
                 [:div {:class "uk-width-2-3@m"}
                  [:h3.uk-text-uppercase.uk-text-meta	 "task dependencies"]
                  (into [:p]
                        (map (fn [m]
                               [:div (deps-span (:available m)) (str "&nbsp;&nbsp;"  (:task-name m))])
                             (:task-deps m)))]
                 [:div {:class "uk-width-auto@m"}
                  [:h3.uk-text-uppercase.uk-text-meta "mpd dependencies"]
                  (if (empty? (:mp-deps m))
                    [:p "none"] 
                    (into [:p]
                          (map (fn [m]
                                 [:div (deps-span (:running m)) (str "&nbsp;&nbsp;"  (:mp-id m))])
                               (:mp-deps m))))]]])
             data)))

(defn body-home [conf data]
  [:body
   (nav-links conf data)
   (home-content conf data)
   (hp/include-js "/js/jquery.js")
   (hp/include-js "/js/uikit.js")
   (hp/include-js "/js/uikit-icons.js")])

(defn home [conf data] (hp/html5 (head conf data) (body-home conf data)))
