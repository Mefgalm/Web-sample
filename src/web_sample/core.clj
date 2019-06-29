(ns web-sample.core
  (:gen-class)
  (:require [web-sample.common :refer :all]
            [clojure.core.async :as async]
            [web-sample.saga :as saga]            
            [web-sample.domain.user :as d-user]
            [web-sample.write-db :as write]
            [clojure.pprint :as pp]
            [clojure.core.match :refer [match]]))

(def event-chan (async/chan))

(defn w-user-registered! []
  (println "Write-side! User registered"))

(defn r-user-registered! []
  (println "Read-side! User registered"))

(defn event-handler
  [channel]
  (async/go-loop []
    (let [[{:keys [data] :as root-event} reply-channel] (async/<! channel)
          {:keys [event-type event-data]} data]
      (match event-type
        :user-removed
        (do 
          (println "User removed!")
          (async/>! reply-channel :fail))
        :user-registered
        (do (w-user-registered!)
            (r-user-registered!)
            (async/>! reply-channel :ok))
        :email-sended
        (do
          (println "Email sended!")
          (async/>! reply-channel :ok))
        :email-changed
        (do 
          (println "Email changes")
          (async/>! reply-channel :ok))
        :else (throw (Exception. (str "Wrong event: " root-event)))))
    (recur)))

(defn command-response [command events]
  {:command command
   :events events})

(defn add-compensation [cmd data]
  (assoc cmd :compensation data))

(defn build-cmd
  ([cmd-key cmd-data]
   {:command-type cmd-key
    :data cmd-data})
  ([cmd-key cmd-data
    comp-key comp-data]
   (let [cmd (build-cmd cmd-key cmd-data)
         comp (build-cmd comp-key comp-data)]
     (add-compensation cmd comp))))

(defn run-command
  [{:keys [command-type data] :as command}]
  (println "run-command " command)
  (match command-type
    :register-user (let [{:keys [name email]} data
                         id                   12
                         user-root            (write/create-entity :user)
                         compensation         (build-cmd :remove-user {:id id})
                         events               (d-user/register id name email)
                         adapted-events       (write/adapt-events user-root events now)]
                     (command-response (add-compensation command compensation)
                                       adapted-events))
    :change-email (let [{:keys [id new-email]} data
                        user-root              (write/get-entity :user id)
                        user                   (:entity user-root)
                        compensation           (build-cmd :change-email {:id        id
                                                                         :new-email (:email user)})
                        events                 (d-user/change-email user new-email)
                        adapted-events         (write/adapt-events user-root events now)]
                    (command-response (add-compensation command compensation)
                                      adapted-events))
    :remove-user (d-user/remove)
    :else (throw (Exception. (str "run-command !Wrong state " command)))))

(defn run-event [event]
  (let [reply-channel (async/chan)]
    (async/>!! event-chan [event reply-channel])
    (async/alt!!
      reply-channel ([x] x)
      (async/timeout 1000) :timeout)))

(defn request->command
  [{:keys [body]}]
  body)

(defn -main []
  (event-handler event-chan)
  (let [saga (saga/run
              run-command
              run-event
              (build-cmd :register-user
                         {:name  "Vlad"
                          :email "kuz@ff"})
              (build-cmd :change-email
                         {:id 12
                          :new-email "mefgalm@gmail.com"}))]
    (println  "end " saga)))