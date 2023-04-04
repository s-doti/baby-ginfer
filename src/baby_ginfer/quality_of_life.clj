(ns baby-ginfer.quality-of-life
  (:require [baby-sepl.core :refer [baby-step]]))

;this ns is 100% optional,
;just adding 'quality-of-life' features,
;to aid in graph blueprints declaration

;'dsl' utils
; just being cute, sprinkle syntactic sugar to hide explicit, less readable, maps

(defn update-node [id attribute value]
  (baby-step "update-flow" [id attribute value]))

(defn notify-node [id attribute]
  (baby-step "notify-flow" [id attribute]))

(defn eval-node [id attribute]
  (baby-step "eval-flow" [id attribute]))

(defn generic
  "Create a generic/simple/static attribute"
  [] {})

(defn links-with
  "Create a reference attribute, linked with another"
  [ref]
  {:ref ref})

(defn inferred-with
  "Create an inferred attribute given pure logic and source-paths"
  ([attribute f sources-paths] (merge attribute (inferred-with f sources-paths)))
  ([f source-paths]
   {:eval-fn f :sources source-paths}))

;boot logic - only for convenience, simplify graph blueprints declaration

(defn deduce-listeners [blueprints]
  "Remove the need to explicitly declare 'listeners'.
   Per any entry in the given map of the form [A {:sources [[->B->C]]}],
   ensure the reverse paths [C {:listeners [[->B->A]]}] [B {:listeners [[->A]]}] exist."
  (letfn [(deduce-propagation-paths [blueprints]
            (for [[target {:keys [sources]}] blueprints
                  path sources]
              (when (every? (partial get blueprints) path)
                (loop [propagation-paths []
                       [source & steps] (reverse path)]
                  (let [propagation-path (conj (mapv #(:ref (get blueprints %)) steps) target)]
                    (cond-> propagation-paths
                            (every? (comp not nil?) propagation-path) (conj [source propagation-path])
                            steps (recur steps)))))))
          (apply-listeners [blueprints [attribute propagation-path]]
            (update-in blueprints [attribute :listeners] (comp distinct conj) propagation-path))]
    (->> (deduce-propagation-paths blueprints)
         (apply concat)
         (reduce apply-listeners blueprints))))

(defn expand-refs [blueprints]
  "Remove the need to declare symmetric relations.
   Per any entry in the given map of the form [A {:ref B}],
   ensure the symmetric entry [B {:ref A}] exists."
  (reduce (fn [blueprints [attribute {:keys [ref]}]]
            (cond-> blueprints
                    ref (assoc ref {:ref attribute})))
          blueprints
          blueprints))
