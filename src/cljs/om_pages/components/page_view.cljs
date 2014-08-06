(ns om-pages.components.page-view
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om-pages.util :refer [xhr-req]]
            [om-pages.components.util :refer [app-bar]]
            [om-pages.components.templates :refer [templates]]))


;;;
; App-bar controls
;;;
(defn publish-page! [page-id view-cursor]
  (xhr-req
    {:method "PUT"
     :url (str "/api/pages/" page-id "/publish")
     :on-complete
     (fn [{:keys [published error]}]
       (if (and (true? published) (nil? error))
         (om/update! view-cursor :published page-id)))}))

(defn unpublish-page! [page-id view-cursor]
  (xhr-req
    {:method "PUT"
     :url (str "/api/pages/" page-id "/unpublish")
     :on-complete
     (fn [{:keys [published error]}]
       (if (and (false? published) (nil? error))
         (om/transact! view-cursor :published #(if (= page-id %) nil %))))}))

(defn app-bar-controls [view-cursor owner]
  (reify
    om/IRender
    (render [_]
      (let [{:keys [page-id published]} view-cursor
            published? (= page-id published)
            pub-text (if published? "Unpublish" "Publish")
            pub-action (if published? unpublish-page! publish-page!)
            btn-classes "btn btn-default btn-sm navbar-btn"]
        (dom/div #js {:className "navbar-right"}
          (dom/a #js {:className (str btn-classes (when published? " btn-danger"))
                      :onClick #(pub-action page-id view-cursor)} pub-text)
          (dom/a #js {:className btn-classes :href (str "#" page-id "/edit")} "Edit")
          (dom/a #js {:className btn-classes :href "#new"} "+"))))))


;;;
; Versions sidebar
;;;
(defn versions-sidebar [page-id versions]
  (let [get-date #(js/Date.parse (:date %))
        desc #(compare %2 %1)
        versions (sort-by get-date desc versions)]
    (dom/div #js {:id "page-versions"}
      (dom/h3 nil "Versions")
      (apply dom/ul nil
        (map
          (fn [{:keys [id date author]}]
            (dom/li (when (= id page-id) #js {:className "selected"})
              (dom/a #js {:href (str "#" id)} date (dom/br nil nil) author)))
          versions)))))


;;;
; Main view
;;;
(defn page-view [view-cursor owner]
  (reify
    om/IRender
    (render [_]
      (let [{:keys [page-id branch-id published versions]} view-cursor
            page (first (filter #(= (:id %) page-id) versions))
            template (get templates (:template page "default"))]
        (dom/div nil
          (let [opts {:title {:text "Pages" :link "#"}
                      :subtitle {:text (:url page)}
                      :controls app-bar-controls}]
            (om/build app-bar view-cursor {:opts opts}))
          (if (not page)
            (dom/div #js {:className "loading-page"} nil)
            (dom/div #js {:id "page-view"}
              (dom/div #js {:id "page-render"} (template page))
              (versions-sidebar page-id versions))))))))
