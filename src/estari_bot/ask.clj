(ns estari-bot.ask
  (:require [estari-bot.markov :as m]
            [estari-bot.stats :refer [break-sentence]]
            [clojure.string :as s]
            [clojure.set :as set]))

(defn normalize [word]
  (->> word s/lower-case (re-find #"[A-Za-zÖÄÅäöå]+")))

(defn ask-question [sentence]
  (let [words (break-sentence sentence)
        keys (->> m/all-keys
                  (filter #(= % (s/capitalize %)))
                  (map #(normalize %))
                  (filter #(not= nil %)))
        common (set/intersection (set words) (set keys))]
    (if (not-empty common)
      (m/new-message (first (shuffle common)))
      (m/new-message))))

