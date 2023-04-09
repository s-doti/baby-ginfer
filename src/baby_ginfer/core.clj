(ns baby-ginfer.core
  (:require [baby-sepl.core :refer [baby-sepl baby-step]]
            [baby-ginfer.quality-of-life :refer :all]))

;in-mem persistence (default)
(def in-mem-connector
  {:load-fn (fn [cache node attribute]
             (get-in cache [node attribute]))
   :store-fn (fn [cache node attribute value]
             (assoc-in cache [node attribute] value))})

;graph persistence side-effects
(defn get! [{:keys [connector nodes] :as state} node attribute]
  ((:load-fn connector) nodes node attribute))
(defn mutate! [{:keys [connector nodes] :as state} node attribute value]
  (update state :nodes (:store-fn connector) node attribute value))

;generic/common graph logic
(defn resolve [state node paths f]
  "Walk the graph starting at node,
   execute f upon reaching the end point of each path,
   and return the sequence of outcomes"
  (letfn [(stepper [x path]
            (when x
              (if (coll? x)
                (map #(getter % path) x)
                (getter x path))))
          (getter [n [step & steps]]
            (if steps
              (-> (get! state node step)
                  (stepper steps))
              (f n step)))]
    (map (partial getter node) paths)))

;graph inference algorithm declaration for baby-sepl (update->notify-eval naive loop)
(def ginfer-flows
  {"update-flow" {:side-effect-action (fn get-value [state [node attribute]]
                                        [(get-in state [:blueprints attribute :ref])
                                         (get! state node attribute)])
                  :pure-action        (fn update-fn [[node attribute value] [ref curr-value]]
                                        (when (not= value curr-value)
                                          (cond-> []
                                                  :mutate (conj (baby-step "mutate-flow" [node attribute value]))
                                                  (some? ref) (conj (baby-step "update-flow" [value ref node]))
                                                  :notify (conj (baby-step "notify-flow" [node attribute])))))}
   "mutate-flow" {:side-effect-action (fn mutate [state [node attribute value]]
                                        (mutate! state node attribute value))}
   "notify-flow" {:side-effect-action (fn get-listeners [state [node attribute]]
                                        (let [listeners-paths (get-in state [:blueprints attribute :listeners])]
                                          (->> (resolve state node listeners-paths #(do [%1 %2]))
                                               (flatten)
                                               (filter some?)
                                               (partition-all 2))))
                  :pure-action        (fn notify [args listeners]
                                        (map #(baby-step "eval-flow" %) listeners))}
   "eval-flow"   {:side-effect-action (fn get-sources-data [state [node attribute]]
                                        (let [{:keys [eval-fn sources]} (get-in state [:blueprints attribute])]
                                          [eval-fn
                                           (resolve state node sources #(get! state %1 %2))]))
                  :pure-action        (fn eval [[node attribute] [eval-fn sources-data]]
                                        (let [value (apply eval-fn sources-data)]
                                          [(baby-step "update-flow" [node attribute value])]))}})

;main api
(defn infer
  ([blueprints events] (infer blueprints events in-mem-connector))
  ([blueprints events connector]
   (baby-sepl ginfer-flows
              {:blueprints (deduce-listeners (expand-refs blueprints))
               :connector  connector}
              events)))
