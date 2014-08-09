(ns om-pages.components.page-edit
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om-pages.util :refer [xhr-req]]
            [om-pages.components.modal :refer [set-modal close-modal]]
            [om-pages.components.util :refer [app-bar]]
            [om-pages.components.templates :refer [templates]]))


;;;
; App-bar controls
;;;
(defn update-page [edit-cursor]
  (let [content (.getContent (.-activeEditor js/tinymce) {:format "raw"})
        page (assoc-in (:page @edit-cursor) [:content] content)]
    (xhr-req
      {:method "PUT"
       :url (str "/api/pages/" (:id page))
       :data {:page page}
       :on-complete
       (fn [{:keys [updated error]}]
         (if (and (true? updated) (nil? error))
           (om/transact! edit-cursor #(assoc % :page page :dirty false))))})))

(defn edit-done [evt view-url]
  (.preventDefault evt)
  (.stopPropagation evt)
  (set-modal
    {:title "Unsaved Changes"
     :content "You have made changes to this page that have not
              yet been saved. How do you wish to proceed?"
     :options [{:text "Discard Changes"
                :style "danger"
                :action #(do (set! (.-hash js/location) view-url)
                             (close-modal))}
               {:text "Cancel"
                :action close-modal}]}))

(defn app-bar-controls [edit-cursor owner]
  (reify
    om/IRender
    (render [_]
      (let [btn-classes "btn btn-default btn-sm navbar-btn"
            dirty? (:dirty edit-cursor)
            view-url (str "#" (:page-id edit-cursor))]
        (dom/div #js {:className "navbar-right"}
          (when dirty?
            (dom/a #js {:className (str btn-classes " btn-primary")
                        :onClick (when dirty? #(update-page edit-cursor))}
              "Save"))
          (dom/a #js {:className btn-classes
                      :onClick (when dirty? #(edit-done % view-url))
                      :href view-url}
            "Done"))))))

;;;
; TinyMCE Options
;;;
(def tinymce-opts
  #js {:selector "#content"
       :schema "html5"
       :inline true
       :menu_bar false})

(defn set-dirty [editor edit-cursor]
  (let [start-content (.-startContent editor)
        current-content (.getContent editor #js {:format "raw"})]
    (.log js/console "checking dirty")
    (.log js/console start-content)
    (.log js/console current-content)
    (om/transact! edit-cursor :dirty #(not= start-content current-content))))


;;;
; Main view
;;;
(defn page-edit [edit-cursor owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (.init js/tinymce tinymce-opts)
      (let [editor (.-activeEditor js/tinymce)]
        (.on editor "blur" #(set-dirty editor edit-cursor))
        (.on editor "change" #(set-dirty editor edit-cursor))))
    om/IWillUnmount
    (will-unmount [_]
      (.remove js/tinymce "#content"))
    om/IRender
    (render [_]
      (let [{:keys [page-id page]} edit-cursor
            opts {:title {:text "Pages" :link "#"}
                  :subtitle {:text (:url page)}
                  :controls app-bar-controls}
            template (get templates (:template page "default"))]
        (dom/div nil
          (om/build app-bar edit-cursor {:opts opts})
          (dom/div #js {:id "toolbar"} nil)
          (if (not page)
            (dom/div #js {:className "loading-page"} nil)
            (dom/div #js {:id "page-edit"}
              (dom/div #js {:id "page-render"} (template page)))))))))
