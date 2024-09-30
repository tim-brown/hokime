(ns hokime.core
  (:require [clojure.datafy :as d]
            [hokime.java-parser.cleanup]
            [hokime.java-parser.core :as jp]
            [hokime.sources :as hs]
            [hokime.convert :as cnv]
            )
  (:import (com.github.javaparser.ast.body VariableDeclarator)))

(require 'hokime.datafy)

(defn parse-all-files
  "*easy* entry-point to getting all your files parsed"
  [top config]
  (let [env (jp/base-javaparser-environment config)
        source-roots (map (juxt str (partial hs/dir->unparsed-source-root (:parser-configuration env)))
                          (hs/all-src-paths-under top))]
    (doseq [r (map second source-roots)] (.tryToParse r))
    source-roots))

(defn all-cus
  "*easy* entry point to all your compilation-units"
  [top config]
  (->> (parse-all-files top config)
       (mapcat (fn [[n sr]] (map (fn [cu] [n cu]) (.getCompilationUnits sr))))))

(defn clean-all-cus
  [top config]
  (->> (all-cus top config)
       (map second)))

(defn hokime-datafy [config top]
  (->> (clean-all-cus top config)
       (map d/datafy)))

(defn hokime [config top]
  (->> (hokime-datafy config top)
       (map cnv/restructure)))

(comment
  (->> "corpus"
       hs/resolve-resource-to-path ;(parse-all-files {})
       ;(all-cus {})
       (hokime {})
       ;first
       ;second
       ;jp-bean/node->bean
       ;doall
       ;(map keys)
       ;((constantly 42))
       ;prn
       ;first
       ;:range
       ;:children
       )

  VariableDeclarator

  (hs/all-src-paths-under (hs/resolve-resource-to-path "corpus"))
  )

;(comment
;  (defn type-key [o] (keyword "hokime.core" (.getSimpleName (type o))))
;
;  (defn identifier-node->hokime [u]
;    (with-meta {::type (type-key u) :identifier (.toString u)} {::raw u}))
;
;  (defmulti node->hokime "Converts a JavaParser Node to a Hokime structure" type-key)
;
;  (defn simple-node-with-Name->hokime [u interesting-property?]
;    (let [m [{::type       (type-key u)
;              ::properties (into {} (filter (comp interesting-property? first) (bean u)))
;              ::name       (node->hokime (first (seq (.getChildNodes u))))}]]
;      (with-meta m {::raw u})))
;
;  (defn node->hokime-property [v] (apply-or-map-if-java-collection node->hokime v))
;
;  (defn extract-named-children [u b ks]
;    (let [ks+ (into fixed-child-bearing-keys ks)
;          entries-from-bean (filter (comp ks+ first) b)
;          named-children (map (fn [[k v]] [k (node->hokime-property v)]) entries-from-bean)
;          captured (set (mapcat (comp ensure-property-is-coll last) entries-from-bean))]
;      [named-children captured]))
;
;  (defn compound-node->hokime [u child-bearing-keys]
;    (let [b (group-by #(contains? node-meta-properties (first %)) (groom-bean u))
;          [named-children captured] (extract-named-children u (b false) child-bearing-keys)
;          m (into {::type (type-key u)} named-children)
;          children (map node->hokime (remove captured (.getChildNodes u)))]
;      (with-meta (into m (when (seq children) {::children (vec children)}))
;                 {::raw u, ::properties (into {} (b true))})))
;
;  (defmethod node->hokime ::SimpleName [u] (identifier-node->hokime u))
;
;  (defmethod node->hokime ::Name [u] (identifier-node->hokime u))
;
;  (defmethod node->hokime ::PackageDeclaration [u] (simple-node-with-Name->hokime u #{}))
;
;  (defmethod node->hokime ::ImportDeclaration [u]
;    (simple-node-with-Name->hokime u #{:asterisk :static}))
;
;  (defmethod node->hokime ::CompilationUnit [u]
;    (compound-node->hokime u #{:imports :packageDeclaration :types}))
;
;  (defmethod node->hokime ::ClassOrInterfaceDeclaration [u]
;    (compound-node->hokime u #{:modifiers}))
;
;  (defmethod node->hokime ::MethodDeclaration [u]
;    (compound-node->hokime u #{:type}))
;
; FIXME: :modifiers produces a list of sets; we need to union them up!
;
;(defmethod node->hokime ::Modifier [u]
;  (with-meta #{(modifier-keyword->keyword (.getKeyword u))} {:raw u}))
;
;(defmethod node->hokime :default [u]
;  (compound-node->hokime u #{}))
;
;(vec (map identity compilation-units))
;
;(defn map-all-units []
;  (vec (map node->hokime compilation-units)))
;
;(map-all-units)
;
;(comment
;  (resolve-resource-to-path test-class-file-name)
;  project-root)
;)