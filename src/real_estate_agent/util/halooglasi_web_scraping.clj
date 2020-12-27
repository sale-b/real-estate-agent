(ns real-estate-agent.util.halooglasi-web-scraping
  (:require [clojure.data.json :as json]
            [net.cgrand.enlive-html :as html])
  (:use [clojure.string :only [index-of]]))



(defn html-page [url] (html/html-resource (java.net.URL. url)))


(defn get-geolocation [page]
  (let [sting
        (some #(and (clojure.string/includes? % "GeoLocationRPT") %)
                       (map html/text
                            (html/select
                              page
                              [:script])))]
    (:GeoLocationRPT (json/read-str
                       (subs sting
                             (+ (index-of sting "QuidditaEnvironment.CurrentClassified=") 38)
                             (index-of sting "; for")) :key-fn keyword))))
