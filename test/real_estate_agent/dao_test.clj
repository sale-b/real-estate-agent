(ns real-estate-agent.dao-test
  (:require [clojure.test :refer :all]
            [test-helpers :refer [with-database-reset]]
            [real-estate-agent.db.dao :as dao]))

(use-fixtures :each with-database-reset)

(deftest get-user-test
  (testing "should return user wih id 1"
    (let [u (dao/get-user 1)]
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
    (let [u (dao/get-user 2)]
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
