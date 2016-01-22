(defproject onyx-http "0.8.4.0-SNAPSHOT"
  :description "Onyx plugin for http"
  :url "https://github.com/vsolovyov/onyx-http"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/core.async "0.2.374"]
                 [org.onyxplatform/onyx "0.8.4"]
                 [cc.qbits/jet "0.7.3"]]
  :profiles {:dev {:dependencies []
                   :plugins []}})
