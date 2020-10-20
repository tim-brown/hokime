(ns hokime.core
  (:import (java.nio.file Path)
           (java.net URI)
           (java.util.function Consumer)
           (com.github.javaparser.utils SourceRoot
                                        SourceRoot$Callback
                                        SourceRoot$Callback$Result)
           (com.github.javaparser ParserConfiguration
                                  ParseResult)
           ;(com.github.javaparser.ast.visitor GenericVisitorWithDefaults)
           (com.github.javaparser.ast.expr LambdaExpr)))


(def project-root (System/getenv "HOKIME_PROJECT_ROOT"))

(def roots '(;"src/main/java"
              ""
              ))

(def source-roots
  (map #(SourceRoot.
           (.resolve (Path/of (URI/create (str "file://" project-root))) %))
       roots))

(doall (map #(.tryToParse %) source-roots))

(def all-compilation-units
  (apply concat (map #(.getCompilationUnits %) source-roots)))

(comment
(defn process-parse-result [compilation-unit-consumer]
  (reify SourceRoot$Callback
    (process [this local-path absolute-path result]
      (.ifSuccessful result compilation-unit-consumer)
      SourceRoot$Callback$Result/DONT_SAVE)))

(defn parse-source-root [source-root compilation-unit-consumer]
  (.parse source-root "" (process-parse-result compilation-unit-consumer)))

(defn process-all-compilation-units [compilation-unit-consumer]
  (map #(parse-source-root
          % 
          (reify Consumer (accept [this u] (compilation-unit-consumer u))))
       source-roots)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn classifed-lambda-expression [cu-path n]
  (and (instance? LambdaExpr n)
       {:class nil
        :path cu-path
        :range (-> n .getRange .get .toString)
        :source (.toString n)
        :node n
        }))

(defn process-cu [cu]
  (let [cu-path (-> cu .getStorage .get .getPath .toString)
        streamed-nodes (-> (.stream cu) .iterator iterator-seq)]
    (filter identity (map (partial classifed-lambda-expression cu-path) streamed-nodes))))

(defn test-roots []
  (mapcat process-cu all-compilation-units))
