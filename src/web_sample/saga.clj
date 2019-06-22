(ns web-sample.saga
  (:refer-clojure :exclude [next])
  (:require [clojure.core.match :refer [match]]))

(defn create
  [commands]
  {:saga-type :in-forward
   :commands commands
   :complete (list)})

(defn apply-command
  [run-cmmand-fn run-event-fn
   {:keys [saga-type commands complete] :as saga}
   {:keys [command-type] :as command}]
  (match [saga-type command-type]
    [:in-forward :proceed]
    (let [[current & next] commands]
      (if current
        (let [event-statuses (->> (run-cmmand-fn current)
                                  (map run-event-fn))]
          (if (every? (partial = :ok) event-statuses)
            (merge saga {:commands next
                         :complete (cons current complete)})
            (merge saga {:saga-type :in-backward
                         :complete (empty complete)
                         :commands (cons current complete)})))
        (merge saga {:saga-type :done})))
    [:in-backward :proceed]
    (let [[current & next] commands]
      (if current
        (let [compensation-command (:compensation current)
              event-statuses (->> (run-cmmand-fn compensation-command)
                                  (map run-event-fn))]
          (if (every? (partial = :ok) event-statuses)
            (merge saga {:commands next
                         :complete (cons current complete)})
            (merge saga {:saga-type :fail})))
        (merge saga {:saga-type :backward-done})))
    :else (throw (Exception. (str "apply-command !Wrong state " saga-type " " command-type)))))

(defn next
  [{:keys [saga-type]}]
  (match saga-type
    :in-forward {:command-type :proceed}
    :in-backward {:command-type :proceed}
    :else (throw (Exception. (str "saga-next !Wrong state " saga-type)))))

(defn run
  [run-command-fn run-event-fn cmd1 & cmds]
  (let [apply-command-baked (partial apply-command
                                     run-command-fn
                                     run-event-fn)]
    (loop [{:keys [saga-type] :as saga} (create (cons cmd1 cmds))]
      (if (or (= :done saga-type)
              (= :backward-done saga-type)
              (= :fail saga-type))
        saga
        (recur (apply-command-baked saga (next saga)))))))