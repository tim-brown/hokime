(ns hokime.core
  (:require [clojure.java.io :as io]
            [clojure.set :as set])
  (:import (java.nio.file Path)
           (java.net URI)
           (java.net URL)
           (com.github.javaparser ParseResult)
           (com.github.javaparser.ast Node
                                      CompilationUnit
                                      Modifier
                                      Modifier$Keyword
                                      )
           (com.github.javaparser.utils ParserCollectionStrategy
                                        ProjectRoot
                                        SourceRoot)))

(def test-class-file-name "net/timb/hokime/test/Foo.java")

(defn resolve-resource-to-path [rsrc]
  (-> rsrc
      io/resource
      .toURI
      Path/of))

(def collection-strategy (new ParserCollectionStrategy))

(def project-root (.collect collection-strategy (resolve-resource-to-path "corpus")))

(def source-roots (.getSourceRoots project-root))

(for [source-root source-roots] (.tryToParse source-root))

(def compilation-units (flatten (map #(vec (.getCompilationUnits %)) source-roots)))

(def expression-type-flags
  "Symbols that indicate properties in node beans used to distinguish types of expressions.
  This is (or could be) duplicated in the ::type of the hokime node"
  #{
    :annotationExpr
    :arrayAccessExpr
    :arrayCreationExpr
    :arrayInitializerExpr
    :assignExpr
    :binaryExpr
    :booleanLiteralExpr
    :castExpr
    :charLiteralExpr
    :classExpr
    :conditionalExpr
    :doubleLiteralExpr
    :enclosedExpr
    :fieldAccessExpr
    :instanceOfExpr
    :integerLiteralExpr
    :lambdaExpr
    :literalExpr
    :literalStringValueExpr
    :longLiteralExpr
    :markerAnnotationExpr
    :methodCallExpr
    :methodReferenceExpr
    :nameExpr
    :normalAnnotationExpr
    :nullLiteralExpr
    :objectCreationExpr
    :patternExpr
    :polyExpression
    :singleMemberAnnotationExpr
    :stringLiteralExpr
    :superExpr
    :switchExpr
    :textBlockLiteralExpr
    :thisExpr
    :typeExpr
    :unaryExpr
    :variableDeclarationExpr
    })

(def statment-type-flags
  "Symbols that indicate properties in node beans used to distinguish types of statements.
  This is (or could be) duplicated in the ::type of the hokime node"
  #{
    :forEachStmt
    :assertStmt
    :synchronizedStmt
    :emptyStmt
    :tryStmt
    :returnStmt
    :blockStmt
    :labeledStmt
    :unparsableStmt
    :whileStmt
    :throwStmt
    :yieldStmt
    :continueStmt
    :expressionStmt
    :ifStmt
    :localClassDeclarationStmt
    :doStmt
    :breakStmt
    :forStmt
    :switchStmt
    :explicitConstructorInvocationStmt
    })

(def type-type-flags
  "Symbols that indicate properties in node beans used to distinguish types of types.
  This is (or could be) duplicated in the ::type of the hokime node"
  #{
    :wildcardType
    :classOrInterfaceType
    :intersectionType
    :typeParameter
    :unionType
    :elementType
    :varType
    :arrayType
    :voidType
    :referenceType
    :primitiveType
    :unknownType
    })

(def declaration-type-flags
  "Symbols that indicate properties in node beans used to distinguish types of declarations.
  This is (or could be) duplicated in the ::type of the hokime node"
  #{
    :constructorDeclaration
    :enumConstantDeclaration
    :initializerDeclaration
    :annotationDeclaration
    :callableDeclaration
    :localClassDeclaration
    :classOrInterfaceDeclaration
    :typeDeclaration
    :methodDeclaration
    :fieldDeclaration
    :enumDeclaration
    :annotationMemberDeclaration
    })

(def node-ignore-set
  "Properties that will be discarded because they are otherwise captured by typing"
  (set/union expression-type-flags
             statment-type-flags
             type-type-flags
             declaration-type-flags))

(def expected-property-values-set
  "set of entries of keyword to generally accepted default values for properties
  given that, if a property matches this... we don't include it in the
  hokime objects"
  (set (seq {
             :innerClass false
             :topLevelType true
             :nestedType false
             :arrayLevel 0
             :synchronized false
             :native false
             :static false
             :varArgs false
             :default false
             :boxedType false
             :qualified false
             :enclosingParameters false
             })))

(def node-meta-properties
  "Properties that will be segregaed into the meta-data of objects"
  #{
    :comment
    :comments
    :allContainedComments
    :allComments
    :orphanComments
    :primaryType
    :class
    :phantom
    :metaModel
    :parentNode
    :parentNodeForChildren
    :parsed
    :childNodes
    :range
    :tokenRange
    :dataKeys
    :lineEndingStyle
    :storage
    })

