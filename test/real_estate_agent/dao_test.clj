(ns real-estate-agent.dao-test
  (:require [clojure.test :refer :all]
            [test-helpers :refer [with-database-reset]]
            [real-estate-agent.db.dao :as dao]
            [crypto.password.scrypt :as password]))

(use-fixtures :each with-database-reset)

(deftest insert-user-test
  (testing "should insert a new user into db and assign him id, created_on, modified on"
    (let [user {:password "test",
                :email    "test@test.com",
                :enabled  false}
          ]
      (let [db-response (dao/insert-user user)]
        (is (not (nil? db-response)))
        (is (not (nil? (:id db-response))))
        (let [db-user (dao/get-user-by-id (:id db-response))]
          (is (not (nil? db-user)))
          (is (password/check "test" (:password db-user)))
          (is (= "test@test.com" (:email db-user)))
          (is (= false (:enabled db-user)))
          (is (not (nil? (:created_on db-user))))
          (is (not (nil? (:modified_on db-user))))
          )))))

(deftest get-user-by-id-test
  (testing "should return user wih id 1"
    (let [u (dao/get-user-by-id 1)]
      (is (not (empty? u)))
      (is (not (nil? u)))
      (is (= 1 (:id u)))
      (is (password/check "admin" (:password u)))
      (is (= "admin@admin.com" (:email u)))
      (is (= true (:enabled u)))
      (is (= #inst "2020-12-28T15:09:16.437000000-00:00" (:created_on u)))
      (is (= #inst "2020-12-28T15:09:16.437000000-00:00" (:modified_on u)))
      ))
  (testing "should return user wih id 2"
    (let [u (dao/get-user-by-id 2)]
      (is (not (empty? u)))
      (is (not (nil? u)))
      (is (= 2 (:id u)))
      (is (password/check "user" (:password u)))
      (is (= "user@user.com" (:email u)))
      (is (= true (:enabled u)))
      (is (= #inst "2020-12-28T15:09:17.437000000-00:00" (:created_on u)))
      (is (not (= #inst "2020-12-28T15:09:17.437000000-00:00" (:modified_on u))))
      )))

(deftest get-user-by-email-test
  (testing "should return user wih email admin@admin.com"
    (let [u (dao/get-user-by-email "admin@admin.com")]
      (is (not (empty? u)))
      (is (not (nil? u)))
      (is (= 1 (:id u)))
      (is (password/check "admin" (:password u)))
      (is (= "admin@admin.com" (:email u)))
      (is (= true (:enabled u)))
      (is (= #inst "2020-12-28T15:09:16.437000000-00:00" (:created_on u)))
      (is (= #inst "2020-12-28T15:09:16.437000000-00:00" (:modified_on u)))
      )))

(deftest update-user-test
  (testing "should return user where id = 1 with updated password and modified_on timestamp"
    (let [u (dao/get-user-by-id 1)]
      (is (= '(1) (dao/update-user (assoc u :password "password"))))
      )
    (let [u (dao/get-user-by-id 1)]
      (is (not (empty? u)))
      (is (not (nil? u)))
      (is (= 1 (:id u)))
      (is (= "password" (:password u)))
      (is (= "admin@admin.com" (:email u)))
      (is (= true (:enabled u)))
      (is (= #inst "2020-12-28T15:09:16.437000000-00:00" (:created_on u)))
      (is (not (= #inst "2020-12-28T15:09:16.437000000-00:00" (:modified_on u))))
      )))

(deftest delete-user-test
  (testing "should delete user from db"
    (let [u (dao/get-user-by-id 1)]
      (is (= '(1) (dao/delete-user (assoc u :password "password"))))
      )
    (let [u (dao/get-user-by-id 1)]
      (is (nil? u))
      )))

(deftest insert-session-test
  (testing "should insert a new session into db, retrieve that session and update last_used timestamp"
    (let [session {:user_id 2,
                   :token   "bdfff95c-3803-4874-b70c-2f2adc69a2ab"}
          ]
      (let [db-response (dao/insert-session session)]
        (is (not (nil? db-response)))
        (is (not (nil? (:id db-response))))
        ;session is unexpired if it is under 50 seconds old
        (let [db-session (dao/get-unexpired-session "bdfff95c-3803-4874-b70c-2f2adc69a2ab" 50)]
          (is (not (nil? db-session)))
          (is (= 2 (:user_id db-session)))
          (is (= "bdfff95c-3803-4874-b70c-2f2adc69a2ab" (:token db-session)))
          (is (not (nil? (:last_used db-session))))
          (dao/update-session-duration-by-id (:id db-session))
          (let [updated-db-session (dao/get-unexpired-session "bdfff95c-3803-4874-b70c-2f2adc69a2ab" 50)]
            (is (not (nil? updated-db-session)))
            (is (= 2 (:user_id db-session)))
            (is (= "bdfff95c-3803-4874-b70c-2f2adc69a2ab" (:token db-session)))
            (is (< 0 (- (inst-ms (:last_used updated-db-session)) (inst-ms (:last_used db-session)))))
            )
          ;expectiong nil because there are no expired sessions with this token
          (is (nil? (dao/get-unexpired-session "bdfff95c-3803-4874-b70c-2f2adc69a2ab" -5)))
          )))))

(deftest insert-real-estate-test
  (testing "should insert a new real estate into db with related pictures in transaction and assign it id, created_on, modified on"
    (let [real-estate {:url               "www.test.com"
                       :description       (str "Stan na dobroj lokaciji. Po strukturi troiposoban, dvostrano orjentisan, dva mokra cvora, svetao i komforan. "
                                               "Kompletno renoviran i ne zahteva dodatna ulaganja. Uknjizen na 110m2 ali mereno sa terasamo stan ima 130m2.  "
                                               "Uknjizen. Za preporuku. Agancijska provizija 2%. Za sve dodatne informacije, molimo da nas kontaktirate.,")
                       :living_space_area 130.0
                       :geolocation       "44.809900,20.421300"
                       :location          "Opština Novi Beograd"
                       :micro_location    "Arena"
                       :furniture         nil
                       :advertiser        "Agencija"
                       :rooms_number      3.5
                       :tittle            "Novi Beograd-Arena-Blok 25-130m2-Lux-Uknjižen ID#1"
                       :type              "Stan"
                       :pictures          ["/slike/oglasi/Thumbs/201227/l/novi-beograd-arena-blok-25-130m2-lux-uknjizen-5425636159803-71793910130.jpg"
                                           "/slike/oglasi/Thumbs/201227/l/novi-beograd-arena-blok-25-130m2-lux-uknjizen-5425636159803-71793910131.jpg"
                                           "/slike/oglasi/Thumbs/201227/l/novi-beograd-arena-blok-25-130m2-lux-uknjizen-5425636159803-71793910132.jpg"
                                           "/slike/oglasi/Thumbs/201227/l/novi-beograd-arena-blok-25-130m2-lux-uknjizen-5425636159803-71793910133.jpg"
                                           "/slike/oglasi/Thumbs/201227/l/novi-beograd-arena-blok-25-130m2-lux-uknjizen-5425636159803-71793910134.jpg"
                                           "/slike/oglasi/Thumbs/201227/l/novi-beograd-arena-blok-25-130m2-lux-uknjizen-5425636159803-71793910135.jpg"
                                           "/slike/oglasi/Thumbs/201227/l/novi-beograd-arena-blok-25-130m2-lux-uknjizen-5425636159803-71793910136.jpg"
                                           "/slike/oglasi/Thumbs/201227/l/novi-beograd-arena-blok-25-130m2-lux-uknjizen-5425636159803-71793910137.jpg"
                                           "/slike/oglasi/Thumbs/201227/l/novi-beograd-arena-blok-25-130m2-lux-uknjizen-5425636159803-71793910138.jpg"
                                           "/slike/oglasi/Thumbs/201227/l/novi-beograd-arena-blok-25-130m2-lux-uknjizen-5425636159803-71793910139.jpg"]
                       :price             260000.0
                       :phone             "061/33-33-33"
                       :floor             "7"
                       :heating_type      "EG"}
          ]
      (let [db-response (dao/insert-real-estate real-estate)]
        (is (not (nil? db-response)))
        (is (not (nil? (:id db-response))))
        (let [db-real-estate (dao/get-real-estate-by-id (:id db-response))]
          (is (not (nil? db-real-estate)))
          (is (= (str "Stan na dobroj lokaciji. Po strukturi troiposoban, dvostrano orjentisan, dva mokra cvora, svetao i komforan. "
                      "Kompletno renoviran i ne zahteva dodatna ulaganja. Uknjizen na 110m2 ali mereno sa terasamo stan ima 130m2.  "
                      "Uknjizen. Za preporuku. Agancijska provizija 2%. Za sve dodatne informacije, molimo da nas kontaktirate.,") (:description db-real-estate)))
          (is (== 130.0 (:living_space_area db-real-estate)))
          (is (= "44.809900,20.421300" (:geolocation db-real-estate)))
          (is (= "Opština Novi Beograd" (:location db-real-estate)))
          (is (= "Arena" (:micro_location db-real-estate)))
          (is (= nil (:furniture db-real-estate)))
          ;its not stored in db since we are collecting only owners ads
          (is (= nil (:advertiser db-real-estate)))
          (is (== 3.5 (:rooms_number db-real-estate)))
          (is (= "Novi Beograd-Arena-Blok 25-130m2-Lux-Uknjižen ID#1" (:tittle db-real-estate)))
          (is (= "Stan" (:type db-real-estate)))
          (is (= ["https://img.halooglasi.com/slike/oglasi/Thumbs/201227/l/novi-beograd-arena-blok-25-130m2-lux-uknjizen-5425636159803-71793910130.jpg"
                  "https://img.halooglasi.com/slike/oglasi/Thumbs/201227/l/novi-beograd-arena-blok-25-130m2-lux-uknjizen-5425636159803-71793910131.jpg"
                  "https://img.halooglasi.com/slike/oglasi/Thumbs/201227/l/novi-beograd-arena-blok-25-130m2-lux-uknjizen-5425636159803-71793910132.jpg"
                  "https://img.halooglasi.com/slike/oglasi/Thumbs/201227/l/novi-beograd-arena-blok-25-130m2-lux-uknjizen-5425636159803-71793910133.jpg"
                  "https://img.halooglasi.com/slike/oglasi/Thumbs/201227/l/novi-beograd-arena-blok-25-130m2-lux-uknjizen-5425636159803-71793910134.jpg"
                  "https://img.halooglasi.com/slike/oglasi/Thumbs/201227/l/novi-beograd-arena-blok-25-130m2-lux-uknjizen-5425636159803-71793910135.jpg"
                  "https://img.halooglasi.com/slike/oglasi/Thumbs/201227/l/novi-beograd-arena-blok-25-130m2-lux-uknjizen-5425636159803-71793910136.jpg"
                  "https://img.halooglasi.com/slike/oglasi/Thumbs/201227/l/novi-beograd-arena-blok-25-130m2-lux-uknjizen-5425636159803-71793910137.jpg"
                  "https://img.halooglasi.com/slike/oglasi/Thumbs/201227/l/novi-beograd-arena-blok-25-130m2-lux-uknjizen-5425636159803-71793910138.jpg"
                  "https://img.halooglasi.com/slike/oglasi/Thumbs/201227/l/novi-beograd-arena-blok-25-130m2-lux-uknjizen-5425636159803-71793910139.jpg"] (:pictures db-real-estate)))
          (is (== 260000.0 (:price db-real-estate)))
          (is (= "061/33-33-33" (:phone db-real-estate)))
          (is (= "7" (:floor db-real-estate)))
          (is (= "EG" (:heating_type db-real-estate)))
          (is (not (nil? (:created_on db-real-estate))))
          (is (not (nil? (:modified_on db-real-estate))))
          )
        ))))

(deftest find-last-real-estate-test
  (testing "should return last inserted ad"
    (let [db-real-estate-last (dao/get-last-inserted-real-estate)]
      (is (not (nil? db-real-estate-last)))
      (is (= "www.last.com" (:url db-real-estate-last)))
      (is (= "Stan na dobroj lokaciji. Po strukturi dvoiposoban, dvostrano orjentisan..." (:description db-real-estate-last)))
      (is (== 60.0 (:living_space_area db-real-estate-last)))
      (is (= "44.123123,20.123456" (:geolocation db-real-estate-last)))
      (is (= "Opština Vračаr" (:location db-real-estate-last)))
      (is (= "Franš" (:micro_location db-real-estate-last)))
      (is (= nil (:furniture db-real-estate-last)))
      ;its not stored in db since we are collecting only owners ads
      (is (= nil (:advertiser db-real-estate-last)))
      (is (== 2.5 (:rooms_number db-real-estate-last)))
      (is (= "Vracar povoljno" (:tittle db-real-estate-last)))
      (is (= "Stan" (:type db-real-estate-last)))
      (is (= nil (:pictures db-real-estate-last)))
      (is (== 250.0 (:price db-real-estate-last)))
      (is (= "062/222-22-22" (:phone db-real-estate-last)))
      (is (= "VPR" (:floor db-real-estate-last)))
      (is (= "CG" (:heating_type db-real-estate-last)))
      (is (not (nil? (:created_on db-real-estate-last))))
      (is (not (nil? (:modified_on db-real-estate-last))))
      )))

(deftest get-all-locations-test
  (testing "should return all ads unique locations"
    (let [locations (dao/get-all-locations)]
      (is (not (empty? locations)))
      (is (not (nil? locations)))
      (is (= "Opština Novi Beograd" (:location (first locations))))
      (is (= 2 (count locations)))
      )))

(deftest get-all-micro-locations-test
  (testing "should return all ads unique micro locations"
    (let [micro-locations (dao/get-all-micro-locations)]
      (is (not (empty? micro-locations)))
      (is (not (nil? micro-locations)))
      (is (= 2 (count micro-locations)))
      (is (= "Arena" (:micro_location (first micro-locations))))
      (is (= "Franš" (:micro_location (second micro-locations))))
      )))

(deftest get-ads-with-price-higher-than-test
  (testing "should return ads with price higher than provided ordered by id desc"
    (let [ads (dao/get-paged-real-estates
                {:priceHigher 105}
                0)]
      (is (not (empty? ads)))
      (is (not (nil? ads)))
      (is (= 1 (count ads)))
      (is (< 105 (:price (first ads))))
      (is (= 2 (:id (first ads))))
      )
    (let [ads (dao/get-paged-real-estates
                {:priceHigher 5}
                0)]
      (is (not (empty? ads)))
      (is (not (nil? ads)))
      (is (= 2 (count ads)))
      (is (< 5 (:price (first ads))))
      (is (= 2 (:id (first ads))))
      (is (< 5 (:price (second ads))))
      (is (= 1 (:id (second ads))))
      )))

(deftest get-ads-with-price-les-than-test
  (testing "should return ads with price les than provided ordered by id desc"
    (let [ads (dao/get-paged-real-estates
                {:priceLes 105}
                0)]
      (is (not (empty? ads)))
      (is (not (nil? ads)))
      (is (= 1 (count ads)))
      (is (> 105 (:price (first ads))))
      (is (= 1 (:id (first ads))))
      )
    (let [ads (dao/get-paged-real-estates
                {:priceLes 260}
                0)]
      (is (not (empty? ads)))
      (is (not (nil? ads)))
      (is (= 2 (count ads)))
      (is (> 260 (:price (first ads))))
      (is (= 2 (:id (first ads))))
      (is (> 260 (:price (second ads))))
      (is (= 1 (:id (second ads))))
      )))

(deftest get-ads-with-price-higher-than-test
  (testing "should return ads with price higher than provided ordered by id desc"
    (let [ads (dao/get-paged-real-estates
                {:priceHigher 105}
                0)]
      (is (not (empty? ads)))
      (is (not (nil? ads)))
      (is (= 1 (count ads)))
      (is (< 105 (:price (first ads))))
      (is (= 2 (:id (first ads))))
      )
    (let [ads (dao/get-paged-real-estates
                {:priceHigher 5}
                0)]
      (is (not (empty? ads)))
      (is (not (nil? ads)))
      (is (= 2 (count ads)))
      (is (< 5 (:price (first ads))))
      (is (= 2 (:id (first ads))))
      (is (< 5 (:price (second ads))))
      (is (= 1 (:id (second ads))))
      )))

(deftest get-ads-with-space-area-les-than-test
  (testing "should return ads with space area les than provided ordered by id desc"
    (let [ads (dao/get-paged-real-estates
                {:spaceAreaLes 55}
                0)]
      (is (not (empty? ads)))
      (is (not (nil? ads)))
      (is (= 1 (count ads)))
      (is (> 55 (:living_space_area (first ads))))
      (is (= 1 (:id (first ads))))
      )
    (let [ads (dao/get-paged-real-estates
                {:spaceAreaLes 70}
                0)]
      (is (not (empty? ads)))
      (is (not (nil? ads)))
      (is (= 2 (count ads)))
      (is (> 70 (:living_space_area (first ads))))
      (is (= 2 (:id (first ads))))
      (is (> 70 (:living_space_area (second ads))))
      (is (= 1 (:id (second ads))))
      )))


(deftest get-ads-with-space-area-higher-than-test
  (testing "should return ads with space area higher than provided ordered by id desc"
    (let [ads (dao/get-paged-real-estates
                {:spaceAreaHigher 55}
                0)]
      (is (not (empty? ads)))
      (is (not (nil? ads)))
      (is (= 1 (count ads)))
      (is (> 105 (:living_space_area (first ads))))
      (is (= 2 (:id (first ads))))
      )
    (let [ads (dao/get-paged-real-estates
                {:spaceAreaHigher 35}
                0)]
      (is (not (empty? ads)))
      (is (not (nil? ads)))
      (is (= 2 (count ads)))
      (is (< 35 (:living_space_area (first ads))))
      (is (= 2 (:id (first ads))))
      (is (< 35 (:living_space_area (second ads))))
      (is (= 1 (:id (second ads))))
      )))

(deftest get-ads-with-rooms-number-les-than-test
  (testing "should return ads with rooms number les than provided ordered by id desc"
    (let [ads (dao/get-paged-real-estates
                {:roomsNumberLes 3.0}
                0)]
      (is (not (empty? ads)))
      (is (not (nil? ads)))
      (is (= 1 (count ads)))
      (is (> 3.0 (:rooms_number (first ads))))
      (is (= 2 (:id (first ads))))
      )
    (let [ads (dao/get-paged-real-estates
                {:roomsNumberLes 4.5}
                0)]
      (is (not (empty? ads)))
      (is (not (nil? ads)))
      (is (= 2 (count ads)))
      (is (> 4.5 (:rooms_number (first ads))))
      (is (= 2 (:id (first ads))))
      (is (> 4.5 (:rooms_number (second ads))))
      (is (= 1 (:id (second ads))))
      )))


(deftest get-ads-with-rooms-number-higher-than-test
  (testing "should return ads with rooms number higher than provided ordered by id desc"
    (let [ads (dao/get-paged-real-estates
                {:roomsNumberHigher 3.0}
                0)]
      (is (not (empty? ads)))
      (is (not (nil? ads)))
      (is (= 1 (count ads)))
      (is (< 3.0 (:rooms_number (first ads))))
      (is (= 1 (:id (first ads))))
      )
    (let [ads (dao/get-paged-real-estates
                {:roomsNumberHigher 1.5}
                0)]
      (is (not (empty? ads)))
      (is (not (nil? ads)))
      (is (= 2 (count ads)))
      (is (< 1.5 (:rooms_number (first ads))))
      (is (= 2 (:id (first ads))))
      (is (< 1.5 (:rooms_number (second ads))))
      (is (= 1 (:id (second ads))))
      )))

(deftest get-ads-with-location-test
  (testing "should return ads on provided locations ordered by id desc"
    (let [ads (dao/get-paged-real-estates
                {:location ["Opština Vračаr"]}
                0)]
      (is (not (empty? ads)))
      (is (not (nil? ads)))
      (is (= 1 (count ads)))
      (is (= "Opština Vračаr" (:location (first ads))))
      (is (= 2 (:id (first ads))))
      )
    (let [ads (dao/get-paged-real-estates
                {:location ["Opština Vračаr" "Opština Novi Beograd"]}
                0)]
      (is (not (empty? ads)))
      (is (not (nil? ads)))
      (is (= 2 (count ads)))
      (is (= "Opština Vračаr" (:location (first ads))))
      (is (= 2 (:id (first ads))))
      (is (= "Opština Novi Beograd" (:location (second ads))))
      (is (= 1 (:id (second ads))))
      )
    (let [ads (dao/get-paged-real-estates
                {}
                0)]
      (is (not (empty? ads)))
      (is (not (nil? ads)))
      (is (= 2 (count ads)))
      (is (= "Opština Vračаr" (:location (first ads))))
      (is (= 2 (:id (first ads))))
      (is (= "Opština Novi Beograd" (:location (second ads))))
      (is (= 1 (:id (second ads))))
      )
    (let [ads (dao/get-paged-real-estates
                {:location nil}
                0)]
      (is (not (empty? ads)))
      (is (not (nil? ads)))
      (is (= 2 (count ads)))
      (is (= "Opština Vračаr" (:location (first ads))))
      (is (= 2 (:id (first ads))))
      (is (= "Opština Novi Beograd" (:location (second ads))))
      (is (= 1 (:id (second ads))))
      )
    ))

(deftest get-ads-with-micro-location-test
  (testing "should return ads on provided micro locations ordered by id desc"
    (let [ads (dao/get-paged-real-estates
                {:microLocation ["Franš"]}
                0)]
      (is (not (empty? ads)))
      (is (not (nil? ads)))
      (is (= 1 (count ads)))
      (is (= "Franš" (:micro_location (first ads))))
      (is (= 2 (:id (first ads))))
      )
    (let [ads (dao/get-paged-real-estates
                {:microLocation ["Arena" "Franš"]}
                0)]
      (is (not (empty? ads)))
      (is (not (nil? ads)))
      (is (= 2 (count ads)))
      (is (= "Franš" (:micro_location (first ads))))
      (is (= 2 (:id (first ads))))
      (is (= "Arena" (:micro_location (second ads))))
      (is (= 1 (:id (second ads))))
      )
    (let [ads (dao/get-paged-real-estates
                {}
                0)]
      (is (not (empty? ads)))
      (is (not (nil? ads)))
      (is (= 2 (count ads)))
      (is (= "Franš" (:micro_location (first ads))))
      (is (= 2 (:id (first ads))))
      (is (= "Arena" (:micro_location (second ads))))
      (is (= 1 (:id (second ads))))
      )
    (let [ads (dao/get-paged-real-estates
                {:microLocation nil}
                0)]
      (is (not (empty? ads)))
      (is (not (nil? ads)))
      (is (= 2 (count ads)))
      (is (= "Franš" (:micro_location (first ads))))
      (is (= 2 (:id (first ads))))
      (is (= "Arena" (:micro_location (second ads))))
      (is (= 1 (:id (second ads))))
      )
    ))