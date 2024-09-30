(ns hokime.sources
  (:require [clojure.java.io :as io])
  (:import (com.github.javaparser.utils SourceRoot)
           (java.io File)))

(defn all-src-paths-under
  "Returns all the source roots -- directories called \"src\" -- under (and including) top"
  [top]
  (prn top)
  (filter (fn [^File f] (->> f .getName (= "src")))
          (file-seq (io/file top))))

(defn resolve-resource-to-path [rsrc]
  (-> rsrc io/resource .toURI))

(defn source-roots-in-resources
  "returns source roots in top directory in resources"
  [top]
  (all-src-paths-under (resolve-resource-to-path top)))

(defn dir->unparsed-source-root [parser-configuration f]
  (SourceRoot. (.toPath f) parser-configuration))