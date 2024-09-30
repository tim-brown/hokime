(ns hokime.java-parser.visitation-flags
  (:require [clojure.set :as set]))

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

(def statement-type-flags
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

(def all-visitation-flags (set/union declaration-type-flags
                                     type-type-flags
                                     statement-type-flags
                                     expression-type-flags))
