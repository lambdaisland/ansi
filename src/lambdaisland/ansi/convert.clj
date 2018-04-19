(ns lambdaisland.ansi.convert
  (:gen-class)
  (:require [lambdaisland.ansi :as ansi]
            [hiccup.core :as hiccup]))

(def colors
  {:black   [0 0 0]
   :red     [170 0 0]
   :green   [0 170 0]
   :yellow  [170 85 0]
   :blue    [0 0 170]
   :magenta [170 0 170]
   :cyan    [0 170 170]
   :white   [170 170 170]})

(def colors-css
  (->> colors
       (map (fn [[c [r g b]]]
              (str "." (name c) " { color: rgb(" r ", " g ", " b "); } \n"
                   ".bg-" (name c) " { background-color: rgb(" r ", " g ", " b "); } \n")))
       (apply str)))

(defn layout [content]
  [:html
   [:head
    [:style {:type "text/css"}
     (str ".pre { white-space: pre; font-family: monospace } \n"
          ".bold { font-weight: bold; } \n"
          colors-css)]]
   [:body [:div.pre content]]])

(defn -main [fname]
  (print
   (hiccup/html (layout
                 (ansi/ansi->hiccup (slurp fname))))))
