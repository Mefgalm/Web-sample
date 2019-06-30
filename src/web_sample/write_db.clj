(ns web-sample.write-db
  (:require [web-sample.common :refer :all]
            [web-sample.domain.common :refer :all]
            [clojure.java.jdbc :as db]
            [honeysql.core :as sql]
            [honeysql.helpers :refer :all]
            [web-sample.domain.user :as dom-user]))

(def pg-db {:dbtype     "postgresql"
            :dbname     "sam"
            :host       "127.0.0.1"
            :user       "sample"
            :password   "123"
            ;:ssl true
            :sslfactory "org.postgresql.ssl.NonValidatingFactory"})

(defn select-events-q [streamid]
  {:select [:*]
   :from   [:events]
   :where  [:= :streamid streamid]})

(defn save-event-q [root-event]
  {:insert-into :events
   :values      [root-event]})

(defn select-events [streamid]  
  (db/query pg-db (sql/format (select-events-q streamid))))

(defn save-event [root-event]
  (db/execute! pg-db (sql/format (save-event-q root-event))))

(def db-events
  [{:id        1
    :streamid "0ad07d4e-6998-4198-bb00-44c3393c0669"
    :version   0
    :data      (serialize-json {:event-type :user-registered
                                :event-data {:id    1
                                             :name  "vlad"
                                             :email "mef@mail.com"}})
    :type      :user
    :logdate  (now)}
   {:id       2
    :streamid "0ad07d4e-6998-4198-bb00-44c3393c0669"
    :version  1
    :data     (serialize-json {:event-type :email-changed
                               :event-data {:new-email "mef2@mail.com"}})
    :type     :user
    :logdate  (now)}])

; (defn save-event [root-event]
;   )

(defn create-aggregate
  ([streamid type]
   (create-aggregate streamid type 0 nil))
  ([streamid type version entity]
   {:version  version
    :streamid streamid
    :type     type
    :entity   entity}))

(defn event-with-stream
  [streamid type version event time-fn]
  {:version  version
   :streamid streamid
   :data     event
   :type     (str type)
   :meta     nil
   :logdate  (str (time-fn))})

(defn adapt-events  
  ([{:keys [streamid type version] :as root} events time-fn]
   (map-indexed (fn [index event] 
                  (event-with-stream streamid
                                     type
                                     (+ version index)
                                     event
                                     time-fn))
                events)))

(defn get-entity [type streamid]
  (let [entity-events (filter #(= (:streamid %) streamid) db-events)
        domain-events (map (comp parse-json :data) entity-events)]
    (if (seq entity-events)
      (create-aggregate streamid
                        type
                        (inc (apply max (map :version entity-events)))
                        (rehydrate-entity type
                                          domain-events))
      (throw (Exception. (str "Entity with id " streamid " and type " type " not found"))))))

(defn create-entity [type]
  (create-aggregate (uuid) type))