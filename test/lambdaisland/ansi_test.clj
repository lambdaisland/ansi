(ns lambdaisland.ansi-test
  (:require [clojure.test :refer :all]
            [lambdaisland.ansi :as ansi :refer :all]))

(deftest ansi->hiccup-test
  (is (= [[:span {:class "red"} "hello"]]
         (ansi->hiccup "\033[31mhello")))

  (is (= [[:span {:class "red"} " this is red"]
          [:span {:class "red bg-magenta"} " magenta background"]
          [:span {:class "bold red bg-magenta"} " bold"]
          [:span {:class "bold green bg-magenta"} " green foreground"]]
         (ansi->hiccup (str "\033[31m this is red"
                            "\033[45m magenta background"
                            "\033[1m bold"
                            "\033[32m green foreground")))))

(deftest scan-test
  (are [x y] (= (scan x) y)
    ""
    [["0m" ""]]

    "hello"
    [["0m" "hello"]]

    "foo\nbar"
    [["0m" "foo\nbar"]]

    "\033[5mhello"
    [["0m" ""] ["5m" "hello"]]

    "\033[5;3mhello"
    [["0m" ""] ["5;3m" "hello"]]

    "\033[33mhello\033[0;39mworld"
    [["0m" ""] ["33m" "hello"] ["0;39m" "world"]]))

(deftest code->attrs-test
  (are [x y] (= (code->attrs x) y)
    ""  ansi/defaults
    "0" ansi/defaults
    "1" {:bold true}
    "30" {:foreground :black}
    "40" {:background :black}
    "45" {:background :magenta}))

(deftest csi->attrs-test
  (is (= (csi->attrs "0m")
         ansi/defaults))

  (is (= (csi->attrs "33;45;1m")
         {:foreground :yellow
          :background :magenta
          :bold true})))

(deftest attrs->classes-test
  (are [x y] (= (attrs->classes x) y)
    {}                     []
    {:bold true}           [:bold]
    {:foreground :blue}    [:blue]
    {:background :magenta} [:bg-magenta]
    {:bold true
     :foreground :yellow
     :background :black} [:bold :yellow :bg-black]))