(def fixed-child-bearing-keys
  "Keys that will always be mapped into the parent, and removed from ::children"
  #{
    :name
    :members
    :modifiers
    :typeParameters
    })

(defn unpack-optional [v]
  (if (instance? java.util.Optional v) (.orElse v nil) v))

(defn java-collection? [v]
  "test for an instance of java.util.Collection"
  (instance? java.util.Collection v))

(defn apply-or-map-if-java-collection
  "applies f to a value unless it's a Java Collection,
  in which case map f over the collection and return a
  sequence"
  [f v]
  (if (java-collection? v) (map f v) (f v)))

(defn ensure-property-is-coll [p] (if (java-collection? p) (seq p) [p]))

(defn interesting-property? [p]
  (let [v (last p)]
    (cond
      (expected-property-values-set p) false
      (instance? java.util.Optional v) (.isPresent v)
      (java-collection? v) (not (.isEmpty v))
      (not (coll? v)) true
      true (seq v))))

(defn groom-bean
  "Prepares a bean for seaparation into the meta and value parts"
  [u]
  (->> (bean u)
       (filter #(not (node-ignore-set (first %))))
       (filter interesting-property?)
       (map #(vector (first %) (unpack-optional (last %))))))

(defn type-key [o] (keyword "hokime.core" (.getSimpleName (type o))))

(defn identifier-node->hokime [u]
  (with-meta {::type (type-key u) :identifier (.toString u)} {::raw u}))

(defn simple-node-with-Name->hokime [u interesting-property?]
  (let [m [{::type (type-key u)
            ::properties (into {} (filter (comp interesting-property? first) (bean u)))
            ::name (node->hokime (first (seq (.getChildNodes u))))}]]
    (with-meta m {::raw u})))

(defmulti node->hokime "Converts a JavaParser Node to a Hokime structure" type-key)

(defn node->hokime-property [v] (apply-or-map-if-java-collection node->hokime v))

(defn extract-named-children [u b ks]
  (let [ks+ (into fixed-child-bearing-keys ks)
        entries-from-bean (filter (comp ks+ first) b)
        named-children (map (fn [[k v]] [k (node->hokime-property v)]) entries-from-bean)
        captured (set (mapcat (comp ensure-property-is-coll last) entries-from-bean))]
    [named-children captured]))

(defn compound-node->hokime [u child-bearing-keys]
  (let [b (group-by #(contains? node-meta-properties (first %)) (groom-bean u))
        [named-children captured] (extract-named-children u (b false) child-bearing-keys)
        m (into {::type (type-key u)} named-children)
        children (map node->hokime (remove captured (.getChildNodes u)))]
    (with-meta (into m (when (seq children) {::children (vec children)}))
               {::raw u, ::properties (into {} (b true))})))

(defmethod node->hokime ::SimpleName [u] (identifier-node->hokime u))

(defmethod node->hokime ::Name [u] (identifier-node->hokime u))

(defmethod node->hokime ::PackageDeclaration [u] (simple-node-with-Name->hokime u #{}))

(defmethod node->hokime ::ImportDeclaration [u]
  (simple-node-with-Name->hokime u #{:asterisk :static}))

(defmethod node->hokime ::CompilationUnit [u]
  (compound-node->hokime u #{:imports :packageDeclaration :types}))

(defmethod node->hokime ::ClassOrInterfaceDeclaration [u]
  (compound-node->hokime u #{:modifiers}))

(defmethod node->hokime ::MethodDeclaration [u]
  (compound-node->hokime u #{:type}))

;; FIXME: :modifiers produces a list of sets; we need to union them up!

(def modifier-keyword->keyword
  {
   Modifier$Keyword/DEFAULT :default
   Modifier$Keyword/PUBLIC :public
   Modifier$Keyword/PROTECTED :protected
   Modifier$Keyword/PRIVATE :private
   Modifier$Keyword/ABSTRACT :abstract
   Modifier$Keyword/STATIC :static
   Modifier$Keyword/FINAL :final
   Modifier$Keyword/TRANSIENT :transient
   Modifier$Keyword/VOLATILE :volatile
   Modifier$Keyword/SYNCHRONIZED :synchornized
   Modifier$Keyword/NATIVE :native
   Modifier$Keyword/STRICTFP :strictfp
   Modifier$Keyword/TRANSITIVE :transitive
   })

(defmethod node->hokime ::Modifier [u]
  (with-meta #{(modifier-keyword->keyword (.getKeyword u))} {:raw u}))

(defmethod node->hokime :default [u]
  (compound-node->hokime u #{}))

(vec (map identity compilation-units))

(defn map-all-units []
  (vec (map node->hokime compilation-units)))

(map-all-units)

(comment
  (resolve-resource-to-path test-class-file-name)
  project-root)


