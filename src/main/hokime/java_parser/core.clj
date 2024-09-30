(ns hokime.java-parser.core
  (:import (com.github.javaparser ParserConfiguration)
           (com.github.javaparser.symbolsolver JavaSymbolSolver)
           (com.github.javaparser.symbolsolver.resolution SymbolSolver)
           (com.github.javaparser.symbolsolver.resolution.typesolvers CombinedTypeSolver JarTypeSolver ReflectionTypeSolver)
           (java.util List)))

(defn base-javaparser-environment [{:keys [jar-files] :as config}]
  (let [jar-type-solvers (map #(JarTypeSolver. ^String %) jar-files)
        type-solver (CombinedTypeSolver. (List/copyOf (into [] (concat jar-type-solvers [(ReflectionTypeSolver.)]))))
        symbol-solver (SymbolSolver. type-solver)
        symbol-resolver (JavaSymbolSolver. type-solver)
        parser-configuration (doto (ParserConfiguration.) (.setSymbolResolver symbol-resolver))]
    {
     :jar-type-solvers jar-type-solvers
     :type-solver type-solver
     :symbol-solver symbol-solver
     :symbol-resolver symbol-resolver
     :parser-configuration parser-configuration
     }))
