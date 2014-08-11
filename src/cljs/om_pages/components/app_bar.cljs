(ns om-pages.components.app-bar
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))


(defn app-bar [cursor owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [attrs children]}]
      (let [{:keys [title href]} attrs]
        (dom/nav #js {:className "app-bar navbar navbar-default navbar-static-top"}
          (apply dom/div #js {:className "container-fluid"}
            (dom/div #js {:className "app-bar-title navbar-header"}
              (dom/a #js {:className "navbar-brand" :href href} title))
            children))))))
