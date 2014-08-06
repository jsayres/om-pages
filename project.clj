(defproject om-pages "0.1.0-SNAPSHOT"
  :description "Simple CMS with om frontend"
  :url "http://ciese.org"

  :jvm-opts ^:replace ["-Xmx1g" "-server"]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2268"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [org.clojure/data.json "0.2.5"]
                 [om "0.6.5"]
                 [ring "1.3.0"]
                 [compojure "1.1.8"]]

  :plugins [[lein-ring "0.8.11"]
            [lein-cljsbuild "1.0.4-SNAPSHOT"]]

  :source-paths ["src/clj" "src/cljs"]
  :resource-paths ["resources"]
  :main om-pages.core

  :ring {:handler om-pages.core/routes
         :auto-reload? true}

  :cljsbuild {
    :builds [{:id "dev"
              :source-paths ["src/clj" "src/cljs"]
              :compiler {
                :output-to "resources/public/js/om_pages.js"
                :output-dir "resources/public/js/out"
                :optimizations :none
                :source-map true}}]})
