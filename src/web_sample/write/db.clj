(ns web-sample.write.db
  (:require [clojure.java.jdbc :as db]))

(def pg-db {:dbtype "postgresql"
            :dbname "sam"
            :host "127.0.0.1"
            :user "sample"
            :password "123"
            ;:ssl true
            :sslfactory "org.postgresql.ssl.NonValidatingFactory"})


(db/query pg-db
         ["select * from events"])