(ns lambdaisland.ansi.cljs-test-runner
  (:gen-class)
  (:require [doo.core :as doo]
            [cljs.build.api :as cljs]))

(def cljs-config {:main 'lambdaisland.ansi-test
                  :output-to "out/testable.js"
                  :output-dir "out"
                  :optimizations :simple
                  :target :nodejs})

(defn -main [& args]
  (cljs/build ["src" "test"] cljs-config)
  (let [{:keys [exit] :as res}
        (doo/run-script :node cljs-config {:debug true})]
    (System/exit exit)))
