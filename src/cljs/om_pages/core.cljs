(ns om-pages.core
  (:require [goog.dom :as gdom]
            [clojure.string :as s]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om-pages.util :refer [xhr-req]]
            [om-pages.components.modal :refer [modal]]
            [om-pages.components.page-list :refer [page-list]]
            [om-pages.components.page-view :refer [page-view]]
            [om-pages.components.page-edit :refer [page-edit]]))


(def app-data (atom {:mode :list
                     :list {:pages []
                            :sort-on :url
                            :sort-dir :asc}
                     :view {:page-id nil
                            :branch-id nil
                            :published nil
                            :versions []}
                     :edit {:page-id nil
                            :page nil
                            :dirty false}
                     :message {:title nil
                               :content nil}}))

(defn reload-page-list-data [app-cursor]
  (xhr-req {:method "GET"
            :url "/api/pages"
            :on-complete
            (fn [{:keys [pages]}] (om/update! app-cursor [:list :pages] pages))}))

(defn reload-page-view-data [app-cursor page-id]
  (xhr-req
    {:method "GET"
     :url (str "/api/pages/" page-id)
     :on-complete
     (fn [{:keys [branch-id published versions]}]
       (om/transact! app-cursor :view #(assoc %
                                              :branch-id branch-id
                                              :published published
                                              :versions versions)))}))

(defn reload-page-edit-data [app-cursor page-id]
  (xhr-req
    {:method "GET"
     :url (str "/api/pages/" page-id)
     :on-complete
     (fn [{:keys [versions]}]
       (om/transact! app-cursor [:edit :page] #(some (fn [page] (when (= (:id page) page-id) page)) versions)))}))

(defn list-pages [app-cursor]
  (om/update! app-cursor :mode :list)
  (reload-page-list-data app-cursor))

(defn view-page [app-cursor page-id]
  (om/transact! app-cursor #(-> (assoc % :mode :view)
                                (assoc-in [:view :page-id] page-id)))
  (reload-page-view-data app-cursor page-id))

(defn edit-page [app-cursor page-id]
  (om/transact! app-cursor
    (fn [app]
      (let [view-data (:view app)
            page (some #(when (= (:id %) (:page-id view-data)) %) (:versions view-data))]
        (-> (assoc app :mode :edit)
            (assoc-in [:edit :page-id] page-id)
            (assoc-in [:edit :page] (when (= (:id page) page-id) page))))))
  (reload-page-edit-data app-cursor page-id))

(defn set-view-from-route [app-cursor]
  (let [route (s/replace (.-hash js/location) #"^#" "")]
    (condp re-find route
      #"^$" :>> #(list-pages app-cursor)
      #"^\d+$" :>> #(view-page app-cursor (js/parseInt %))
      #"^\d+/edit$" :>> #(edit-page app-cursor (js/parseInt %)))))

(defn watch-routes [app-cursor]
  (set! (.-onhashchange js/window) #(set-view-from-route app-cursor)))

(defn pages-app [app-cursor owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (set-view-from-route app-cursor)
      (watch-routes app-cursor))
    om/IRender
    (render [_]
      (dom/div nil
        (case (:mode app-cursor)
          :list (om/build page-list (:list app-cursor))
          :view (om/build page-view (:view app-cursor))
          :edit (om/build page-edit (:edit app-cursor)))))))


(om/root pages-app app-data {:target (gdom/getElement "pages-app")})
(om/root modal {} {:target (gdom/getElement "modal")})

