(ns web-sample.domain)

(defn register-user-data
  [id name email]
  {:id id
   :name name
   :email email})

(defn remove-user-data [id]
  {:id id})

(defn register-user []
  (println "Register user")
  (vector {:event-type :user-registered}
          {:event-type :email-sended}))

(defn remove-user []
  (println "Remove user")
  (vector {:event-type :user-removed}))