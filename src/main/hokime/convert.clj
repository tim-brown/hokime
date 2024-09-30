(ns hokime.convert
  (:import (com.github.javaparser Position Range)
           (com.github.javaparser.ast CompilationUnit)
           (com.github.javaparser.ast.body ClassOrInterfaceDeclaration FieldDeclaration MethodDeclaration)
           (javassist.compiler.ast MethodDecl)))

(defn position->
  "converts a JavaParser Position to hokime"
  [^Position p] {:line (.-line p), :column (.-column p)})

(defn range->
  "converts a JavaParser Range to hokime"
  [^Range r] {:begin (position-> (.-begin r)), :end (position-> (.-end r))})

(defmulti restructure :class)

(defmethod restructure CompilationUnit [cu]
  (update cu :types #(->> % (map restructure) (into {}))))

(defmethod restructure ClassOrInterfaceDeclaration [i]
  [(:name i)
   (concat (list (if (:interface i) 'define-interface 'define-class)
                 (into #{} (map :keyword (:modifiers i)))
                 {:unused
                  (seq (dissoc i :interface :name :modifiers :class :classOrInterfaceDeclaration :typeDeclaration :fullyQualifiedName :members :range))})
           (map restructure (:members i)))])

;(defmethod restructure MethodDeclaration [m]
;  [[(:name m) (:signature m)] m]
;  )

;(defmethod restructure FieldDeclaration [m]
;  [(:name m) m]
  ;)

(defmethod restructure :default [i] i)
