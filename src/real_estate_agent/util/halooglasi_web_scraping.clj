(ns real-estate-agent.util.halooglasi-web-scraping
  (:require [clojure.data.json :as json]
            [net.cgrand.enlive-html :as html])
  (:use [clojure.string :only [index-of]]))

(defn get-substring [string start end]
  (subs string
        (+ (index-of string start) (.length start))
        (index-of string end)
        ))

(defn html-page [url] (html/html-resource (java.net.URL. url)))

(defn ad-content [page]
  (json/read-str
    (get-substring (some #(and (clojure.string/includes? % "CurrentContactData") %)
                         (map html/text
                              (html/select
                                page
                                [:script]))) "QuidditaEnvironment.CurrentClassified=" "; for (var")
    :key-fn keyword))

(defn get-tittle [page]
  (:content (:attrs (first (filter #(= (:property (:attrs %)) "og:title")
                                   (html/select page [:head :meta]))))))
(defn get-price [page]
  (:cena_d (:OtherFields (ad-content page))))

(defn get-real-estate-type [page]
  (:tip_nekretnine_s (:OtherFields (ad-content page))))

(defn get-rooms-number [page]
  (:broj_soba_s (:OtherFields (ad-content page))))

(defn get-floor [page]
  (:sprat_s (:OtherFields (ad-content page))))

(defn get-furniture [page]
  (:namestenost_s (:OtherFields (ad-content page))))

(defn get-heating [page]
  (:grejanje_s (:OtherFields (ad-content page))))

(defn get-living-space-area [page]
  (:kvadratura_d (:OtherFields (ad-content page))))

(defn get-description [page]
  ;sometimes html tags can occur in text
  (clojure.string/replace (:TextHtml (ad-content page)) #"<[^>]*>" ""))

(defn get-pictures [page]
  (:ImageURLs (ad-content page)))

(defn get-advertiser [page]
  (:oglasivac_nekretnine_s (:OtherFields (ad-content page))))

(defn get-geolocation [page]
  (:GeoLocationRPT (ad-content page)))



