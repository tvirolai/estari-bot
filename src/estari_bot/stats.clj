(ns estari-bot.stats
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.data.csv :as csv]
            [incanter.core :as i]
            [incanter.charts :as c]
            [estari-bot.markov :refer [get-sentences]]))

(defn- data-sentences [data]
  (->> data (map get-sentences) flatten))

(def data
  "Load the whole dataset as a lazy sequence."
  (with-open [in-file (io/reader "./data/estarit_uusi.tsv")]
    (doall
      (flatten (csv/read-csv in-file :separator \tab)))))

(def hs-sent (->> (str/split (slurp "./data/hs.txt") #"\n")
                  (filter #(> (count %) 0))
                  data-sentences))

; A smaller dataset to test functions
(def sample (take 100 data))

(def stopwords
  (with-open [in-file (io/reader "./data/fi_stopwords.txt")]
    (set (doall (line-seq in-file)))))

(defn- is-capitalized? [string]
  (let [initial (str (first (str/trim string)))]
    (= initial (str/upper-case initial))))

(defn- starting-number? [string]
  (boolean (re-find #"^\d" string)))

(defn- filter-stopwords [se]
  (filter #((complement contains?) stopwords %) se))

(defn- tokenize [se]
  (let [string (apply str se)]
    (map str/lower-case (re-seq #"[\p{L}\p{M}]+" string))))

(defn- word-freq [string]
  (frequencies string))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; Public functions ;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn tokenfrequencies [dataset]
  (let [strdata (apply str dataset)]
    (->> strdata
         (tokenize)
         (word-freq))))

(defn toptokens [se amount]
  "Takes an integer and returns the top (amount) tokens from the data."
  (->> se
       (tokenize)
       (filter-stopwords)
       (filter #(> (count %) 1))
       (word-freq)
       (sort-by last)
       (reverse)
       (take amount)))

(defn aliases 
  "Takes a sequence of messages and returns a list of writer aliases"
  [se]
  (->> se
       (map #(str/split % #"—"))
       (filter #(> (count %) 1))
       (map #(str/trim (last %)))
       (filter is-capitalized?)
       (remove starting-number?)
       (filter #(< 3 (count %) 20))
       (frequencies)
       (sort-by last)
       (reverse)))

(def aliaslist "A list of writer aliases." (aliases data))

(defn get-sent-by-endchar [data endchar]
  (->> (data-sentences data) (filter #(str/ends-with? % endchar))))

(defn break-sentence [sentence]
  (-> sentence tokenize filter-stopwords))

(defn ends-with-percentage [data endchar]
  (let [excl (count (get-sent-by-endchar data endchar))
        others (count (data-sentences data))]
    (Math/round (float (* 100 (/ excl others))))))

(defn plot-endings [data]
  (let [values (map (partial ends-with-percentage data) ["." "!" "?"])
        chart (c/bar-chart ["piste" "huutomerkki" "kysymysmerkki"]
                            values
                            :title "Lauseet päättävät merkit"
                            :x-label ""
                            :y-label "Osuus (%)")]
    (do
      (println values)
      (i/view chart)
      (i/save chart "hsplot.png"))))
