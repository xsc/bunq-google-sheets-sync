(ns bunq-google-sheets-sync.google-sheets
  (:require [bunq-google-sheets-sync.oauth2 :as oauth2])
  (:import (com.google.api.services.sheets.v4
             Sheets
             Sheets$Builder)
           (com.google.api.services.sheets.v4.model
             BatchUpdateSpreadsheetRequest
             CellData
             CellFormat
             ExtendedValue
             GridCoordinate
             GridRange
             NumberFormat
             Request
             RowData
             UpdateCellsRequest)))

;; ## Service

(defn- create-sheets-service
  ^Sheets
  [ctx]
  (-> (Sheets$Builder.
        oauth2/+transport+
        oauth2/+json+
        ctx)
      (.setApplicationName "bunq-google-sheets-sync")
      (.build)))

;; ## Sheet Manipulation

(defn- find-spreadsheet
  [^Sheets service {:keys [spreadsheet-id]}]
  (-> (.spreadsheets service)
      (.get spreadsheet-id)
      (.execute)))

(defn- find-sheet-id
  [spreadsheets {:keys [sheet-title]}]
  (some-> (->> (get spreadsheets "sheets")
               (map #(get % "properties"))
               (filter #(= sheet-title (get % "title")))
               (first))
          (get "sheetId")))

(defn- as-string-cell
  ^CellData
  [s]
  (-> (CellData.)
      (.setUserEnteredValue
        (-> (ExtendedValue.)
            (.setStringValue s)))))

(let [currency-format (doto (CellFormat.)
                        (.setNumberFormat
                          (-> (NumberFormat.)
                              (.setType "CURRENCY"))))]
  (defn- as-currency-cell
    ^CellData
    [s]
    (-> (CellData.)
        (.setUserEnteredValue
          (-> (ExtendedValue.)
              (.setNumberValue
                (Double/parseDouble s))))
        (.setUserEnteredFormat currency-format))))

(defn- create-update-request
  ^UpdateCellsRequest
  [sheet-id accounts]
  (let [origin (-> (GridCoordinate.)
                   (.setSheetId sheet-id)
                   (.setRowIndex (int 0))
                   (.setColumnIndex (int 0)))]
    (doto (UpdateCellsRequest.)
      (.setStart origin)
      (.setRows
        (for [{:keys [name amount]} accounts]
          (-> (RowData.)
              (.setValues
                [(as-string-cell name)
                 (as-currency-cell amount)]))))
      (.setFields "userEnteredValue,userEnteredFormat"))))

(defn- create-clear-request
  ^UpdateCellsRequest
  [sheet-id]
  (doto (UpdateCellsRequest.)
    (.setRange
      (doto (GridRange.)
        (.setSheetId sheet-id)))
    (.setFields "*")))

(defn- create-request
  ^Request
  [^UpdateCellsRequest update-request]
  (doto (Request.)
    (.setUpdateCells update-request)))

(defn- execute-update!
  [^Sheets service {:keys [spreadsheet-id]} requests]
  (-> service
      (.spreadsheets)
      (.batchUpdate
        spreadsheet-id
        (-> (BatchUpdateSpreadsheetRequest.)
            (.setRequests (map create-request requests))))
      (.execute)))

(defn write-accounts!
  "Given Google credentials, and a map with `:spreadsheet-id` and `:sheet-title`,
  update the sheet with rows of account name and balance."
  [ctx opts accounts]
  (let [service     (create-sheets-service ctx)
        spreadsheet (find-spreadsheet service opts)
        sheet-id    (find-sheet-id spreadsheet opts)]
    (->> [(create-clear-request sheet-id)
          (create-update-request sheet-id accounts)]
         (execute-update! service opts))))
