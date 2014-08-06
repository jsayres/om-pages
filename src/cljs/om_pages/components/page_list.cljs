(ns om-pages.components.page-list
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om-pages.util :refer [kw->title]]
            [om-pages.components.util :refer [app-bar render-check]]))

;;;
; App-bar controls
;;;
(defn app-bar-controls [list-cursor owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:className "navbar-right"}
        (dom/a #js {:className "btn btn-default btn-sm navbar-btn" :href "#new"}
          "+")))))


;;;
; Page list table helpers
;;;
(def fields [:url :template :author :date :published :prev-published])

(defn toggle-sort-dir [dir]
  (if (= dir :asc) :desc :asc))

(defn set-sort! [new-sort-on sort-on sort-dir list-cursor]
  (let [new-sort-dir (if (= new-sort-on sort-on) (toggle-sort-dir sort-dir) :asc)]
    (om/transact! list-cursor
                  #(assoc % :sort-on new-sort-on :sort-dir new-sort-dir))))

(defn header-row [list-cursor owner]
  (reify
    om/IRender
    (render [_]
      (let [{:keys [sort-on sort-dir]} list-cursor
            set-sort! #(set-sort! % sort-on sort-dir list-cursor)]
        (apply dom/tr nil
          (map
            (fn [field]
              (dom/th nil
                (dom/a #js {:onClick #(set-sort! field)} (kw->title field))
                (dom/span (when (not= field sort-on) #js {:className "invisible"})
                  (dom/i #js {:className (str "fa fa-caret-"
                                              (if (= sort-dir :asc) "up" "down"))}))))
            fields))))))

(defn row [page owner]
  (reify
    om/IRender
    (render [_]
      (apply dom/tr #js {:onClick #(set! (.-hash js/location) (str "#" (:id @page)))}
        (let [page (reduce #(update-in %1 [%2] render-check)
                         page
                         [:published :prev-published])]
          (map #(dom/td nil (% page)) fields))))))

(def asc compare)
(def desc #(compare %2 %1))
(def sort-key-fns {:date js/Date.parse :published not :prev-published not})

(defn sort-pages [{:keys [pages sort-on sort-dir]}]
  (sort-by (comp (sort-on sort-key-fns identity) sort-on)
           (if (= :asc sort-dir) asc desc)
           pages))


;;;
; Main view
;;;
(defn page-list [list-cursor owner]
  (reify
    om/IRender
    (render [_]
      (dom/div nil
        (let [opts {:title {:text "Pages" :link "#"}
                    :controls app-bar-controls}]
          (om/build app-bar list-cursor {:opts opts}))
        (dom/table #js {:className "table table-hover" :id "page-list"}
          (dom/thead nil
            (om/build header-row list-cursor))
          (apply dom/tbody nil
            (om/build-all row (sort-pages list-cursor))))))))

