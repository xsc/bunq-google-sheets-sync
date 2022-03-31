(defproject bunq-google-sheets-sync "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "https://github.com/xsc/bunq-google-sheets-sync"
  :license {:name "MIT"
            :url "https://choosealicense.com/licenses/mit"
            :comment "MIT License"
            :year 2022
            :key "mit"}
  :dependencies [[org.clojure/clojure "1.10.3"]

                 ;; Google
                 [com.google.api-client/google-api-client "1.33.4"]
                 [com.google.http-client/google-http-client "1.41.5"]
                 [com.google.http-client/google-http-client-jackson2 "1.41.5"]
                 [com.google.apis/google-api-services-sheets "v4-rev12-1.22.0"]
                 [com.google.oauth-client/google-oauth-client "1.33.1"]
                 [com.google.oauth-client/google-oauth-client-jetty "1.33.1"]

                 ;; Bunq
                 [com.github.bunq/sdk_java "1.19.1.3"]

                 ;; Utilities
                 [com.google.code.gson/gson "2.9.0"]
                 [aero "1.1.6"]]
  :repositories {"jitpack" {:url "https://jitpack.io"}}
  :main bunq-google-sheets-sync.core
  :profiles {:dev {:global-vars {*warn-on-reflection* true}}}
  :pedantic? :abort)
