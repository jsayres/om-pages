(ns om-pages.components.page-view
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om-pages.util :refer [xhr-req]]
            [om-pages.components.util :refer [domify]]
            [om-pages.components.app-bar :refer [app-bar]]
            [om-pages.components.templates :refer [templates]]))


(defn publish-page [page-id view-cursor]
  (xhr-req
    {:method "PUT"
     :url (str "/api/pages/" page-id "/publish")
     :on-complete
     (fn [{:keys [published error]}]
       (if (and (true? published) (nil? error))
         (om/update! view-cursor :published page-id)))}))

(defn unpublish-page [page-id view-cursor]
  (xhr-req
    {:method "PUT"
     :url (str "/api/pages/" page-id "/unpublish")
     :on-complete
     (fn [{:keys [published error]}]
       (if (and (false? published) (nil? error))
         (om/transact! view-cursor :published #(if (= page-id %) nil %))))}))

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

(defn page-view [view-cursor owner]
  (reify
    om/IRender
    (render [_]
      (let [{:keys [page-id branch-id published versions]} view-cursor
            page (some #(when (= (:id %) page-id) %) versions)
            btn-classes "btn btn-default btn-sm navbar-btn"
            template (get templates (:template page "default"))
            dom-app-bar (domify app-bar view-cursor)]
        (dom/div nil
          (dom-app-bar {:title "Pages" :href "#"}
            (dom/p #js {:className "navbar-text"} (:url page))
            (dom/div #js {:className "navbar-right"}
              (if (= page-id published)
                (dom/a #js {:onClick #(unpublish-page page-id view-cursor)
                            :className btn-classes} "Unpublish")
                (dom/a #js {:onClick #(publish-page page-id view-cursor)
                            :className btn-classes} "Publish"))
              (dom/a #js {:className btn-classes :href (str "#" page-id "/edit")} "Edit")
              (dom/a #js {:className btn-classes :href "#new"} "+")))
          (if (not page)
            (dom/div #js {:className "loading-page"} nil)
            (dom/div #js {:id "page-view"}
              (dom/div #js {:id "page-render"} (template page))
              (versions-sidebar page-id versions))))))))
