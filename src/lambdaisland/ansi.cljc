(ns lambdaisland.ansi
  (:require [clojure.string :as str]))

;; https://en.wikipedia.org/wiki/ANSI_escape_code#CSI_sequences
;;


;; The ESC [ is followed by any number (including none) of "parameter bytes" in
;; the range 0x30–0x3F (ASCII 0–9:;<=>?), then by any number of "intermediate
;; bytes" in the range 0x20–0x2F (ASCII space and !"#$%&'()*+,-./), then finally
;; by a single "final byte" in the range 0x40–0x7E (ASCII @A–Z[\]^_`a–z{|}~).
(def csi-pattern
  "Regex to match a Control Sequence Introducer"
  #?(:clj  #"(?s)([^\033]*)\033\[([\x30-\x3F]*[\x20-\x2F]*[\x40-\x7E])(.*)"
     :cljs #"([^\033]*)\033\[([\x30-\x3F]*[\x20-\x2F]*[\x40-\x7E])([\s\S]*)"))

(def ESC
  "ASCII escape character (codepoint 27, hex 1b, octal 33).

  In ClojureScript: returns a single character String."
  #?(:clj \u001b
     :cljs "\033"))

;; | Name           | FG |  BG | VGA         | CMD         | Terminal.app | PuTTY       | mIRC        | xterm       | Ubuntu      |
;; |----------------+----+-----+-------------+-------------+--------------+-------------+-------------+-------------+-------------|
;; | Black          | 30 |  40 | 0,0,0       | 1,1,1       | 0,0,0        | 0,0,0       | 0,0,0       | 0,0,0       | 0,0,0       |
;; | Red            | 31 |  41 | 170,0,0     | 128,0,0     | 194,54,33    | 187,0,0     | 127,0,0     | 205,0,0     | 222,56,43   |
;; | Green          | 32 |  42 | 0,170,0     | 0,128,0     | 37,188,36    | 0,187,0     | 0,147,0     | 0,205,0     | 57,181,74   |
;; | Yellow         | 33 |  43 | 170,85,0    | 128,128,0   | 173,173,39   | 187,187,0   | 252,127,0   | 205,205,0   | 255,199,6   |
;; | Blue           | 34 |  44 | 0,0,170     | 0,0,128     | 73,46,225    | 0,0,187     | 0,0,127     | 0,0,238     | 0,111,184   |
;; | Magenta        | 35 |  45 | 170,0,170   | 128,0,128   | 211,56,211   | 187,0,187   | 156,0,156   | 205,0,205   | 118,38,113  |
;; | Cyan           | 36 |  46 | 0,170,170   | 0,128,128   | 51,187,200   | 0,187,187   | 0,147,147   | 0,205,205   | 44,181,233  |
;; | White          | 37 |  47 | 170,170,170 | 192,192,192 | 203,204,205  | 187,187,187 | 210,210,210 | 229,229,229 | 204,204,204 |
;; | Bright Black   | 90 | 100 | 85,85,85    | 128,128,128 | 129,131,131  | 85,85,85    | 127,127,127 | 127,127,127 | 128,128,128 |
;; | Bright Red     | 91 | 101 | 255,85,85   | 255,0,0     | 252,57,31    | 255,85,85   | 255,0,0     | 255,0,0     | 255,0,0     |
;; | Bright Green   | 92 | 102 | 85,255,85   | 0,255,0     | 49,231,34    | 85,255,85   | 0,252,0     | 0,255,0     | 0,255,0     |
;; | Bright Yellow  | 93 | 103 | 255,255,85  | 255,255,0   | 234,236,35   | 255,255,85  | 255,255,0   | 255,255,0   | 255,255,0   |
;; | Bright Blue    | 94 | 104 | 85,85,255   | 0,0,255     | 88,51,255    | 85,85,255   | 0,0,252     | 92,92,255   | 0,0,255     |
;; | Bright Magenta | 95 | 105 | 255,85,255  | 255,0,255   | 249,53,248   | 255,85,255  | 255,0,255   | 255,0,255   | 255,0,255   |
;; | Bright Cyan    | 96 | 106 | 85,255,255  | 0,255,255   | 20,240,240   | 85,255,255  | 0,255,255   | 0,255,255   | 0,255,255   |
;; | Bright White   | 97 | 107 | 255,255,255 | 255,255,255 | 233,235,235  | 255,255,255 | 255,255,255 | 255,255,255 | 255,255,255 |
(def color-schemes
  "Color schemes used in popular applications."
  (->> [[:vga           :cmd           :osx           :putty         :mirc          :xterm         :ubuntu        ]
        [[   0   0   0 ][   1   1   1 ][   0   0   0 ][   0   0   0 ][   0   0   0 ][   0   0   0 ][   0   0   0 ]]
        [[ 170   0   0 ][ 128   0   0 ][ 194  54  33 ][ 187   0   0 ][ 127   0   0 ][ 205   0   0 ][ 222  56  43 ]]
        [[   0 170   0 ][   0 128   0 ][  37 188  36 ][   0 187   0 ][   0 147   0 ][   0 205   0 ][  57 181  74 ]]
        [[ 170  85   0 ][ 128 128   0 ][ 173 173  39 ][ 187 187   0 ][ 252 127   0 ][ 205 205   0 ][ 255 199   6 ]]
        [[   0   0 170 ][   0   0 128 ][  73  46 225 ][   0   0 187 ][   0   0 127 ][   0   0 238 ][   0 111 184 ]]
        [[ 170   0 170 ][ 128   0 128 ][ 211  56 211 ][ 187   0 187 ][ 156   0 156 ][ 205   0 205 ][ 118  38 113 ]]
        [[   0 170 170 ][   0 128 128 ][  51 187 200 ][   0 187 187 ][   0 147 147 ][   0 205 205 ][  44 181 233 ]]
        [[ 170 170 170 ][ 192 192 192 ][ 203 204 205 ][ 187 187 187 ][ 210 210 210 ][ 229 229 229 ][ 204 204 204 ]]

        [[ 85   85  85 ][ 128 128 128 ][ 129 131 131 ][  85  85  85 ][ 127 127 127 ][ 127 127 127 ][ 128 128 128 ]]
        [[ 255  85  85 ][ 255   0   0 ][ 252  57  31 ][ 255  85  85 ][ 255   0   0 ][ 255   0   0 ][ 255   0   0 ]]
        [[ 85  255  85 ][   0 255   0 ][  49 231  34 ][  85 255  85 ][   0 252   0 ][   0 255   0 ][   0 255   0 ]]
        [[ 255 255  85 ][ 255 255   0 ][ 234 236  35 ][ 255 255  85 ][ 255 255   0 ][ 255 255   0 ][ 255 255   0 ]]
        [[  85  85 255 ][   0   0 255 ][  88  51 255 ][  85  85 255 ][   0   0 252 ][  92  92 255 ][   0   0 255 ]]
        [[ 255  85 255 ][ 255   0 255 ][ 249  53 248 ][ 255  85 255 ][ 255   0 255 ][ 255   0 255 ][ 255   0 255 ]]
        [[  85 255 255 ][   0 255 255 ][  20 240 240 ][  85 255 255 ][   0 255 255 ][   0 255 255 ][   0 255 255 ]]
        [[ 255 255 255 ][ 255 255 255 ][ 233 235 235 ][ 255 255 255 ][ 255 255 255 ][ 255 255 255 ][ 255 255 255 ]]]
       (apply mapv vector)
       (into {} (map (fn [[x & xs]] [x (vec xs)])))))

(def ^:dynamic *color-scheme*
  "Color scheme currently in use during parsing."
  (:xterm color-schemes))

(defmacro with-color-scheme
  "Execute code with the given color scheme active, name must be one
  of :vga :cmd :osx :putty :mirc :xterm :ubuntu. For finer control bind to
  *color-scheme* directly."
  [name & body]
  `(binding [*color-scheme* (get color-schemes name)]
     ~@body))

(defn get-color [n]
  (into [:rgb] (get *color-scheme* n)))

(defn parse-int [s]
  #?(:clj (Integer/parseInt s)
     :cljs (js/parseInt s 10)))

