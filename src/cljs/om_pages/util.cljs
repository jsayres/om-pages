(ns om-pages.util
  (:require [clojure.string :as s]
            [goog.events :as events])
  (:import [goog.net XhrIo]
           goog.net.EventType
           [goog.events EventType]))


(defn title [txt]
  (s/replace txt #"\b." #(.toUpperCase %)))

(defn kw->title [kw]
  (-> (name kw) (s/replace #"-" " ") (title)))

(defn xhr->clj [xhr]
  (-> (.getResponseText xhr) (js/JSON.parse) (js->clj :keywordize-keys true)))

(defn xhr-req [{:keys [method url data on-complete]}]
  (let [xhr (XhrIo.)]
    (events/listen xhr goog.net.EventType.COMPLETE #(on-complete (xhr->clj xhr)))
    (. xhr
       (send url method (when data (.stringify js/JSON (clj->js data)))
             #js {"Content-Type" "application/json"}))))
