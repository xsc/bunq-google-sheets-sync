(ns bunq-google-sheets-sync.core
  (:gen-class)
  (:require [bunq-google-sheets-sync.bunq :as bunq]
            [bunq-google-sheets-sync.google :as google]
            [bunq-google-sheets-sync.google-sheets :as sheets]
            [aero.core :as aero]))

(defn- print-accounts!
  [accounts]
  (doseq [{:keys [name amount]} accounts]
    (println (format "%-30s\t%s" name amount))))

(defn run
  []
  (let [{:keys [bunq google google-sheets]}
        (aero/read-config "config.edn")
        bunq-context          (bunq/authorize! bunq)
        google-context        (google/authorize! google)
        accounts              (bunq/list-accounts bunq-context)]
    (print-accounts! accounts)
    (sheets/write-accounts! google-context google-sheets accounts)))

(defn -main
  [& _]
  (run)
  (System/exit 0))
