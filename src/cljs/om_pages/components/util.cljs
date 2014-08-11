(ns om-pages.components.util
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))


(defn render-check [bool]
  (if bool (dom/i #js {:className "fa fa-check"} nil)))

(defn domify
  ([component cursor] (domify component cursor {}))
  ([component cursor m]
    (fn [attrs & children]
      (om/build component cursor (assoc m :state {:attrs attrs
                                                  :children children})))))

(defn domify-all
  ([component cursors] (domify-all component cursors {}))
  ([component cursors m] (map domify cursors (repeat m))))


(defn check-link [cursor owner {:keys [attrs children]}]
  (reify
    om/IRender
    (render [_]
      (apply dom/a attrs "√ " children) )))

(defn stuff [cursor owner]
  (reify
    om/IRender
    (render [_]
      (let [dom-check-link (domify check-link cursor)]
        (dom-check-link #js {:href "javascript:alert('Hello world!');"}
          "Click here!")))))
