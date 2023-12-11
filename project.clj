(defproject bunq-google-sheets-sync "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "https://github.com/xsc/bunq-google-sheets-sync"
  :license {:name "MIT"
            :url "https://choosealicense.com/licenses/mit"
            :comment "MIT License"
            :year 2022
            :key "mit"}
  :dependencies [[org.clojure/clojure "1.11.1"]

                 ;; Google
                 [com.google.api-client/google-api-client "1.35.2"]
                 [com.google.http-client/google-http-client "1.43.3"]
                 [com.google.http-client/google-http-client-jackson2 "1.43.3"]
                 [com.google.apis/google-api-services-sheets "v4-rev12-1.22.0"]
                 [com.google.oauth-client/google-oauth-client "1.34.1"]
                 [com.google.oauth-client/google-oauth-client-jetty "1.34.1"]

                 ;; Bunq
                 [com.github.bunq/sdk_java "1.19.11.48"]

                 ;; Utilities
                 [org.apache.httpcomponents/httpclient "4.5.14"]
                 [org.apache.httpcomponents/httpcore "4.4.16"]
                 [com.google.code.gson/gson "2.10.1"]
                 [aero "1.1.6"]]
  :repositories {"jitpack" {:url "https://jitpack.io"}}
  :main bunq-google-sheets-sync.core
  :profiles {:dev {:global-vars {*warn-on-reflection* true}}}
  :pedantic? :abort)
