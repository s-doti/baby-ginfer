(ns baby-ginfer.t-riddle
  (:require [midje.sweet :refer :all]
            [baby-ginfer.quality-of-life :refer :all]
            [baby-ginfer.core :refer :all]))

;demonstrates.. well, everything
;this is a self-bootstrapping/self-solving riddle
;it creates an elaborate graph model, and runs events all over

(fact
  "how many ways can a person traverse up a stairway,
   if allowed to skip one stair with each step,
   for any arbitrary length of stairway?"

  (let [blueprints {
                    "stair's index"    (inferred-with dec
                                                      [["one stair above" "stair's index"]])
                    "one stair below"  (-> (links-with "one stair above")
                                           (inferred-with #(when (pos? %) (str "stair #" (dec %)))
                                                          [["stair's index"]]))
                    "two stairs below" (-> (links-with "two stairs above")
                                           (inferred-with identity
                                                          [["one stair below" "one stair below"]]))
                    "paths up to here" (inferred-with (fnil + 0 1)
                                                      [["two stairs below" "paths up to here"]
                                                       ["one stair below" "paths up to here"]])
                    }
        ;a single event will bootstrap and solve the puzzle
        events [(update-node "stair #8" "stair's index" 8)]
        final-state (infer blueprints events)]

    (->> (:nodes final-state)
         (sort-by key)
         (map (comp #(get % "paths up to here") val))) =>   ;spoiler below on line 50..















    ;the correct answer, given via the fibonacci sequence:
    [nil 1 1 2 3 5 8 13 21]))
