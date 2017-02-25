(ns estari-bot.markov
     (:require [clojure.string :as s]
               [opennlp.nlp :as nlp]
               [markov.core :as m]))

(def names (-> (slurp "./data/nimim_uusi.txt") (s/split #"\n")))

(def get-sentences
  (nlp/make-sentence-detector "./data/fi-sent.bin"))

(def trans-table
  (-> "./data/table.edn" slurp read-string))

(def all-keys (vec (map first (keys trans-table))))

(defn- build-table [inputfile outputfile]
  (->> inputfile
      m/build-from-file
      (spit outputfile)))

(defn- remove-trailing-period [string]
  (if (= \. (last string))
    (subs string 0 (dec (count string)))
    string))

(defn- get-random-key []
  (let [word (->> (get all-keys (rand-int (count all-keys)))
                  (re-find #"\w+") s/capitalize)]
    (if (contains? (set all-keys) word)
      word
      (get-random-key))))

(defn- write-to-log [message]
  (spit "messages.log" (str message "\n\n") :append true))

(defn- is-proper-sent?
  "Tries to validate a sentence by checking that it's capitalized,
  does not end in a letter and has at least two words in it."
  [sent]
  (and
   (= (s/capitalize sent) sent)
   (contains? #{\. \? \! \"}  (last sent))
   (-> sent (s/split #" ") count (> 1))))

(defn- shorten-message
  "Shorten message by one sentence."
  [message]
  (s/join " " (rest (get-sentences message))))

(defn new-sent [table length startword]
  (let [sentences 
        (->> table
             (m/generate-walk startword)
             (take length)
             (s/join " ")
             (get-sentences)
             (filter is-proper-sent?))]
    (if (> (count sentences) 1) (s/join " " sentences)
        (new-sent table length startword))))

(defn generate-name
  "Generate a name for the author. Try not to use any names
  found in the input data without modification."
  [names]
  (let [words (-> (s/join #" " names) (s/split #" ") shuffle)
        firstw (-> words first s/capitalize remove-trailing-period)
        restw (s/join " " (take (rand-int 2) (shuffle words)))
        name (s/trim (str firstw " " restw))]
    (if (contains? (set (map remove-trailing-period names)) name)
      (generate-name names)
      name)))

(defn make-signature [message names]
  (str message " - " (generate-name names) ))

(defn generate-msg
  "Generates a message with a randomly generated signature"
  [startword]
  (let [msg
        (-> (new-sent trans-table 20 startword)
            (make-signature names))]
    (if (> (count msg) 140) (shorten-message msg) msg)))

(defn new-message
  "Entry point function, returns and logs a message.
  Takes a starting word as an optional argument. If the
  word is not found in the transition table, it is ignored."
  ([] (doto (generate-msg (get-random-key)) (write-to-log)))
  ([word]
   (let [w (s/capitalize word)
         validw (if (contains? (set all-keys) w)
                  w
                  (get-random-key))]
     (doto (generate-msg validw) (write-to-log)))))

