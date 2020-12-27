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
    )))
