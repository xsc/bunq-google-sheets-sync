(ns bunq-google-sheets-sync.core
  (:gen-class)
  (:require [bunq-google-sheets-sync.bunq :as bunq]
            [bunq-google-sheets-sync.google :as google]
            [bunq-google-sheets-sync.google-sheets :as sheets]
            [aero.core :as aero]))

(defn- fetch-accounts!
  [{:keys [bunq]}]
  (->> (bunq/authorize! bunq)
       (bunq/list-accounts)))

(defn- print-accounts!
  [accounts]
  (doseq [{:keys [name amount]} accounts]
    (println (format "%-30s\t%s" name amount))))

(defn- write-accounts!
  [{:keys [google google-sheets]} accounts]
  (->> #(sheets/write-accounts! % google-sheets accounts)
       (google/authorize-and-run! google)))

(defn run
  []
  (let [config   (aero/read-config "config.edn")
        accounts (fetch-accounts! config)]
    (print-accounts! accounts)
    (write-accounts! config accounts)))

(defn -main
  [& _]
  (run)
  (System/exit 0))
