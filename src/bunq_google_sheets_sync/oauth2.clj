(ns bunq-google-sheets-sync.oauth2
  (:require [clojure.java.io :as io])
  (:import (com.google.api.client.auth.oauth2
             AuthorizationCodeFlow
             AuthorizationCodeFlow$Builder
             Credential
             DataStoreCredentialRefreshListener)
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

(def ^String +user+ "user")

;; ## Credential Store

(defn- create-credential-store
  ^DataStore
  [path]
  (let [credential-file (io/file path)]
    (-> (FileDataStoreFactory.
          (.getParentFile credential-file))
        (.getDataStore (.getName credential-file)))))

(defn- create-refresh-listener
  ^DataStoreCredentialRefreshListener
  [^DataStore store]
  (DataStoreCredentialRefreshListener. +user+ store))

(defn set-credential-store
  ^AuthorizationCodeFlow$Builder
  [^AuthorizationCodeFlow$Builder builder path]
  (let [store (create-credential-store path)
        listener (create-refresh-listener store)]
    (-> builder
        (.setCredentialDataStore store)
        (.addRefreshListener listener))))

;; ## Local Authorization

(defn authorize-local!
  ^Credential
  ([flow]
   (authorize-local! flow -1))
  ([^AuthorizationCodeFlow flow port]
   (-> (AuthorizationCodeInstalledApp.
         flow
         (-> (LocalServerReceiver$Builder.)
             (.setPort (int port))
             (.build)))
       (.authorize +user+))))
