(ns baby-ginfer.t-core
  (:require [midje.sweet :refer :all]
            [baby-ginfer.quality-of-life :refer :all]
            [baby-ginfer.core :refer :all]))

;demonstrates the basic core mechanics

(fact
  "empty declaration does absolutely nothing"
  (let [blueprints {}
        events []
        final-state (infer blueprints events)]
    (:nodes final-state) => nil))

(fact
  "data point attribute declaration"

  (let [blueprints {"some data point" (generic)}
        events [(update-node "some node" "some data point" "some arbitrary value")]
        final-state (infer blueprints events)]

    (get-in final-state [:nodes "some node" "some data point"]) => "some arbitrary value"))

;inference

(fact
  "data point attribute inference declaration"

  (let [blueprints {"some data point" (generic)
                    "inferred data point" (inferred-with inc [["some data point"]])}
        events [(update-node "some node" "some data point" 2)]
        final-state (infer blueprints events)]

    (get-in final-state [:nodes "some node" "inferred data point"]) => 3))

(fact
  "data point notifies multiple inferred data points"

  (let [blueprints {"square's edge length" (generic)
                    "square's area"        (inferred-with (fn [edge-len] (* edge-len edge-len))
                                                          [["square's edge length"]])
                    "square's perimeter"   (inferred-with (partial * 4)
                                                          [["square's edge length"]])}
        events [(update-node "3x3" "square's edge length" 3)]
        final-state (infer blueprints events)]

    (get-in final-state [:nodes "3x3" "square's area"]) => 9
    (get-in final-state [:nodes "3x3" "square's perimeter"]) => 12))

(fact
  "data point inferred from multiple data points"

  (let [blueprints {"width"          (generic)
                    "height"         (generic)
                    "rectangle area" (inferred-with (fnil * 0 0)
                                                    [["width"]
                                                     ["height"]])}
        events [(update-node "3x2" "width" 3)
                (update-node "3x2" "height" 2)]
        final-state (infer blueprints events)]

    (get-in final-state [:nodes "3x2" "rectangle area"]) => 6))

;reference

(fact
  "data points referencing one another"

  (let [blueprints {"connects to" (links-with "connected from")}
        events [(update-node "some node" "connects to" "another node")]
        final-state (infer blueprints events)]

    (get-in final-state [:nodes "another node" "connected from"]) => "some node"))

;inference via reference

(fact
  "data point is inferred via reference path"

  (let [blueprints {"connects to"         (links-with "connected from")
                    "data point"          (generic)
                    "inferred data point" (inferred-with (fnil inc 0) [["connects to" "data point"]])}
        events [(update-node "some node" "connects to" "another node")
                (update-node "another node" "data point" 2)]
        final-state (infer blueprints events)]

    (get-in final-state [:nodes "some node" "inferred data point"]) => 3))
