(ns lambdaisland.ansi
  (:import java.util.Scanner)
  (:require [clojure.string :as str]))

(def ESC \o33)

(def colors [:black :red :green :yellow :blue :magenta :cyan :white])

(def defaults
  {:foreground nil
   :background nil
   :bold false})

(defn string-scanner [s]
  (doto (java.util.Scanner. (str "\033[0m" s))
    (.useDelimiter #"\033\[")))

(defn split-token [token]
  (if-let [[_ csi txt] (re-find #"([\x30-\x3F]*[\x20-\x2F]*[\x40-\x7E])(.*)" token)]
    [csi txt]
    [nil (str ESC "[" token)]))

(defn scan [s]
  (let [scanner (string-scanner s)]
    (loop [token (.next scanner)
           result []]
      (let [parsed (split-token token)]
        (if (.hasNext scanner)
          (recur (.next scanner)
                 (conj result parsed))
          (conj result parsed))))))

(defn code->attrs [code]
  (cond
    (#{"" "0"} code)    defaults
    (= "1" code)        {:bold true}
    (= \3 (first code)) {:foreground (get colors (Integer/parseInt (str (last code))))}
    (= \4 (first code)) {:background (get colors (Integer/parseInt (str (last code))))}))

(defn csi->attrs [csi]
  (if (= \m (last csi))
    (let [codes (str/split (apply str (butlast csi)) #";")]
      (reduce #(merge %1 (code->attrs %2)) {} codes))))

(defn attrs->classes [attrs]
  (cond-> []
    (:bold attrs)       (conj :bold)
    (:foreground attrs) (conj (:foreground attrs))
    (:background attrs) (conj (keyword (str "bg-" (name (:background attrs)))))))

(defn ansi->hiccup [s]
  (->> (scan s)
       (reduce (fn [[attrs res] [csi txt]]
                 (let [attrs (merge attrs (csi->attrs csi))]
                   [attrs (conj res [:span {:class (->> attrs
                                                        attrs->classes
                                                        (map name)
                                                        (str/join " "))} txt])]))
               [{} []])
       second
       (drop 1)))
