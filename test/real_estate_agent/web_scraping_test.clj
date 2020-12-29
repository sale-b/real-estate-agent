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

(deftest get-price-test
  (testing "should return ad price from a webpage"
    (let [price (get-price (fetch-page "resources\\test_files\\sample-ad.html"))]
      (is (= 260000.0 price))
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



