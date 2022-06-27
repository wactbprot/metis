(ns metis.page.elements
  ^{:author "Thomas Bock thomas.bock@ptb.de"
    :doc "Provides elements for the input page."}
  (:require [metis.page.utils :as u]
            [clojure.string :as string]))


(defn card [m k {:keys [Unit Select] :as v}]
  [:div.uk-card.uk-card-default.uk-card-body
   [:h3.uk-card-title k]
   (let [m (assoc m :exchpath k)]
     (cond
       ;; order is important: select element
       ;; may also have a Unit entry
       (and (map? v)
            Select)  [:form.uk-form-horizontal.uk-margin-large
                      (u/select m k v)]
       (and (map? v)
            Unit)    (into [:form.uk-form-horizontal.uk-margin-large]
                           (mapv (fn [[k v]] (u/element m k v)) v))

       (string? v)   [:p v]
       (boolean? v)  [:p v]
       :else         (str v)))])

(defn elems [{es :value :as m} e]
  (into [:div.uk-accordion-content]
        (mapv #(card m % (get e % :not-found)) es)))

(defn li [{:keys [value] :as m} a e]
  (let [n (count value)]
    (into (u/li-all m a)
          [(u/li-title m (when (pos? n)
                           (str n " input" (when (< 1 n) "s"))))
           (elems m e) ])))

(defn content [conf data]
  [:div.uk-container.uk-container-large.uk-padding-large
   (into [:ul.uk-accordion {:uk-accordion "multiple: false"}]
         (map #(li % (:active data) (:all-exch data)) (:data data) ))])
