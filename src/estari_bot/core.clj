(ns estari-bot.core
  (:require [estari-bot.scrape :as scrape]
            [estari-bot.markov :as m]
            [estari-bot.ask :refer [ask-question]]
            [environ.core :refer [env]]
            [twitter.api.restful :as twitter]
            [twitter.oauth :as twitter-oauth])
  (:gen-class))

(def my-creds (twitter-oauth/make-oauth-creds
               (env :app-consumer-key)
               (env :app-consumer-secret)
               (env :user-access-token)
               (env :user-access-secret)))

(defn get-mentions []
  (twitter/statuses-mentions-timeline :oauth-creds my-creds))

(defn parse-ment [mention]
  {:name (-> mention :user :screen_name)
   :text (:text mention)})

(defn parse-mentions [response]
  (let [mentions (:body response)]
    (map parse-ment mentions)))

(defn send-tweet []
  (let [tweet (str (m/new-message) " #estarit")]
    (println "tweet: " tweet)
    (if (> (count tweet) 140) (send-tweet)
        (when (not-empty tweet)
          (try (twitter/statuses-update :oauth-creds my-creds
                                        :params {:status tweet})
               (catch Exception e (println "Error! " (.getMessage e))))))))

(defn run []
  (do
    (send-tweet)
    (Thread/sleep 600000)
    (run)))

(defn -main [& args]
  (run))
