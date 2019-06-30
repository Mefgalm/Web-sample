(ns web-sample.common
  (:require [clojure.data.json :as json]))

(defn uuid [] (java.util.UUID/randomUUID))

(defn now [] (new java.util.Date))

(defn- str-keyword? [s]
  (when (string? s)
    (let [[f & left] s]
      (and (seq left)
           (= f \:)))))

(defn- str->keyword [[_ & rest]]
  (keyword rest))

(defn- str?->keyword [x]
  (if (str-keyword? x)
    (keyword (apply str (rest x)))
    x))

(defn- keyword?->str [k]
  (if (keyword? k) (str k) k))

(defn parse-json [string]
  (json/read-str string 
                 :key-fn str?->keyword
                 :value-fn #(str?->keyword %2)))

(defn serialize-json [map-object]
  (json/write-str map-object
                  :key-fn keyword?->str
                  :value-fn #(keyword?->str %2)))

; (def t1 {:event-type :user-registered
;          :ok "vlad"
;          :data {:id 1
;                 :name "vlad"
;                 :sex :male
;                 :email "mef@mail.com"}})

; (= t1 (parse-json (serialize-json t1)))

