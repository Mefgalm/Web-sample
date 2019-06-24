(ns web-sample.domain.common)

(defn create-root
  [p & ps]
  (reduce #(assoc %1 %2 nil)
          {}
          (cons p ps)))

(defmulti apply-event 
  (fn [entity-type state event] 
    entity-type))

(defmulti create identity)

(defn create-event
  [event-type data]
  {:event-type event-type
   :data data})

(defn rehydrate-entity 
  [entity-type events]
  (let [apply-event-type (partial apply-event entity-type)
        create-type (partial create entity-type)]
    (reduce apply-event-type (create-type) events)))