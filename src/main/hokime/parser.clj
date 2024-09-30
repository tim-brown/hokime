(ns hokime.parser
  (:require [debux.core :as d]
            [hokime.convert]))

(declare bean->hokime)

(defn bring-up-children [n]
  (update
    (assoc n :children (-> n :bean :childNodes))
    :bean dissoc :childNodes))

(defn remap-seq-entry [[k v]]
  (cond
    (not (seqable? v)) nil
    (string? v) nil
    (map? v) nil
    :else [k (mapv bean->hokime v)]))

(defn recur-on-seqs [n]
  (if-let [remappings (seq (keep remap-seq-entry n))]
    (apply assoc n (mapcat identity remappings))
    n))

(defn class-to-type [n]
  (let [n+ (assoc n :type (some->> n :bean :class (cast Class) .getSimpleName (clojure.string/lower-case) (keyword "hokime.type")))]
    (update n+ :bean #(dissoc % :class))))

(defn convert-ranges [n]
  (if (-> n :bean :range)
    (update-in n [:bean :range] hokime.convert/range->)
    n))

(defn bean->hokime [b]
  (->> b
       bring-up-children
       recur-on-seqs
       class-to-type
       convert-ranges
       ))