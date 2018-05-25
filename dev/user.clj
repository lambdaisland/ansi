(ns user)

(defn add-dependency [dep-vec]
  (require 'cemerick.pomegranate)
  ((resolve 'cemerick.pomegranate/add-dependencies)
   :coordinates [dep-vec]
   :repositories (merge @(resolve 'cemerick.pomegranate.aether/maven-central)
                        {"clojars" "https://clojars.org/repo"})))
