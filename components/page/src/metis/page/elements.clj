(ns metis.page.elements
  ^{:author "Thomas Bock thomas.bock@ptb.de"
    :doc "Provides elements for the input page."}
  (:require [metis.page.utils :as u]
            [clojure.string :as string]))



;; ------------------------------------------------------------------------
;; exch inputs
;; ------------------------------------------------------------------------
(defn btn [{:keys [mp-id no-idx exchpath]} k v]
  [:a.uk-button.uk-button-default.exch-btn
   {:data-mp-id mp-id
    :data-struct "exch"
    :data-type :bool
    :data-exchpath exchpath
    :data-no-idx no-idx} "ok"])

(defn input [{:keys [mp-id no-idx exchpath] :as m} k v]
  (let [id (u/gen-exch-id m (name k))]
        [:div.uk-margin
         [:label.uk-form-label {:for id} k]
         [:div.uk-form-controls
          [:input.uk-input.exch-input
           {:type "text"
            :id id
            :value v
            :data-type (u/val-type v)
            :data-mp-id mp-id
            :data-struct "exch"
            :data-exchpath exchpath
            :data-exchkey k
            :data-no-idx no-idx}]]]))

(defmulti element (fn [m k v] (keyword k)))

(defmethod element :Type [m k v] (input m k v))

(defmethod element :Position [m k v] (input m k v))

(defmethod element :Mode [m k v] (input m k v))

(defmethod element :Unit [m k v] (input m k v))

(defmethod element :Value [m k v] (input m k v))

(defmethod element :SdValue [m k v] (input m k v))

(defmethod element :N [m k v] (input m k v))

(defmethod element :Ready [m k v] (btn m k v))

(defmethod element :default [m k v]
  [:div.uk-margin
   [:label.uk-form-label k]
   [:div.uk-form-controls
    [:span v]]])



(defn select [{:keys [mp-id no-idx exchpath] :as m} k {:keys [Unit Selected Select Ready] :as v}]
  (let [id (u/gen-exch-id m (name k))]
    [:div.uk-margin
     (when Unit (element m :Unit Unit))
     [:label.uk-form-label {:for id} "Select"]
     [:div.uk-form-controls
      (into [:select.uk-select.exch-select
             {:id id
              :data-mp-id mp-id
              :data-struct "exch"
              :data-exchpath exchpath
              :data-no-idx no-idx
              :data-type (u/val-type Selected)}
             [:option {:value Selected} Selected]]
            (mapv (fn [{:keys [value display]}]
                    [:option {:value value} (or display value)])
                  Select))
      (when Ready (btn m k v))]]))

(defn card [m k {:keys [Unit Selected]:as v}]
  [:div.uk-card.uk-card-default.uk-card-body
   [:h3.uk-card-title k]
   (let [m (assoc m :exchpath k)]
     (cond
       (and (map? v) Unit) (into [:form.uk-form-horizontal.uk-margin-large]
                                       (mapv (fn [[k v]] (element m k v)) v))
       (and (map? v) Selected) [:form.uk-form-horizontal.uk-margin-large (select m k v)]
       (string? v) [:p v]
       (boolean? v) [:p v]
       :else (str v)))])


(defn elems [{es :value :as m} e]
  (into [:div.uk-accordion-content]
        (mapv #(card m % (get e % :not-found)) es)))

(defn li [m a e]
  (let [n (count (:value m))]
    (into (u/li-all m a)
          [(u/li-title m (when (pos? n)
                         (str n " input" (when (< 1 n) "s"))))
           (elems m e) ])))

(defn content [conf data]
  [:div.uk-container.uk-container-large.uk-padding-large
   (into [:ul.uk-accordion {:uk-accordion "multiple: false"}]
         (map #(li % (:active data) (:all-exch data)) (:data data) ))])
