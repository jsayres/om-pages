(ns om-pages.components.templates
  (:require [om.dom :as dom :include-macros true]))


(defn main [page]
  (let [{:keys [url content]} page]
    (dom/div nil
      (dom/style nil
        "
        #header {
          background-color: #adf;
        }
        #main {
          min-height: 500px;
        }
        ")
      (dom/div #js {:id "header"}
        (dom/div #js {:className "container"}
          (dom/div #js {:className "row"}
            (dom/div #js {:className "col-md-12"}
              (dom/h1 nil "Title")))))
      (dom/div #js {:id "main" :className "container"}
        (dom/div #js {:className "row"}
          (dom/div #js {:className "col-md-12"}
            (dom/div #js {:id "content"
                          :dangerouslySetInnerHTML #js {:__html content}} nil))))
      (dom/div #js {:id "footer" :className "container"}
        (dom/div #js {:className "row"}
          (dom/div #js {:className "col-md-12"} nil))))))

(defn sub [page]
  (let [{:keys [url content]} page]
    (dom/div nil
      (dom/style nil
        "
        #header {
          background-color: #afd;
        }
        #main {
          min-height: 500px;
        }
        ")
      (dom/div #js {:id "header"}
        (dom/div #js {:className "container"}
          (dom/div #js {:className "row"}
            (dom/div #js {:className "col-md-12"}
              (dom/h1 nil "Title")))))
      (dom/div #js {:id "main" :className "container"}
        (dom/div #js {:className "row"}
          (dom/div #js {:className "col-md-12"}
            (dom/div #js {:id "content"
                          :dangerouslySetInnerHTML #js {:__html content}} nil))))
      (dom/div #js {:id "footer" :className "container"}
        (dom/div #js {:className "row"}
          (dom/div #js {:className "col-md-12"} nil))))))


(def templates {"main" main "sub" sub "default" main})
