(ns hokime.java-parser.utils)

(def expected-property-values
  "set of entries of keyword to generally accepted default values for properties
  given that, if a property matches this... we don't include it in the
  hokime objects"
  (seq {
        :innerClass          false
        :topLevelType        true
        :nestedType          false
        :arrayLevel          0
        :synchronized        false
        :native              false
        :static              false
        :varArgs             false
        :default             false
        :boxedType           false
        :qualified           false
        :enclosingParameters false
        :phantom             false
        }))
