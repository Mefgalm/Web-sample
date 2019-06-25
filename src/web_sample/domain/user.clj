(ns web-sample.domain.user
  (:refer-clojure :exclude [apply remove])
  (:require [web-sample.domain.common :refer :all]
            [clojure.core.match :refer [match]]))

(defn register-data
  [id name email]
  {:id id
   :name name
   :email email})

(defn remove-data [id]
  {:id id})

(defmethod create 
  :user [_]
  (create-root :id :name :email))

(defn register [id name email]
  (println "Register user")
  (vector (create-event :user-registered
                        (register-data id name email))
          (create-event :email-sended)))

(defn change-email [user new-email]
  (println "Email changed")
  (vector {:event-type :email-changed
           :data {:new-email new-email}}))

(defn remove [id]
  (println "Remove user")
  (vector {:event-type :user-removed
           :data (remove-data id)}))

(defmethod apply-event
  :user [_ state {:keys [event-type data]}]
  (match event-type
    :user-registered (merge state data)
    :email-changed (assoc state :email (:new-email data))
    :email-sended state    
    :else (throw (Exception. (str "apply-single! Wrong state " event-type)))))