(ns web-sample.core
  (:gen-class)
  (:require [clojure.core.async :as async]
            [web-sample.saga :as saga]
            [web-sample.domain.common :as d-com]
            [web-sample.domain.user :as d-user]
            [clojure.core.match :refer [match]]))

(def event-chan (async/chan))

(defn w-user-registered! []
  (println "Write-side! User registered"))

(defn r-user-registered! []
  (println "Read-side! User registered"))

(defn event-handler
  [channel]
  (async/go-loop []
    (let [[{:keys [event-type data] :as event} reply-channel] (async/<! channel)]
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
          (async/>! reply-channel :fail))
        :else (throw (Exception. (str "Wrong event: " event)))))
    (recur)))

(defn run-command
  [{:keys [command-type] :as command}]
  (println "run-command " command)
  (match command-type
    :register-user (d-user/register)
    :remove-user (d-user/remove)
    :else (throw (Exception. (str "run-command !Wrong state " command)))))

(defn run-event [event]
  (let [reply-channel (async/chan)]
    (async/>!! event-chan [event reply-channel])
    (async/alt!!
      reply-channel ([x] x)
      (async/timeout 1000) :timeout)))

(defn build-cmd
  ([cmd-key cmd-data]
   {:command-type cmd-key
    :data cmd-data})
  ([cmd-key cmd-data
    comp-key comp-data]
   (let [cmd (build-cmd cmd-key cmd-data)
         comp (build-cmd comp-key comp-data)]
     (assoc cmd :compensation comp))))

(defn make-register-user-command
  [key data]
  (match key
    :register-user (let [{:keys [name email]} data
                         id 1]
                     (build-cmd :register-user
                                (d-user/register-data id name email)
                                :remove-user                                           
                                (d-user/remove-data id)))
    :else nil))


(defn request->command
  [{:keys [body]}]
  body)

(let [user-events (d-user/register 1 "vlad" "kuz@mail.com")]
  (d-com/root-apply :user user-events))

(defn -main []
  (event-handler event-chan)
  (let [saga (saga/run run-command run-event (make-register-user-command :register-user {:name "Vlad"
                                                                                         :email "kuz@ff"}))]
    (println  "end " saga)))