(ns om-pages.components.util
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))


(defn render-check [bool]
  (if bool (dom/i #js {:className "fa fa-check"} nil)))

(defn app-bar [cursor owner {:keys [title subtitle controls]}]
  (reify
    om/IRender
    (render [_]
      (dom/nav #js {:className "app-bar navbar navbar-default navbar-static-top"}
        (dom/div #js {:className "container-fluid"}
          (dom/div #js {:className "app-bar-title navbar-header"}
            (dom/a #js {:className "navbar-brand" :href (:link title)} (:text title)))
          (dom/p #js {:className "navbar-text"}
            (dom/a #js {:href (:link subtitle)} (:text subtitle)))
          (om/build controls cursor))))))