(defn code->attrs
  "Given a CSI code, return a map of properties it sets. A value of `nil` means
  the property gets unset."
  [code]
  (cond
    (= 0 code)        {:foreground nil
                       :background nil
                       :bold       nil}
    (= 1 code)        {:bold true}
    (<= 30 code 37)   {:foreground (get-color (- code 30))}
    (= 39 code)       {:foreground nil}
    (<= 40 code 47)   {:background (get-color (- code 40))}
    (= 49 code)       {:background nil}
    (<= 90 code 99)   {:foreground (get-color (+ 8 (- code 90)))}
    (<= 100 code 109) {:background (get-color (+ 8 (- code 40)))}))

;; 0-  7:  standard colors (as in ESC [ 30–37 m)
;; 8- 15:  high intensity colors (as in ESC [ 90–97 m)
;; 16-231:  6 × 6 × 6 cube (216 colors): 16 + 36 × r + 6 × g + b (0 ≤ r, g, b ≤ 5)
;; 232-255:  grayscale from black to white in 24 steps
(defn color-8-bit
  "Parse a \"8-bit\" color, given the code that follows on ESC[38;5;<code>m."
  [code]
  (cond
    (<= 0 code 7)     [(get-color code) false]
    (<= 8 code 15)    [(get-color (- code 8)) true]
    (<= 16 code 231)  (let [code         (- code 16)
                            blue         (mod code 6)
                            green        (mod (/ (- code blue) 6) 6)
                            red          (/ (- code blue (* 6 green)) 36)
                            color-values [0x00 0x5f 0x87 0xaf 0xd7 0xff]]
                        [(into [:rgb] (map color-values) [red green blue])
                         false])
    (<= 232 code 255) (let [x (+ 8 (* 10 (- code 232)))]
                        [[:rgb x x x] false])))

(defn color-24-bit [[r g b]]
  [:rgb r g b])

(defn parse-color
  "Handle CSI code 38 and 48, used to specify 8 or 24 bit colors. This may consume
  up to 5 codes in total (ESC [38;2;r;g;bm). Returns the map of properties that
  get set, and the remaining, unconsumed codes."
  [fg-or-bg [colorspace & more]]
  (let [type (case fg-or-bg 38 :foreground 48 :background)]
    (case colorspace
      5 (let [[color bold?] (color-8-bit (first more))]
          [(merge {type color} (if bold? {:bold true}))
           (next more)])
      2 [{:foreground (color-24-bit (take 3 more))}
         (nthnext more 3)])))

(defn csi->attrs
  "Given a CSI specifier, excluding ESC[ but including the final \"m\", convert it
  to a map of properties that it sets or unsets. Property values of nil indicate
  a reset/unset. "
  [csi]
  (if (= \m (last csi)) ;; m: SGR - Select Graphic Rendition
    (loop [[code & codes] (map parse-int (str/split (apply str (butlast csi)) #";"))
           result         {}]
      (if code
        (if (or (= 38 code) (= 48 code))
          (let [[res codes] (parse-color code codes)]
            (recur codes (merge result res)))
          (recur codes
                 (merge result (code->attrs code))))
        result))))

(defn str-length
  "Fast string length"
  [s]
  #?(:clj (.length s)
     :cljs (.-length s)))

(defn has-escape-char?
  "Efficient check to see if a string contains an escape character."
  [s]
  (let [len (str-length s)]
    (loop [i 0]
      (cond
        (= i len)
        false

        (= ESC (.charAt s i))
        true

        :else
        (recur (inc i))))))

(defn token-stream
  "Tokenize a string, whereby each CSI sequence gets transformed into a map of
  properties. The result is a vector of strings and maps."
  [string]
  (if (has-escape-char? string) ;; short circuit
    (loop [input string
           result []]
      (if-let [match (re-find csi-pattern input)]
        (let [[_ start csi tail] match]
          (recur tail
                 (-> result
                     (cond-> #_result (seq start) (conj start))
                     (conj (csi->attrs csi)))))
        (cond-> result (seq input) (conj input))))
    [string]))

(defn apply-props
  "Stateful transducer, apply it over the output of token-stream to know which
  styling should be applied over each piece of text.

  The results are pairs consisting of a property map and a string."
  [rf]
  (let [state (atom {})]
    (fn
      ([] (rf))
      ([res] (rf res))
      ([res val]
       (when (map? val)
         (swap! state #(into {} (remove (comp nil? second)) (merge % val))))
       (if (string? val)
         (rf res [@state val])
         res)))))

(defn rgb->css [[_ r g b]]
  (str "rgb(" r "," g "," b ")"))

(defn chunk->hiccup [[{:keys [foreground background bold] :as props} text]]
  [:span (if (seq props)
           {:style (cond-> {}
                     foreground (assoc :color (rgb->css foreground))
                     background (assoc :background-color (rgb->css background))
                     bold       (assoc :font-weight "bold"))}
           {})
   text])

(def hiccup-xform
  "Transducer that consumes strings of input, and produces hiccup elements. Useful
  when dealing with streaming input, since it will carry over the 'terminal
  state'."
  (comp (map token-stream)
        apply-props
        (map chunk->hiccup)))

(defn text->hiccup
  "Convenience function for the basic case where you have a string of terminal
  output and want to turn it into hiccup. Returns a seq of [:span] elements."
  [text]
  (sequence (comp apply-props
                  (map chunk->hiccup))
            (token-stream text)))

