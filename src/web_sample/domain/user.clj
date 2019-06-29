(ns web-sample.domain.user
  (:refer-clojure :exclude [apply remove])
  (:require [web-sample.domain.common :refer :all]
            [clojure.core.match :refer [match]]))

(defmethod create 
  :user [_]
  (create-root :id :name :email))

(defn register [id name email]
  (println "Register user")
  (vector (create-event :user-registered
                        {:id id
                         :name name
                         :email email})
          (create-event :email-sended)))

(defn change-email [user new-email]
  (println "Email changed")
  (vector {:event-type :email-changed
           :data {:new-email new-email}}))

(defn remove [id]
  (println "Remove user")
  (vector {:event-type :user-removed
           :data       {:id id}}))

(defmethod apply-event
  :user [_ state {:keys [event-type data]}]
  (match event-type
    :user-registered (merge state data)
    :email-changed (assoc state :email (:new-email data))
    :email-sended state    
    :else (throw (Exception. (str "apply-single! Wrong state " event-type)))))