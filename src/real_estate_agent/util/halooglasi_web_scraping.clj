(ns real-estate-agent.util.halooglasi-web-scraping
  (:require [clojure.data.json :as json]
            [net.cgrand.enlive-html :as html]
            [real-estate-agent.db.dao :as dao]
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

(defn contact-data [page]
  (json/read-str
    (get-substring (some #(and (clojure.string/includes? % "Phone1") %)
                         (map html/text
                              (html/select
                                page
                                [:script]))) "QuidditaEnvironment.CurrentContactData=" ";QuidditaEnvironment")
    :key-fn keyword))

(defn get-tittle [page]
  (:content (:attrs (first (filter #(= (:property (:attrs %)) "og:title")
                                   (html/select page [:head :meta]))))))

(defn get-price [page]
  (:cena_d (:OtherFields (ad-content page))))

(defn get-location [page]
  (:lokacija_s (:OtherFields (ad-content page))))

(defn get-micro-location [page]
  (:mikrolokacija_s (:OtherFields (ad-content page))))

(defn get-ad-type [page]
  (get (:CategoryNames (ad-content page)) 2))

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

(defn get-phone [page]
  (:Phone1 (first (:ContactInfos (:Advertiser (contact-data page))))))

(defn read-ad [page]
  {:tittle            (get-tittle page)
   :price             (get-price page)
   :type              (get-real-estate-type page)
   :ad_type           (get-ad-type page)
   :rooms_number      (let [rn (get-rooms-number page)]
                        (if (= "5+" rn) 6.0 (cast/string-to-double rn)))
   :floor             (get-floor page)
   :description       (get-description page)
   :location          (get-location page)
   :micro_location    (get-micro-location page)
   :geolocation       (get-geolocation page)
   :living_space_area (get-living-space-area page)
   :furniture         (get-furniture page)
   :heating_type      (get-heating page)
   :pictures          (get-pictures page)
   :has_pictures      (> (count (get-pictures page)) 0)
   :advertiser        (get-advertiser page)
   ;:phone             (get-phone page)
   })

(defn get-ads-url-list [page]
  (into []
        (for [a-tag (html/select page [:h3.product-title :a])]
          (str "https://www.halooglasi.com" (get-in a-tag [:attrs :href])))))

(defn get-ads-pages-number [page]
  (:TotalPages (json/read-str
                 (get-substring (some #(and (clojure.string/includes? % "TotalPages") %)
                                      (map html/text
                                           (html/select
                                             page
                                             [:script]))) "QuidditaEnvironment.serverListData=" ";var $aEl")
                 :key-fn keyword)))

(def last-inserted-url "")

(defn scraping-ads-urls [url]
  (loop [page-number 1]
    (when (<= page-number (get-ads-pages-number (html-page url)))
      (doseq [current-url (get-ads-url-list (html-page (str url "&page=" page-number)))]
        (if (= current-url last-inserted-url) (throw (new RuntimeException "Reached last inserted url")))
        (try
          (dao/insert-real-estate
            (assoc (read-ad
                     (html-page current-url))
              :url current-url))
          (catch org.postgresql.util.PSQLException e
            (println (str "CAUGHT EXCEPTION: " (.getMessage e) " " current-url)))))
      (recur (+ page-number 1))))
  )