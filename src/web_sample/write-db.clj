(ns web-sample.write-db
  (:require [web-sample.common :refer :all]
            [web-sample.domain.common :refer :all]
            [web-sample.domain.user :as dom-user]))

(defn now [] (java.util.Date. 2019 01 01))

(def db-events
  [{:id 1
    :stream-id "0ad07d4e-6998-4198-bb00-44c3393c0669"
    :data (serialize-json {:event-type :user-registered
                           :data {:id 1
                                  :name "vlad"
                                  :email "mef@mail.com"}})
    :type :user
    :log-data (now)}
   {:id 2
    :stream-id "0ad07d4e-6998-4198-bb00-44c3393c0669"
    :data (serialize-json {:event-type :email-changed
                           :data {:new-email "mef2@mail.com"}})
    :type :user
    :log-data (now)}])

(defn create-aggregate
  [stream-id type event-serial entity]
  {:event-serial event-serial
   :stream-id stream-id
   :type type
   :entity entity})

(defn event-with-stream
  [id stream-id type event time-fn]
  {:id id
   :stream-id stream-id
   :data (serialize-json event)
   :type type
   :log-data (time-fn)})

(defn adapt-events
  [{:keys [event-serial stream-id type time-fn] :as aggregate} events]  
  (map (fn [id event] (event-with-stream (+ event-serial id)
                                         stream-id
                                         type
                                         event
                                         time-fn))
       events))

(defn safe-max
  ([] 0)
  ([x] (max x))
  ([x & xs] (apply max (cons x xs))))

(defn get [entity-type stream-id]
  (let [entity-events (filter #(= (:stream-id %) stream-id) db-events)
        domain-events (map (comp parse-json :data) entity-events)]
    (create-aggregate stream-id
                      entity-type
                      (apply safe-max (map :id entity-events))
                      (root-apply entity-type
                                  domain-events))))