(ns real-estate-agent.util.halooglasi-web-scraping
  (:require [clojure.data.json :as json]
            [net.cgrand.enlive-html :as html]
            [real-estate-agent.util.cast :as cast])
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
  (cast/string-to-double (:broj_soba_s (:OtherFields (ad-content page)))))

(defn get-floor [page]
  (cast/string-to-int (:sprat_s (:OtherFields (ad-content page)))))

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

(defn read-ad [page]
  {:tittle (get-tittle page)
   :price (get-price page)
   :type (get-real-estate-type page)
   :rooms_number (get-rooms-number page)
   :floor (get-floor page)
   :description (get-description page)
   :geolocation (get-geolocation page)
   :living_space_area (get-living-space-area page)
   :furniture (get-furniture page)
   :heating_type (get-heating page)
   :pictures (get-pictures page)
   :advertiser (get-advertiser page)
   })



