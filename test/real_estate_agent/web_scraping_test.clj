(ns real-estate-agent.web-scraping-test
  (:require [clojure.test :refer :all])
  (:use real-estate-agent.util.halooglasi-web-scraping
        net.cgrand.enlive-html))

(defn fetch-page
  [file-path]
  (html-resource (java.io.StringReader. (slurp file-path))))

(deftest get-tittle-test
  (testing "should return ad tittle from a webpage"
    (let [tittle (get-tittle (fetch-page "resources\\test_files\\sample-ad.html"))]
      (is (= "Novi Beograd-Arena-Blok 25-130m2-Lux-Uknjižen ID#1" tittle))
      )))

(deftest get-geolocation-test
  (testing "should return geolocaton from a webpage"
    (let [coordinates (get-geolocation (fetch-page "resources\\test_files\\sample-ad.html"))]
    (is (= "44.809900,20.421300" coordinates))
    )))

(deftest get-phone-test
  (testing "should return phone from a webpage"
    (let [phone (get-phone (fetch-page "resources\\test_files\\sample-ad.html"))]
      (is (= "065/662-3984" phone))
      )))

(deftest get-price-test
  (testing "should return ad price from a webpage"
    (let [price (get-price (fetch-page "resources\\test_files\\sample-ad.html"))]
      (is (= 260000.0 price))
      )))

(deftest get-location-test
  (testing "should return ad location from a webpage"
    (let [location (get-location (fetch-page "resources\\test_files\\sample-ad.html"))]
      (is (= "Opština Novi Beograd" location))
      )))

(deftest get-micro-location-test
  (testing "should return ad micro location from a webpage"
    (let [micro-location (get-micro-location (fetch-page "resources\\test_files\\sample-ad.html"))]
      (is (= "Arena" micro-location))
      )))

(deftest get-living-space-area-test
  (testing "should return ad living-space-area from a webpage"
    (let [living-space-area (get-living-space-area (fetch-page "resources\\test_files\\sample-ad.html"))]
      (is (= 130.0 living-space-area))
      )))

(deftest get-advertiser-test
  (testing "should return ad advertiser from a webpage"
    (let [advertiser (get-advertiser (fetch-page "resources\\test_files\\sample-ad.html"))]
      (is (= "Agencija" advertiser))
      )))

(deftest get-real-estate-type-test
  (testing "should return ad real-estate-type from a webpage"
    (let [real-estate-type (get-real-estate-type (fetch-page "resources\\test_files\\sample-ad.html"))]
      (is (= "Stan" real-estate-type))
      )))

(deftest get-living-space-area-test
  (testing "should return ad living-space-area from a webpage"
    (let [living-space-area (get-living-space-area (fetch-page "resources\\test_files\\sample-ad.html"))]
      (is (= 130.0 living-space-area))
      )))

(deftest get-rooms-number-test
  (testing "should return ad rooms-number from a webpage"
    (let [rooms-number (get-rooms-number (fetch-page "resources\\test_files\\sample-ad.html"))]
      (is (= "3.5" rooms-number))
      )))

(deftest get-floor-test
  (testing "should return ad floor from a webpage"
    (let [floor (get-floor (fetch-page "resources\\test_files\\sample-ad.html"))]
      (is (= "7" floor))
      )))

(deftest get-furniture-test
  (testing "should return ad furniture from a webpage"
    (let [furniture (get-furniture (fetch-page "resources\\test_files\\sample-ad.html"))]
      (is (nil? furniture))
      )))

(deftest get-heating-test
  (testing "should return ad heating from a webpage"
    (let [heating (get-heating (fetch-page "resources\\test_files\\sample-ad.html"))]
      (is (= "EG" heating))
      )))

(deftest get-text-test
  (testing "should return ad text from a webpage"
    (let [text (get-description (fetch-page "resources\\test_files\\sample-ad.html"))]
      (is (= (str "Stan na dobroj lokaciji. Po strukturi troiposoban, dvostrano orjentisan, dva mokra cvora, svetao i komforan. "
                  "Kompletno renoviran i ne zahteva dodatna ulaganja. Uknjizen na 110m2 ali mereno sa terasamo stan ima 130m2.  "
                  "Uknjizen. Za preporuku. Agancijska provizija 2%. Za sve dodatne informacije, molimo da nas kontaktirate.,") text))
      )))

(deftest get-pictures-test
  (testing "should return ad pictures from a webpage"
    (let [pictures (get-pictures (fetch-page "resources\\test_files\\sample-ad.html"))]
      (is (= ["/slike/oglasi/Thumbs/201227/l/novi-beograd-arena-blok-25-130m2-lux-uknjizen-5425636159803-71793910130.jpg"
               "/slike/oglasi/Thumbs/201227/l/novi-beograd-arena-blok-25-130m2-lux-uknjizen-5425636159803-71793910131.jpg"
               "/slike/oglasi/Thumbs/201227/l/novi-beograd-arena-blok-25-130m2-lux-uknjizen-5425636159803-71793910132.jpg"
               "/slike/oglasi/Thumbs/201227/l/novi-beograd-arena-blok-25-130m2-lux-uknjizen-5425636159803-71793910133.jpg"
               "/slike/oglasi/Thumbs/201227/l/novi-beograd-arena-blok-25-130m2-lux-uknjizen-5425636159803-71793910134.jpg"
               "/slike/oglasi/Thumbs/201227/l/novi-beograd-arena-blok-25-130m2-lux-uknjizen-5425636159803-71793910135.jpg"
               "/slike/oglasi/Thumbs/201227/l/novi-beograd-arena-blok-25-130m2-lux-uknjizen-5425636159803-71793910136.jpg"
               "/slike/oglasi/Thumbs/201227/l/novi-beograd-arena-blok-25-130m2-lux-uknjizen-5425636159803-71793910137.jpg"
               "/slike/oglasi/Thumbs/201227/l/novi-beograd-arena-blok-25-130m2-lux-uknjizen-5425636159803-71793910138.jpg"
               "/slike/oglasi/Thumbs/201227/l/novi-beograd-arena-blok-25-130m2-lux-uknjizen-5425636159803-71793910139.jpg"]
             pictures))
      )))


