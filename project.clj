(defproject estari-bot "0.1.0-SNAPSHOT"
  :description "A Twitter bot that generates nonsensical messages"
  :url "http://github.com/tvirolai/estari-bot"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [enlive "1.1.6"]
                 [http-kit "2.1.18"]
                 [clj-time "0.12.0"]
                 [clojure-opennlp "0.3.3"]
                 [twitter-api "0.7.9"]
                 [environ "1.0.0"]
                 [incanter "1.5.7"]
                 [janiczek/markov "0.3.1"]
                 [com.taoensso/timbre "4.8.0"]
                 [org.clojure/data.csv "0.1.3"]
                 [org.clojure/tools.cli "0.3.5"]]
  :main ^:skip-aot estari-bot.core
  :target-path "target/%s"
  :plugins [[lein-environ "1.1.0"]]
  :profiles {:uberjar {:aot :all}})
