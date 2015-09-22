(ns leiningen.heroku-test
  (:require [clojure.test :refer :all]
            [leiningen.heroku :refer :all]
            [clojure.string :as s]))

(deftest file-join-should-work
  (is (= "target/my-app.jar" (file-join "target" "my-app.jar"))))

(deftest uberjar-missing-returns-false
  (is (= false (uberjar-missing {:uberjar-name "lein-heroku-0.5.1.jar"}))))

(deftest uberjar-missing-returns-true
  (is (= true (uberjar-missing {:uberjar-name "quack.jar"}))))

(deftest uberjar-missing-returns-false-when-custom-includes
  (is (= false (uberjar-missing {:uberjar-name "quack.jar" :heroku {:include-files ["whatever"]}}))))
