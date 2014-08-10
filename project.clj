(defproject om-pages "0.1.0-SNAPSHOT"
  :description "Simple CMS with om frontend"
  :url "http://ciese.org"

  :jvm-opts ^:replace ["-Xmx1g" "-server"]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2268"]
                 [org.clojure/core.async "0.1.319.0-6b1aca-alpha"]
                 [org.clojure/data.json "0.2.5"]
                 [om "0.7.1"]
                 [ring "1.3.0"]
                 [ring/ring-json "0.3.1"]
                 [compojure "1.1.8"]]

  :plugins [[lein-ring "0.8.11"]
            [lein-cljsbuild "1.0.4-SNAPSHOT"]]

  :source-paths ["src/clj" "src/cljs"]
  :resource-paths ["resources"]
  :main om-pages.core

  :ring {:handler om-pages.core/app
         :auto-reload? true}

  :cljsbuild {
    :builds [{:id "dev"
              :source-paths ["src/clj" "src/cljs"]
              :compiler {
                :output-to "resources/public/js/om_pages.js"
                :output-dir "resources/public/js/out"
                :optimizations :none
                :source-map true}}
             {:id "prod"
              :source-paths ["src/clj" "src/cljs"]
              :compiler {
                :output-to "resources/public/js/om_pages.js"
                :output-dir "resources/public/js"
                :optimizations :advanced
                :pretty-print false
                :preamble ["react/react.min.js"]
                :externs ["react/externs/react.js" "lib/externs/tinymce.js"]
                :source-map "resources/public/js/om_pages.js.map"}}]})
