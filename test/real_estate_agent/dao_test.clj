(ns real-estate-agent.dao-test
  (:require [clojure.test :refer :all]
            [test-helpers :refer [with-database-reset]]
            [real-estate-agent.db.dao :as dao]))

(use-fixtures :each with-database-reset)

(deftest insert-user-test
  (testing "should insert a new user into db and assign him id, created_on, modified on"
    (let [user {:username "test",
                :password "test",
                :email    "test@test.com",
                :enabled  false}
          ]
      (let [db-response (dao/insert-user user)]
        (is (not (nil? db-response)))
        (is (not (nil? (:id db-response))))
        (let [db-user (dao/get-user-by-id (:id db-response))]
          (is (not (nil? db-user)))
          (is (= "test" (:username db-user)))
          (is (= "test" (:password db-user)))
          (is (= "test@test.com" (:email db-user)))
          (is (= false (:enabled db-user)))
          (is (not (nil? (:created_on db-user))))
          (is (not (nil? (:modified_on db-user))))
          ))))
  )

(deftest get-user-test
  (testing "should return user wih id 1"
    (let [u (dao/get-user-by-id 1)]
      (is (not (empty? u)))
      (is (not (nil? u)))
      (is (= 1 (:id u)))
      (is (= "admin" (:username u)))
      (is (= "admin" (:password u)))
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
      (is (= "user" (:username u)))
      (is (= "user" (:password u)))
      (is (= "user@user.com" (:email u)))
      (is (= true (:enabled u)))
      (is (= #inst "2020-12-28T15:09:17.437000000-00:00" (:created_on u)))
      (is (not (= #inst "2020-12-28T15:09:17.437000000-00:00" (:modified_on u))))
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
      (is (= "admin" (:username u)))
      (is (= "password" (:password u)))
      (is (= "admin@admin.com" (:email u)))
      (is (= true (:enabled u)))
      (is (= #inst "2020-12-28T15:09:16.437000000-00:00" (:created_on u)))
      (is (not (= #inst "2020-12-28T15:09:16.437000000-00:00" (:modified_on u))))
      ))
  )

(deftest delete-user-test
  (testing "should delete user from db"
    (let [u (dao/get-user-by-id 1)]
      (is (= '(1) (dao/delete-user (assoc u :password "password"))))
      )
    (let [u (dao/get-user-by-id 1)]
      (is (nil? u))
      ))
  )
