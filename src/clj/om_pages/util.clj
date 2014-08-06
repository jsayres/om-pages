(ns om-pages.util
  (:require [clojure.data.json :as json]
            [ring.util.response :refer [content-type response]]))


;;;
; Creates a json response from Clojure data
;;;
(defn json-response [data]
  (-> (json/write-str data)
      (response)
      (content-type "application/json; charset=utf-8")))

