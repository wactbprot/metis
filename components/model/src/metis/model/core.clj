(ns metis.model.core
  (:require [metis.config.interface :as c]
            [cheshire.core :as che]
            [metis.stmem.interface :as stmem]
            [clojure.string :as string]
            [metis.utils.interface :as utils]))

(defn safe
  "Replaces all of the `@`-signs (if followed by letters 1)
  by a `%`-sign  because `:%kw` is a valid keyword but `:@kw` not
  (or at least problematic).
  
  1) There are devices annotating channels by `(@101:105)`.
  This expressions should remain as they are."
  ([m]
   (safe c/config m))
  ([{a :at-replace} m]
  (che/decode (string/replace (che/encode m) #"(@)([a-zA-Z])" (str a "$2")) true)))

;;------------------------------
;; exchange
;;------------------------------
(defn build-exchange
  "Builds the exchange interface."
  [{mp-id :mp-id exch :Exchange}]  
  (doseq [[k v] exch]
    (stmem/set-val {:mp-id mp-id :struct :exch :exchpath (name k) :value v})))

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
          (stmem/set-val (assoc m :func :defin :seq-idx sdx :par-idx pdx :value task))
          (stmem/set-val (assoc m :func :state :seq-idx sdx :par-idx pdx :value "ready")))
        s)))
    defin)))

;;------------------------------
;; container
;;------------------------------
(defn build-container
  "Builds a single container."
  [{descr :Description title :Title ctrl :Ctrl elem :Element :as m}]
  (stmem/set-val (assoc m :func :title :value title))
  (stmem/set-val (assoc m :func :descr :value descr))
  (stmem/set-val (assoc m :func :ctrl :value ctrl))
  (stmem/set-val (assoc m :func :elem :value (if (seq elem) elem [])))
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
      (stmem/set-val (assoc m :seq-idx jdx :func :cond :value cnd)))
    cnds)))

(defn build-definitions
  "Stores a definition given in the definition section
  (second way beside container to provide definitions).  This includes
  `DefinitionClass` and `Conditions`."
  [{cls :DefinitionClass descr :ShortDescr conds :Condition :as m}]
  (stmem/set-val (assoc m :func :descr :value descr))
  (stmem/set-val (assoc m :func :cls :value cls))
  (stmem/set-val (assoc m :func :ctrl :value "ready"))
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
(defn proto-tasks [cont defins] (flatten (into (map :Definition cont) (map :Definition defins))))

(defn mp-deps 
  "Filters all `Common-run_mp` tasks from containers and definitions section.
  Returns a distinct list of `:%mpdef`initions."
  [cont defins]
  (let [v (proto-tasks cont defins)
        l (filter #(= (:TaskName %) "Common-run_mp") v)]
    (distinct (map #(get-in % [:Replace :%mpdef]) l))))

(defn task-deps
  "returns a distinct list of all `Tasks` needed."
  [cont defins]
  (distinct (map :TaskName (proto-tasks cont defins))))

(defn build-meta
  "Stores the meta data of an mpd:
  
  * standard
  * name
  * description
  * number of containers
  * number of definitions
  "
  [{mp-id :mp-id std :Standard name :Name descr :Description cont :Container defins :Definitions}]
  (let [m {:mp-id mp-id :struct :meta}]
    (stmem/set-val (assoc m :metapath :std :value std))
    (stmem/set-val (assoc m :metapath :name :value name))
    (stmem/set-val (assoc m :metapath :descr :value descr))
    (stmem/set-val (assoc m :metapath :mp-deps :value (mp-deps cont defins)))
    (stmem/set-val (assoc m :metapath :task-deps :value (task-deps cont defins)))
    (stmem/set-val (assoc m :metapath :nd :value (count defins)))
    (stmem/set-val (assoc m :metapath :nc :value (count cont)))))

;;------------------------------
;; build mpd doc
;;------------------------------
(defn build-mpd [{mp-id :_id m :Mp}]
  (let [m (safe (assoc m :mp-id mp-id))]
    (build-exchange m)
    (build-meta m)
    (build-all-container m)
    (build-all-definitions m)))

;;------------------------------
;; clear mpd doc
;;------------------------------
(defn clear-mpd [{mp-id :mp-id}]
  (stmem/del-vals {:mp-id mp-id :struct :*})) 

;;------------------------------
;; build tasks
;;------------------------------
(defn build-tasks [tasks]
  (map (fn [{task-name :TaskName :as task} ]
         (assoc (stmem/set-val {:task-name task-name
                                :value (safe task)}) :task-name task-name))
       tasks))

;;------------------------------
;; clear mpd doc
;;------------------------------
(defn clear-tasks [] (stmem/del-vals {:task-name :*})) 
