(ns real-estate-agent.web-scraping-test
  (:require [clojure.test :refer :all])
  (:use real-estate-agent.util.halooglasi-web-scraping
        net.cgrand.enlive-html))

(defn fetch-page
  [file-path]
  (html-resource (java.io.StringReader. (slurp file-path))))

(deftest get-geollocation-test
  (testing "should return geolocaton from a webpage"
    (let [coordinates (get-geolocation (fetch-page "resources\\test_files\\sample-ad.html"))]
    (is (not (nil? coordinates)))
    (is (= "44.809900,20.421300" coordinates))
    )))
