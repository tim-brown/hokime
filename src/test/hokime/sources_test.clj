(ns hokime.sources-test
  (:require [clojure.test :refer :all]
            [hokime.sources :refer :all]))

(deftest all-source-roots-under-resources-test
  (testing "We find at least Foo.java in corpus"
    (is (seq (all-src-paths-under "/home/timbrown/github/hokime/resources")))
    (is (resolve-resource-to-path "corpus"))
    (is (seq (source-roots-in-resources "corpus")))))
