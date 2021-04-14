(ns metis.model.core-test
  (:require [clojure.test :refer :all]
            [metis.model.core :refer :all]))

(deftest map->safe-map-i
  (testing "nil behaviour"
    (is (nil? (map->safe-map nil))))
  (testing "empty"
    (is (empty? (map->safe-map {}))))
  (testing "simple keep"
    (is (= {:a "@123"}  (map->safe-map {:a "@123"}))))
  (testing "simple change"
    (is (= {:a "%foo"}  (map->safe-map {:a "@foo"})))))

