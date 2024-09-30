(ns hokime.datafy
  (:require [clojure.core.protocols :as p]
            [clojure.datafy :as d]
            [hokime.java-parser.cleanup :as h-clean]
            )
  (:import (com.github.javaparser Position Range)
           (com.github.javaparser.ast CompilationUnit ImportDeclaration Modifier$Keyword Node PackageDeclaration)
           (com.github.javaparser.ast.body CallableDeclaration$Signature ClassOrInterfaceDeclaration VariableDeclarator)
           (com.github.javaparser.ast.expr Name NameExpr SimpleName)
           (java.util Optional)))

(extend-protocol p/Datafiable Optional (datafy [p] (d/datafy (.orElse p nil))))

(extend-protocol p/Datafiable
  Node (datafy [n]
         (h-clean/node->clean-bean n)))

(extend-protocol p/Datafiable Position (datafy [p] {:l (.-line p) :c (.-column p)}))

(extend-protocol p/Datafiable Range (datafy [r] (mapv d/datafy [(.-begin r) (.-end r)])))

;(extend-protocol p/Datafiable VariableDeclarator (datafy [r] "Variable Delcaration"))

(extend-protocol p/Datafiable SimpleName (datafy [p] (symbol (.getIdentifier p))))

(extend-protocol p/Datafiable PackageDeclaration
  (datafy [p]
    (let [d {:name (d/datafy (.getName p))}]
      (if-let [annotations (seq (map d/datafy (.getAnnotations p)))]
        (assoc d :annotations annotations)
        d))))

(extend-protocol p/Datafiable ImportDeclaration
  (datafy [p] {:name (d/datafy (.getName p))
               :asterisk? (.isAsterisk p)
               :static? (.isStatic p)}))

(extend-protocol p/Datafiable NameExpr
  (datafy [p] {:name (d/datafy (.getName p))
               :nameExpr true
               :range (d/datafy (.getRange p))}))

(extend-protocol p/Datafiable Name
  (datafy [p]
    (let [n (.getIdentifier p)
          fq (loop [q (.getQualifier p), l nil]
               (if (.isPresent q)
                 (let [q ^Name (.get q)]
                   (recur (.getQualifier q) (conj l (.getIdentifier q))))
                 (when l (clojure.string/join "." l))))]
      (symbol fq n))))

(def modifier-keyword->keyword
  {
   Modifier$Keyword/DEFAULT      :default
   Modifier$Keyword/PUBLIC       :public
   Modifier$Keyword/PROTECTED    :protected
   Modifier$Keyword/PRIVATE      :private
   Modifier$Keyword/ABSTRACT     :abstract
   Modifier$Keyword/STATIC       :static
   Modifier$Keyword/FINAL        :final
   Modifier$Keyword/TRANSIENT    :transient
   Modifier$Keyword/VOLATILE     :volatile
   Modifier$Keyword/SYNCHRONIZED :synchronized
   Modifier$Keyword/NATIVE       :native
   Modifier$Keyword/STRICTFP     :strictfp
   Modifier$Keyword/TRANSITIVE   :transitive
   })

(extend-protocol p/Datafiable Modifier$Keyword
  (datafy [p] (modifier-keyword->keyword p)))

(extend-protocol p/Datafiable CallableDeclaration$Signature
  (datafy [p] (.asString p)))

(extend-protocol p/Datafiable ClassOrInterfaceDeclaration
  (datafy [p]
    (-> p h-clean/node->clean-bean)))

  (extend-protocol p/Datafiable CompilationUnit
  (datafy [p]
    (-> p
        h-clean/node->clean-bean
        (dissoc :primaryType)
        )))
