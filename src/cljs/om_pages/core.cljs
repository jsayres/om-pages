(ns om-pages.core
  (:require [goog.dom :as gdom]
            [clojure.string :as s]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om-pages.util :refer [xhr-req]]
            [om-pages.components.page-list :refer [page-list]]
            [om-pages.components.page-view :refer [page-view]]))


(def app-data (atom {:mode :list
                     :list {:pages []
                            :sort-on :url
                            :sort-dir :asc}
                     :view {:page-id nil
                            :branch-id nil
                            :published nil
                            :versions []}}))

(defn reload-page-list-data! [app-cursor]
  (xhr-req {:method "GET"
            :url "/api/pages"
            :on-complete
            (fn [{:keys [pages]}] (om/update! app-cursor [:list :pages] pages))}))

(defn reload-page-view-data! [app-cursor page-id]
  (xhr-req
    {:method "GET"
     :url (str "/api/pages/" page-id)
     :on-complete
     (fn [{:keys [branch-id published versions]}]
       (om/transact! app-cursor :view #(assoc % 
                                       :branch-id branch-id
                                       :published published
                                       :versions versions)))}))

(defn list-pages! [app-cursor]
  (om/update! app-cursor :mode :list)
  (reload-page-list-data! app-cursor))

(defn view-page! [app-cursor page-id]
  (om/transact! app-cursor #(-> (assoc % :mode :view)
                                (assoc-in [:view :page-id] page-id)))
  (reload-page-view-data! app-cursor page-id))

(defn set-view-from-route! [app-cursor]
  (let [route (s/replace (.-hash js/location) #"^#" (fn [_] ""))]
    (condp re-find route
      #"^$" :>> #(list-pages! app-cursor)
      #"^\d+$" :>> #(view-page! app-cursor (js/parseInt %)))))

(defn watch-routes [app-cursor]
  (set! (.-onhashchange js/window) #(set-view-from-route! app-cursor)))

(defn pages-app [app-cursor owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (set-view-from-route! app-cursor)
      (watch-routes app-cursor))
    om/IRender
    (render [_]
      (dom/div nil
        (case (:mode app-cursor)
          :list (om/build page-list (:list app-cursor))
          :view (om/build page-view (:view app-cursor)))))))

(om/root pages-app app-data {:target (gdom/getElement "pages-app")})
