(ns estari-bot.scrape
  (:require [net.cgrand.enlive-html :as html]
            [org.httpkit.client :as http]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.local :as l]))

(defn parse-url [date]
  (let [url-root "http://www.esaimaa.fi/page.php?page_id=30" 
        day (t/day date) month (t/month date) year (t/year date)]
    (str url-root "&searchDate=" day "&searchMonth=" month "&searchYear=" year)))

(defn fetch-page 
  "Takes an URL and returns a DOM element" 
  [url]
  (html/html-snippet
    (:body @(http/get url {:insecure? true}))))

(defn get-estarit
 "Extracts messages from the retrieved DOM element" 
 [dom] 
  (html/select dom [:div#c1 :p]))

(defn get-estarit-old
 "Extracts messages from the retrieved DOM element" 
 [dom] 
  (html/select dom [:div#c1]))

(defn get-estari-content 
  "Extract individual messages into a list of vectors.
  Args: url and a selector function (get-estarit or get-estarit-old)"
  [url selfunc]
  (->> (selfunc (fetch-page url))
       (map #(get % :content))
       (flatten)
       (filter string?)
       (filter #(< 50 (count %)))
       (map #(vector %))))

(defn previousdate 
 "Returns a date object for a date [days] prior to present."
 [days]
  (-> days t/days t/ago))

(def end-date
  (t/date-time 2008 1 1))

(def time-formatter
  (f/formatter "yyyy-MM-dd"))

(defn save-to-file [data filename]
  (with-open [out-file (io/writer (str "./data/" filename) :append true)]
    (csv/write-csv out-file data :separator "\t")))

(defn msgs-for-day [date]
  (let [msgs (get-estari-content (parse-url date) get-estarit)]
    (if (empty? msgs)
      (get-estari-content (parse-url date) get-estarit-old)
      msgs)))

(defn scrape-all 
  "This function scrapes and writes to file all messages 
  beginning from today and until the end-date."
  [filename]
  (loop [prev 0 total 0]
    (let [date (previousdate prev)
          msgs (msgs-for-day date)
          amount (count msgs)]
      (do
        (save-to-file (vec msgs) filename)
        (println (str 
                   (f/unparse time-formatter date) 
                   " - messages per day: " amount 
                   ", total: " (+ amount total)))
        (Thread/sleep 5000)
      (if (t/after? date end-date) (recur (inc prev) (+ total amount)))))))
