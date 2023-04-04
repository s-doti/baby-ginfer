# baby-ginfer

A baby version of the Ginfer (graph inference) library.

This baby is designed with the sole purpose of delivering a gist.
Its core weighs less than 100 lines of code, and still supports the basic concepts:
- Declarative graph structure (aka graph blueprints)
- Separation of execution context and logic
- Agnostic to state/data-model, or even storage tech
- Notifications & evaluations along paths of interconnected data-points (nodes)

It builds on the [baby-sepl](https://github.com/s-doti/baby-sepl) (algorithm execution) engine.
Compared to its mature version, it is limited in scope;
Only 1:1 relations are supported, the attributes' mechanics are very limited, etc.

See examples under [test/baby_ginfer](../tree/main/test/baby_ginfer):
- Core mechanics: notifications/evaluations via inference/reference
- BFF: inference via reference
- Vicious cycle: a cyclic case
- Pets life: order-agnostic consistency
- Riddle: an elaborate, self-bootstrapping/self-solving riddle
- Zero mem: use fs-connector in place of the default in-mem

## Usage

In the following example we: 
1. Declare a graph structure where 
a node infers its own data based on its neighbor's. We then-
2. Simulate an event to tie two nodes together, 
and another to update some data point per one of the nodes. Next we-
3. Run graph inference, injecting the declared graph structure (aka blueprints)
and the events. Finally, we-
4. Observe the inferred data point outcome 
```clojure
(let [blueprints {"connects to"         (links-with "connected from")
                  "data point"          (generic)
                  "inferred data point" (inferred-with (fnil inc 0) [["connects to" "data point"]])}
      events [(update-node "some node" "connects to" "another node")
              (update-node "another node" "data point" 2)]
      final-state (infer blueprints events)]

  (get-in final-state [:nodes "some node" "inferred data point"]) => 3)
```
* Note: the order of events does not matter, the outcome always remains the same

## License

Copyright © 2023 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