(deftest read-ad-test
  (testing "should return ad from a webpage"
    (let [ad (read-ad (fetch-page "resources\\test_files\\sample-ad.html"))]
      (is (= "Novi Beograd-Arena-Blok 25-130m2-Lux-Uknjižen ID#1" (:tittle ad)))
      (is (= "44.809900,20.421300" (:geolocation ad)))
      (is (= 260000.0 (:price ad)))
      (is (= "Opština Novi Beograd" (:location ad)))
      (is (= "Arena" (:micro-location ad)))
      (is (= 130.0 (:living_space_area ad)))
      (is (= "Agencija" (:advertiser ad)))
      (is (= "Stan" (:type ad)))
      (is (= "3.5" (:rooms_number ad)))
      (is (= "7" (:floor ad)))
      (is (nil? (:furniture ad)))
      (is (= "EG" (:heating_type ad)))
      (is (= "065/662-3984" (:phone ad)))
      )))

(deftest read-ads-list-test
  (testing "should return list of ads url-s from a page"
    (let [ad (get-ads-url-list (fetch-page "resources\\test_files\\ads-list-page.html"))]
      (is (= 20 (count ad)))
      (is (= ["https://www.halooglasi.com/nekretnine/prodaja-stanova/vozdovac-vojvode-stepe-1-0-35m2-fantastican-p/5425636119609?kid=4"
              "https://www.halooglasi.com/nekretnine/prodaja-stanova/zvezdara-pancina-2-0-64m2-bez-ag-provizije/5425636143093?kid=4"
              "https://www.halooglasi.com/nekretnine/prodaja-stanova/vracar-vojvode-hrvoja-14-novogradnja-78-m2/5425636079697?kid=4"
              "https://www.halooglasi.com/nekretnine/prodaja-stanova/vidikovac-novogradnja/5425636150288?kid=4"
              "https://www.halooglasi.com/nekretnine/prodaja-stanova/zemun-dvosoban-65m2-funkcionalan-renoviran-mi/5425635968228?kid=4"
              "https://www.halooglasi.com/nekretnine/prodaja-stanova/cetvorosoban-stan-kod-hrama-svetog-save/5425636147466?kid=4"
              "https://www.halooglasi.com/nekretnine/prodaja-stanova/novi-beograd-1-5-lux-namesten-sa-garazom-id1/5425636144211?kid=4"
              "https://www.halooglasi.com/nekretnine/prodaja-stanova/kapije-zlatibora-vila-gruda-lux-apartman/5425636168980?kid=4"
              "https://www.halooglasi.com/nekretnine/prodaja-stanova/kapije-zlatibora-vila-gradina-lux-apartmani/5425636168972?kid=4"
              "https://www.halooglasi.com/nekretnine/prodaja-stanova/kapije-zlatibora-vila-gradina-lux-apartmani/5425636168962?kid=4"
              "https://www.halooglasi.com/nekretnine/prodaja-stanova/dvosoban-stan-blok-70a---delta-city-novi-beog/5425636088499?kid=4"
              "https://www.halooglasi.com/nekretnine/prodaja-stanova/novogradnja-lekino-brdo-cena-sa-pdv-om/5425635735778?kid=4"
              "https://www.halooglasi.com/nekretnine/prodaja-stanova/kapije-zlatibora-vila-gradina---lux-apartmani/5425636168957?kid=4"
              "https://www.halooglasi.com/nekretnine/prodaja-stanova/stari-grad-centar---obilicev-venac/5425636051976?kid=4"
              "https://www.halooglasi.com/nekretnine/prodaja-stanova/savski-venac-beograd-na-vodi---parkview/5425635281934?kid=4"
              "https://www.halooglasi.com/nekretnine/prodaja-stanova/dvoiposoban-stan/5425636010977?kid=4"
              "https://www.halooglasi.com/nekretnine/prodaja-stanova/troiposoban-stan/5425636011343?kid=4"
              "https://www.halooglasi.com/nekretnine/prodaja-stanova/troiposoban-stan/5425636011334?kid=4"
              "https://www.halooglasi.com/nekretnine/prodaja-stanova/troiposoban-stan/5425636011245?kid=4"
              "https://www.halooglasi.com/nekretnine/prodaja-stanova/dvoiposoban-stan/5425636010970?kid=4"] ad))
      )))

(deftest get-pages-number-test
  (testing "should return number of pages with ads"
    (let [pages-number (get-ads-pages-number (fetch-page "resources\\test_files\\ads-list-page.html"))]
      (is (= 1513 pages-number))
      )))

;(deftest sss-test
;  (testing "should return number of pages with ads"
;    (scraping-ads-urls "https://www.halooglasi.com/nekretnine/prodaja-stanova?oglasivac_nekretnine_id_l=387237")
;    ))