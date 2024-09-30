(ns hokime.core-test
  (:require [clojure.test :refer :all]
            [hokime.core :refer :all])
  (:import (com.github.javaparser.utils SourceRoot)
           (java.net URI)
           (java.nio.file Path)))

(deftest resolve-resource-test
  (testing "resolution of resources to path"
    (is (instance? URI (hokime.sources/resolve-resource-to-path "corpus")))
    ))

(deftest parse-all-files-test
  (testing "easy entry point"
    (is (= (parse-all-files (hokime.sources/resolve-resource-to-path "corpus") {})
           ["/home/timbrown/github/hokime/resources/corpus/src"
            (->> "file:///home/timbrown/github/hokime/resources/corpus/src" URI. Path/of SourceRoot.)]))))