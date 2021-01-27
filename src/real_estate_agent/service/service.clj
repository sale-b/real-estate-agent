(ns real-estate-agent.service.service
  (:require [real-estate-agent.db.dao :as dao]
            [real-estate-agent.util.cast :as cast]
            [environ.core :refer [env]]
            [crypto.password.scrypt :as password]
            [cljts.geom :as geom]
            [cljts.prep :as prep]
            [cljts.relation :as relation]
            )
  (:use ring.util.response))

(def random-uuid #(.toString (java.util.UUID/randomUUID)))

(def page-size 10)

(defn authorized? [token]
  (= 1 (first (dao/update-session-duration-by-id
                (:id (dao/get-unexpired-session token (cast/string-to-long (:session-allowed-inactivity-seconds env))))))))

(defn authorized-identity? [token id]
  (let [session (dao/get-unexpired-session token (cast/string-to-long (:session-allowed-inactivity-seconds env)))]
    (if (= (cast/string-to-long id) (:user_id session))
      (= 1 (first (dao/update-session-duration-by-id (:id session)))))))

(defn get-user
  [id request-headers]
  (if (nil? request-headers)
    (bad-request {:message "Not authorized!"})
    (if (authorized-identity? (request-headers "x-auth-token") id)
      (let [u (dao/get-user-by-id (cast/string-to-long id))]
        (if (empty? u)
          (bad-request {:message "User not found!"})
          (response {:id (:id u) :email (:email u)})))
      (bad-request {:message "Not authorized!"}))))

(defn save-filters
  [request-headers request-body]
  (if (nil? request-headers)
    (bad-request {:message "Not authorized!"})
    (if (authorized-identity? (request-headers "x-auth-token") (request-headers "user-id"))
      (if (> 3 (dao/count-users-saved-filters (cast/string-to-long (request-headers "user-id"))))
        (let [f (dao/insert-saved-filter {:locations             (:selectedLocations (:filters request-body))
                                          :ad_types              (:adType (:filters request-body))
                                          :floors                (:floors (:filters request-body))
                                          :furniture             (:furniture (:filters request-body))
                                          :real_estate_types     (:realEstateType (:filters request-body))
                                          :micro_locations       (:selectedMicroLocations (:filters request-body))
                                          :geolocation           (:coordinates (:filters request-body))
                                          :heating_types         (:heatingType (:filters request-body))
                                          :max_price             (cast/string-to-double (:selectedMaxPrice (:filters request-body)))
                                          :min_price             (cast/string-to-double (:selectedMinPrice (:filters request-body)))
                                          :max_living_space_area (cast/string-to-double (:selectedMaxArea (:filters request-body)))
                                          :min_living_space_area (cast/string-to-double (:selectedMinArea (:filters request-body)))
                                          :max_rooms_number      (cast/string-to-double (:selectedMaxRooms (:filters request-body)))
                                          :min_rooms_number      (cast/string-to-double (:selectedMinRooms (:filters request-body)))
                                          :tittle                (:tittle (:filters request-body))
                                          :has_pictures          (:pictures (:filters request-body))
                                          :email_subscribed      (:subscribed (:filters request-body))

                                          } (cast/string-to-long (request-headers "user-id")))]
          (println f)
          (if (empty? f)
            (bad-request {:message "Error saving filter!"})
            (response f)))
        (bad-request {:message "You can not save more than 3 filters!"}))
      (bad-request {:message "Not authorized!"}))))

(defn register
  [user]
  (let [db-user (dao/get-user-by-email (:email user))]
    (if (nil? db-user)
      (let [token (random-uuid) db-user (dao/insert-user (assoc user :enabled true))]
        (dao/insert-session {:user_id (:id db-user) :token token})
        (header (response (dissoc db-user :password)) "x-auth-token" token))
      (bad-request {:message "E-mail is already registered!"})))
  )


(defn login
  [user]
  (let [db-user (dao/get-user-by-email (:email user))]
    (if (nil? db-user)
      (bad-request {:message "Incorrect username or password!"})
      (if (password/check (:password user) (:password db-user))
        (if (:enabled db-user)
          (let [token (random-uuid)]
            (dao/insert-session {:user_id (:id db-user) :token token})
            (header (response (dissoc db-user :password)) "x-auth-token" token))
          (bad-request {:message "User is blocked!"}))
        (bad-request {:message "Incorrect username or password!"}))
      )))

(defn make-short-description
  [description]
  (if (< 100 (count description))
    (str (subs description 0 101) "...")
    description))

(defn get-ad-by-id
  [id]
  (let [ad (dao/get-real-estate-by-id (cast/string-to-long id))]
    (if (nil? ad)
      (bad-request {:message "Ad is not found"})
      (response ad))))

(defn get-saved-filters-by-user-id
  [user-id request-headers]
  (if (authorized-identity? (request-headers "x-auth-token") user-id)
    (let [saved-filters (dao/get-saved-filter-by-user-id (cast/string-to-long user-id))]
      (if (nil? saved-filters)
        (bad-request {:message "Filters are not found"})
        (response {:savedFilters (vec saved-filters)})))
    (bad-request {:message "Not authorized!"})))


(defn get-saved-filter-by-id
  [id request-headers]
  (if (authorized-identity? (request-headers "x-auth-token") (request-headers "user-id"))
    (let [saved-filter (dao/get-saved-filter-by-id (cast/string-to-long id))]
      (if (nil? saved-filter)
        (bad-request {:message "Filter is not found"})
        (response {:savedFilter
                   {:selectedLocations      (:locations saved-filter)
                    :adType                 (:ad_types saved-filter)
                    :floors                 (:floors saved-filter)
                    :furniture              (:furniture saved-filter)
                    :realEstateType         (:real_estate_types saved-filter)
                    :selectedMicroLocations (:micro_locations saved-filter)
                    :coordinates            (:geolocation saved-filter)
                    :heatingType            (:heating_types saved-filter)
                    :selectedMaxPrice       (:max_price saved-filter)
                    :selectedMinPrice       (:min_price saved-filter)
                    :selectedMaxArea        (:max_living_space_area saved-filter)
                    :selectedMinArea        (:min_living_space_area saved-filter)
                    :selectedMaxRooms       (:max_rooms_number saved-filter)
                    :selectedMinRooms       (:min_rooms_number saved-filter)
                    :tittle                 (:tittle saved-filter)
                    :pictures               (:has_pictures saved-filter)
                    :subscribed             (:email_subscribed saved-filter)
                    }})))
    (bad-request {:message "Not authorized!"})))

(defn delete-saved-filter-by-id
  [id request-headers]
  (if (authorized-identity? (request-headers "x-auth-token") (request-headers "user-id"))
    (let [delete-filter (dao/delete-saved-filter (cast/string-to-long id))]
      (if (= 0 delete-filter)
        (bad-request {:message "Filter is not found"})
        (response {:deleted delete-filter})))
    (bad-request {:message "Not authorized!"})))

(defn get-ads-paged-without-geolocation
  [request]
  (if (> (:page request) 0)
    (response (let [db-page (vec (dao/get-paged-real-estates (:filters request) (- (:page request) 1) page-size))]
                {:ads        (vec (map #(update-in % [:description] make-short-description) db-page))
                 :pagination {:currentPage (:page request)
                              :totalPages  (int (:total_pages (dao/get-total-pages-number (:filters request) page-size)))}
                 }))
    (bad-request {:message "Page number must be higher than zero."})))

(defn coordinates-str-to-vec
  [str-coordinates]
  (sort (into []
        (map #(read-string %)
             (clojure.string/split str-coordinates #",")))))

(defn is-geolocation-satisfied?
  [pol point]
  (println pol)
  (println point)
  (let [prepared-polygon (prep/prepare (geom/polygon (geom/linear-ring (into [] (map #(apply geom/c %) pol))) nil))]
    (relation/contains? prepared-polygon (geom/point (apply geom/c point)))))

(defn get-ads-paged-with-geolocation
  [request]
  (let [ads-satisfying-geolocation (let [total-prefiltered-ads (int (:total_pages (dao/get-total-pages-number (:filters request) 1)))]
                                     (let [prefiltered-ads (dao/get-paged-real-estates (:filters request) (- (:page request) 1) total-prefiltered-ads)]
                                       (for [real-estate prefiltered-ads
                                             :let [geolocation (:geolocation real-estate)]
                                             :when (is-geolocation-satisfied? (:coordinates (:filters request))
                                                                              (coordinates-str-to-vec geolocation))]
                                         real-estate)))]
    (response (let [db-page (take page-size (drop (* (- (:page request) 1) page-size) ads-satisfying-geolocation))]
                {:ads        (vec (map #(update-in % [:description] make-short-description) db-page))
                 :pagination {:currentPage (:page request)
                              :totalPages  (int (Math/ceil (/ (count ads-satisfying-geolocation) page-size)))}
                 }))))


(defn get-ads-paged
  [request]
  (if (empty? (:coordinates (:filters request)))
    (get-ads-paged-without-geolocation request)
    (get-ads-paged-with-geolocation request)
    ))


(defn get-all-form-data
  []
  (response {:locations       (into [] (map #(:location %) (dao/get-all-locations)))
             :microLocations  (into [] (map #(:micro_location %) (dao/get-all-micro-locations)))
             :adTypes         (into [] (map #(:ad_type %) (dao/get-all-ad-types)))
             :realEstateTypes (into [] (map #(:type %) (dao/get-all-real-estate-types)))
             :heatingTypes    (into [] (map #(:heating_type %) (dao/get-all-heating-types)))
             :floors          (into [] (map #(:floor %) (dao/get-all-floors)))
             :furnitureTypes  (into [] (map #(:furniture %) (dao/get-all-furniture-types)))
             }))

(defn get-micro-locations-for-location
  [locations]
  (response {
             :microLocations (into [] (map #(:micro_location %)
                                           (if (> (count (remove clojure.string/blank? (:locations locations))) 0)
                                             (dao/get-all-micro-locations-for-location (remove clojure.string/blank? (:locations locations)))
                                             (dao/get-all-micro-locations))))
             }))
