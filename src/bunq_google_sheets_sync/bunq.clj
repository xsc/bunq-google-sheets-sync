(ns bunq-google-sheets-sync.bunq
  (:require [bunq-google-sheets-sync.oauth2 :as oauth2]
            [clojure.string :as string])
  (:import (com.google.api.client.auth.oauth2
             AuthorizationCodeFlow
             AuthorizationCodeFlow$Builder
             BearerToken
             Credential)
           (com.google.api.client.http
             GenericUrl
             HttpExecuteInterceptor
             HttpRequest
             UrlEncodedContent)
           (com.google.api.client.util
             Data);
           (com.bunq.sdk.context ApiContext ApiEnvironmentType BunqContext)
           (com.bunq.sdk.model.generated.endpoint MonetaryAccount)))

;; ## OAuth

(def ^:private +bunq-credentials+
  ".credentials/bunq_credentials")

(def ^:private +bunq-token-url+
  (GenericUrl. "https://api.oauth.bunq.com/v1/token"))

(def ^:private +bunq-auth-url+
  "https://oauth.bunq.com/auth")

(defn- create-query-authorization
  "Bunq's token request expects information in GET query parameters, but the
   default behaviour of this OAuth client is to put it into the POST body
   (which, to me, makes more sense anyways, but well...).

   This request interceptor moves the parameters from the body to the URL."
  [client-id client-secret]
  (proxy [HttpExecuteInterceptor] []
    (intercept [^HttpRequest request]
      (let [^java.util.Map data (-> (UrlEncodedContent/getContent request)
                                    (.getData)
                                    (Data/mapOf))
            url (->> (for [[k v] data]
                       (str k "=" v))
                     (cons (str "client_id=" client-id))
                     (cons (str "client_secret=" client-secret))
                     (string/join "&")
                     (str +bunq-token-url+ "?")
                     (GenericUrl.))]
        (.setUrl request url)
        (.clear data)))))

(defn- create-authorization-flow
  ^AuthorizationCodeFlow
  [{:keys [client-id client-secret]}]
  (let [store (oauth2/create-credential-store +bunq-credentials+)]
    (-> (AuthorizationCodeFlow$Builder.
          (BearerToken/authorizationHeaderAccessMethod)
          oauth2/+transport+
          oauth2/+json+
          +bunq-token-url+
          (create-query-authorization client-id client-secret)
          client-id
          +bunq-auth-url+)
        (.setCredentialDataStore store)
        (.build))))

(defn- oauth-authorize!
  ^Credential
  [secrets]
  (-> (create-authorization-flow secrets)
      (oauth2/authorize-local! 18888)))

;; ## API Context

(def ^:private +bunq-api-context+ ".credentials/bunq_api")

(defn- create-api-context
  [api-key]
  (try
    (ApiContext/restore +bunq-api-context+)
    (catch Exception _
      (doto (ApiContext/create
              ApiEnvironmentType/PRODUCTION
              api-key
              "bunq-google-sheets-sync")
        (.save +bunq-api-context+)))))

(defn authorize!
  [opts]
  (doto (->> (oauth-authorize! opts)
             (.getAccessToken)
             (create-api-context))
    (BunqContext/loadApiContext)))

;; ## Endpoints

(defn list-accounts
  [_]
  (->> (for [record (.. (MonetaryAccount/list) (getValue))
             :let [obj (.getReferencedObject record)]
             :when (= "ACTIVE" (.getStatus obj))]
         {:name   (.getDescription obj)
          :amount (.. obj getBalance getValue)})
       (sort-by :name)))

