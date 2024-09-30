(ns hokime.java-parser.cleanup
  "process to create and clean up beans for further processing in hokime"
  (:require [clojure.datafy :as d]
            [hokime.java-parser.utils :as hu]
            [hokime.java-parser.visitation-flags :as vf]
            [debux.core :as dbg]
            )
  (:import (com.github.javaparser.ast Node)
           (java.util Optional)))

(defn remove-self-or-parent-referencing-keys
  "removes keys that potentially cause loops"
  [m]
  (dissoc m
          :parentNodeForChildren
          :parentNode
          :elementType
          :commentedNode                                    ; beware that commented nodes can cause circularity
          :allContainedComments
          :allComments
          :comment
          :comments
          ))

(defn resolve-optional-entry
  "we tend not to use optionals... so this will get() them or remove the key entirely"
  [[k v]]
  (when (instance? Optional v)
    [k (.orElse v nil)]))

(defn resolve-optionals [m]
  (apply assoc m (mapcat identity (keep resolve-optional-entry m))))

(defn descend-sequable-entry [[k v]]
  (cond
    (string? v) nil
    (map? v) nil
    (seqable? v) [k (map d/datafy v)]
    :else nil))

(defn descend-keys [m]
  (into {} (map (fn [[k v]]
                  (case k
                    :class [k v]
                    [k (d/datafy v)])) m)))

(defn descend-seqs [m]
  (if-let [descended-kvs (seq (mapcat identity (keep descend-sequable-entry m)))]
    (apply assoc m descended-kvs)
    m))

(defn cleanup-empties [m]
  (if-let [empty-entry-keys (seq (keep (fn [[k v]] (when (and (seqable? v) (empty? v)) k)) m))]
    (apply dissoc m empty-entry-keys)
    m))

(defn remove-uninteresting-keys
  "keys that really don't bring much value, but will be in metadata if you need them"
  [m]
  (dissoc m
          :lineEndingStyle
          :tokenRange
          :parsed
          :storage
          :dataKeys
          :metaModel))

(defn remove-false-visitor-flag-keys
  "no need to keep the false key in the beans for visitor classes, their absence is telling enough"
  [m]
  (apply dissoc m (remove #(% m) vf/all-visitation-flags)))

(defn matching-expected-flag-keys [b]
  (keep #(when (= (b (key %)) (val %)) (key %)) hu/expected-property-values))

(defn remove-expected-flags
  "When a flag is set to a sensible (expected) default, we will either not care, or draw implictions
  from it. In any case, remove it from the bean"
  [m]
  (apply dissoc m (matching-expected-flag-keys m)))

(defn remove-acknowledged-nodes-from-child-nodes [m]
  (if (contains? m :childNodes) (dissoc m :childNodes) m)
  #_(let [cNs (into #{} (:childNodes m))]
      (if-let [known-children (seq (mapcat identity (keep (fn [[k v]]
                                                            (cond
                                                              (= k :childNodes) nil
                                                              (cNs v) [v]
                                                              (seqable? v) (filter cNs v)
                                                              :else nil))
                                                          m)))]
        (let [kcs (set known-children)]
          (update m :childNodes (fn [cN] (into [] (remove kcs cN)))))
        m)))

(defn node->clean-bean [N]
  (if (instance? Node N)
    (let [b (->> (bean N)
                 remove-self-or-parent-referencing-keys
                 resolve-optionals
                 remove-uninteresting-keys
                 remove-expected-flags
                 remove-false-visitor-flag-keys
                 descend-seqs
                 remove-acknowledged-nodes-from-child-nodes
                 descend-keys
                 cleanup-empties
                 )]
      b)
    N))
