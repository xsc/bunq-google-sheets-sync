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

(defn- create-request
  ^Request
  [^UpdateCellsRequest update-request]
  (doto (Request.)
    (.setUpdateCells update-request)))

(defn- execute-update!
  [service {:keys [spreadsheet-id]} request]
  (-> service
      (.spreadsheets)
      (.batchUpdate
        spreadsheet-id
        (-> (BatchUpdateSpreadsheetRequest.)
            (.setRequests [request])))
      (.execute)))

(defn write-accounts!
  "Given Google credentials, and a map with `:spreadsheet-id` and `:sheet-title`,
   update the sheet with rows of account name and balance."
  [ctx opts accounts]
  (let [service     (create-sheets-service ctx)
        spreadsheet (find-spreadsheet service opts)
        sheet-id    (find-sheet-id spreadsheet opts)
        request     (create-request (create-update-request sheet-id accounts))]
    (execute-update! service opts request)))
