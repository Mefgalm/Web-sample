(ns web-sample.write-db
  (:require [web-sample.common :refer :all]
            [web-sample.domain.common :refer :all]
            [web-sample.domain.user :as dom-user]))

(def db-events
  [{:id        1
    :stream-id "0ad07d4e-6998-4198-bb00-44c3393c0669"
    :version   0
    :data      (serialize-json {:event-type :user-registered
                                :event-data {:id    1
                                             :name  "vlad"
                                             :email "mef@mail.com"}})
    :type      :user
    :log-data  (now)}
   {:id        2
    :stream-id "0ad07d4e-6998-4198-bb00-44c3393c0669"
    :version   1
    :data      (serialize-json {:event-type :email-changed
                                :event-data {:new-email "mef2@mail.com"}})
    :type      :user
    :log-data  (now)}])

(defn create-aggregate
  ([stream-id type]
   (create-aggregate stream-id type 0 nil))
  ([stream-id type version entity]
   {:version   version
    :stream-id stream-id
    :type      type
    :entity    entity}))

(defn event-with-stream
  [stream-id type version event time-fn]
  {:version   version
   :stream-id stream-id
   :data      event
   :type      type
   :log-data  (time-fn)})

(defn adapt-events  
  ([{:keys [stream-id type version] :as root} events time-fn]
   (map-indexed (fn [index event] 
                  (event-with-stream stream-id
                                     type
                                     (+ version index)
                                     event
                                     time-fn))
                events)))

(defn get-entity [type stream-id]
  (let [entity-events (filter #(= (:stream-id %) stream-id) db-events)
        domain-events (map (comp parse-json :data) entity-events)]
    (if (seq entity-events)
      (create-aggregate stream-id
                        type
                        (inc (apply max (map :version entity-events)))
                        (rehydrate-entity type
                                          domain-events))
      (throw (Exception. (str "Entity with id " stream-id " and type " type " not found"))))))

(defn create-entity [type]
  (create-aggregate (uuid) type))