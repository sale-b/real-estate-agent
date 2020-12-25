(ns real_estate_agent.core-test
  (:require [clojure.test :refer :all]
            [test-helpers :refer [with-database-reset]]
            [real_estate_agent.db.core :refer [connection]]))

(use-fixtures :each with-database-reset)

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 1 1))))
