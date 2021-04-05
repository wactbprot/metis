(ns metis.stmem.api
  (:require [metis.stmem.trans :as trans]))

;; the idea is: no key operation outside stmem
;; the stmem api converts maps to keys and vice versa
;; this should keep everything outside mostly free of side effects
  
;;------------------------------
;; exchange
;;------------------------------
(defn build-exchange
  "Builds the exchange interface."
  [{mp-id :mp-id exch :Exchange}]
  (doseq [[k v] exch]
    (trans/set-val {:mp-id mp-id :struct :exch :exch (name k) :value v})))

;;------------------------------
;; definition (cont & defins)
;;------------------------------
(defn build-defin
  "Builds the definition section."
  [{defin :Definition :as m}]
  (doall
   (map-indexed
    (fn [sdx s]
      (doall
       (map-indexed
        (fn [pdx task]
          (trans/set-val (assoc m :func :defin :seq-idx sdx :par-idx pdx :value task))
          (trans/set-val (assoc m :func :state :seq-idx sdx :par-idx pdx :value "ready")))
        s)))
    defin)))

;;------------------------------
;; container
;;------------------------------
(defn build-container
  "Builds a single container."
  [{descr :Description title :Title ctrl :Ctrl elem :Element :as m}]
  (trans/set-val (assoc m :func :title :value title))
  (trans/set-val (assoc m :func :descr :value descr))
  (trans/set-val (assoc m :func :ctrl :value ctrl))
  (trans/set-val (assoc m :func :elem :value elem))
  (build-defin m))

(defn build-all-container
  "Triggers the storing of the containers."
  [{mp-id :mp-id conts :Container}]
  (doall
   (map-indexed
    (fn [idx cont] (build-container (assoc cont  :mp-id mp-id :struct :cont :no-idx idx)))
    conts)))

;;------------------------------
;; definitions
;;------------------------------
(defn build-conds
  "Stores the definitions conditions."
  [{cnds :Condition :as m}]
  (doall
   (map-indexed
    (fn [jdx cnd]
      (trans/set-val (assoc m :seq-idx jdx :func :cond :value cnd)))
    cnds)))

(defn build-definitions
  "Stores a definition given in the definition section
  (second way beside container to provide definitions).  This includes
  `DefinitionClass` and `Conditions`."
  [{cls :DefinitionClass descr :ShortDescr conds :Condition :as m}]
  (trans/set-val (assoc m :func :descr :value descr))
  (trans/set-val (assoc m :func :cls :value cls))
  (trans/set-val (assoc m :func :ctrl :value "ready"))
  (build-conds m)
  (build-defin m))

(defn build-all-definitions
  "Triggers the storing of the definition section."
  [{mp-id :mp-id defins :Definitions}]
  (doall
   (map-indexed
    (fn [idx defin] (build-definitions (assoc defin :mp-id mp-id :struct :defins :no-idx idx)))
    defins)))


;;------------------------------
;; meta
;;------------------------------
(comment
  (defn build-meta
  "Stores the meta data of an mpd:

  * standard
  * name
  * description
  * number of containers
  * number of definitions
  "
  [p {standard :Standard
      name     :Name
      descr    :Description
      cont     :Container
      defins   :Definitions}]
  (st/set-val! (stu/meta-std-key p) standard)
  (st/set-val! (stu/meta-name-key p) name)
  (st/set-val! (stu/meta-descr-key p) descr)
  (st/set-val! (stu/meta-ndefins-key p) (count defins))
  (st/set-val! (stu/meta-ncont-key p) (count cont)))
)
;;------------------------------
;; build mpd doc
;;------------------------------
(defn build
  [{mp-id :_id {conts :Container defins :Definitions exch :Exchange} :Mp}]
  (build-exchange {:mp-id mp-id :Exchange exch})
  (build-all-container {:mp-id mp-id :Container conts})
  (build-all-definitions {:mp-id mp-id :Definitions defins}))