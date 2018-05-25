(ns lambdaisland.ansi2
  (:require [clojure.string :as str]))

;; https://en.wikipedia.org/wiki/ANSI_escape_code#CSI_sequences
;;
;; The ESC [ is followed by any number (including none) of "parameter bytes" in
;; the range 0x30–0x3F (ASCII 0–9:;<=>?), then by any number of "intermediate
;; bytes" in the range 0x20–0x2F (ASCII space and !"#$%&'()*+,-./), then finally
;; by a single "final byte" in the range 0x40–0x7E (ASCII @A–Z[\]^_`a–z{|}~).

(def csi-pattern #"(?s)([^\033]*)\033\[([\x30-\x3F]*[\x20-\x2F]*[\x40-\x7E])(.*)")

(def colors [:black :red :green :yellow :blue :magenta :cyan :white])

(def defaults
  {:foreground nil
   :background nil
   :bold false})

(defn parse-int [s]
  #?(:clj (Integer/parseInt s)
     :cljs (js/parseInt s 10)))

(defn code->attrs [code]
  (cond
    (= 0 code)        defaults
    (= 1 code)        {:bold true}
    (<= 30 code 39)   {:foreground (get colors (- code 30))}
    (<= 40 code 49)   {:background (get colors (- code 40))}
    (<= 90 code 99)   {:background (get colors (- code 90))
                       :bold       true}
    (<= 100 code 109) {:background (get colors (- code 40))
                       :bold       true}))

;; 0-  7:  standard colors (as in ESC [ 30–37 m)
;; 8- 15:  high intensity colors (as in ESC [ 90–97 m)
;; 16-231:  6 × 6 × 6 cube (216 colors): 16 + 36 × r + 6 × g + b (0 ≤ r, g, b ≤ 5)
;; 232-255:  grayscale from black to white in 24 steps
(defn color-8-bit [code]
  (cond
    (<= 0 code 7)     [(get colors code) false]
    (<= 8 code 15)    [(get colors (- code 8)) true]
    (<= 16 code 231) (let [code (- code 16)
                           blue (mod code 6)
                           green (mod (/ (- code blue) 6) 6)
                           red (/ (- code blue (* 6 green)) 36)
                           color-values [0x00 0x5f 0x87 0xaf 0xd7 0xff]]
                       [(into [:rgb] (map color-values) [red green blue])
                        false])
    (<= 232 code 255) (let [x (+ 8 (* 10 (- code 232)))]
                        [[:rgb x x x] false])))


[(= (color-8-bit 3)
    [:yellow false])

 (= (color-8-bit 12)
    [:blue true])

 (= (color-8-bit 97)
    [[:rgb 0x87 0x5f 0xaf] false])

 (= (color-8-bit 232)
    [[:rgb 8 8 8] false])

 (= (color-8-bit 244)
    [[:rgb 128 128 128] false])

 (= (color-8-bit 255)
    [[:rgb 238 238 238] false])]



(defn color-24-bit [[r g b]]
  [:rgb r g b])

(defn parse-color [fg-or-bg [colorspace & more]]
  (let [type (case fg-or-bg 38 :foreground 48 :background)]
    (case colorspace
      5 (let [[color bold?] (color-8-bit (first more))]
          [(merge {type color} (if bold? {:bold true}))
           (next more)])
      2 [{:foreground (color-24-bit (take 3 more))}
         (drop 3 more)])))

(defn csi->attrs [csi]
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

(defn token-stream [string]
  (loop [input string
         result []]
    (if-let [match (re-find csi-pattern input)]
      (let [[_ start csi tail] match]
        (recur tail
               (-> result
                   (cond-> #_result (seq start) (conj start))
                   (conj (csi->attrs csi)))))
      (conj result input))))

(comment
  (def sample
    (str "start of the string"
         "\033[31m this is red"
         "\033[45m magenta background"
         "\033[1m bold"
         "\033[32m green foreground"))


  (= (token-stream sample)

     ["start of the string"
      {:foreground :red}
      " this is red"
      {:background :magenta}
      " magenta background"
      {:bold true}
      " bold"
      {:foreground :green}
      " green foreground"]))

[(= (token-stream "\033[30;47m black on white")
    [{:foreground :black, :background :white} " black on white"])

 (= (token-stream "\033[1;31m bright red]")
    [{:bold true, :foreground :red} " bright red]"])

 (= (token-stream "\033[39;49m reset to defaults]")
    [{:foreground nil, :background nil} " reset to defaults]"])

 (= (token-stream "\033[0m reset all")
    [{:foreground nil, :background nil, :bold false} " reset all"])

 (= (token-stream "\033[91m bright red")
    [{:background :red, :bold true} " bright red"])]
