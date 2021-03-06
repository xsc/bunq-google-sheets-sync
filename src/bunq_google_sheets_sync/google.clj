(ns bunq-google-sheets-sync.google
  (:require [bunq-google-sheets-sync.oauth2 :as oauth2])
  (:import (com.google.api.client.googleapis.auth.oauth2
             GoogleAuthorizationCodeFlow
             GoogleAuthorizationCodeFlow$Builder
             GoogleClientSecrets
             GoogleClientSecrets$Details)
           (com.google.api.client.auth.oauth2
             Credential)
           (com.google.api.services.sheets.v4
             SheetsScopes)))

;; ## Auth

(def ^:private +google-credentials+
  ".credentials/google_credentials")

(def ^:private +scopes+
  [SheetsScopes/SPREADSHEETS])

(defn- as-google-client-secrets
  ^GoogleClientSecrets
  [{:keys [client-id client-secret redirect-uris]}]
  (let [details (doto (GoogleClientSecrets$Details.)
                  (.setClientId client-id)
                  (.setClientSecret client-secret)
                  (.setRedirectUris (vec redirect-uris)))]
    (doto (GoogleClientSecrets.)
      (.setInstalled details))))

(defn- create-authorization-flow
  ^GoogleAuthorizationCodeFlow
  [^GoogleClientSecrets secrets]
  (let [store (oauth2/create-credential-store +google-credentials+)]
    (-> (GoogleAuthorizationCodeFlow$Builder.
          oauth2/+transport+
          oauth2/+json+
          secrets
          +scopes+)
        (.setAccessType "offline")
        (.setCredentialDataStore store)
        (.build))))

(defn authorize-and-run!
  "Given a map of `:client-id`, `:client-secret` and `:redirect-uris`, this
   will initiate a local authorization flow, opening a browser window for
   confirmation, and persisting the credentials locally for reuse."
  ^Credential
  [secrets f]
  (-> (as-google-client-secrets secrets)
      (create-authorization-flow)
      (oauth2/authorize-and-run! f 18887)))
