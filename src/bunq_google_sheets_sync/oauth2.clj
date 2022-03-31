(ns bunq-google-sheets-sync.oauth2
  (:require [clojure.java.io :as io])
  (:import (com.google.api.client.auth.oauth2
             AuthorizationCodeFlow
             Credential
             TokenResponseException)
           (com.google.api.client.util.store
             DataStore)
           (com.google.api.client.googleapis.javanet
             GoogleNetHttpTransport)
           (com.google.api.client.json.jackson2
             JacksonFactory)
           (com.google.api.client.util.store
             FileDataStoreFactory)
           (com.google.api.client.extensions.jetty.auth.oauth2
             LocalServerReceiver$Builder)
           (com.google.api.client.extensions.java6.auth.oauth2
             AuthorizationCodeInstalledApp)))

;; ## Global Objects

(def +transport+
  (GoogleNetHttpTransport/newTrustedTransport))

(def +json+
  (JacksonFactory/getDefaultInstance))

;; ## Credential Store

(defn create-credential-store
  ^DataStore
  [path]
  (let [credential-file (io/file path)]
    (-> (FileDataStoreFactory.
          (.getParentFile credential-file))
        (.getDataStore (.getName credential-file)))))

;; ## Local Authorization

(defn authorize!
  ^Credential
  ([flow]
   (authorize! flow -1))
  ([^AuthorizationCodeFlow flow port]
   (-> (AuthorizationCodeInstalledApp.
         flow
         (-> (LocalServerReceiver$Builder.)
             (.setPort (int port))
             (.build)))
       (.authorize "user"))))

(defn unauthorize!
  [^AuthorizationCodeFlow flow]
  (-> (.getCredentialDataStore flow)
      (.delete "user")))

(defn authorize-and-run!
  ([flow f]
   (authorize-and-run! flow f -1))
  ([flow f port]
   (try
     (f (authorize! flow port))
     (catch TokenResponseException _
       (unauthorize! flow)
       (f (authorize! flow port))))))
