(ns om-pages.components.modal
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.dom :as gdom]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [chan put! <!]]))


(def modal-chan (chan))

(defn set-modal [modal-opts]
  (put! modal-chan modal-opts))

(defn close-modal []
  (put! modal-chan {:close true}))

(defn option->button [{:keys [text style action]}]
  (let [className (str "btn btn-" (or style "default"))]
    (dom/button #js {:type "button" :className className :onClick action} text)))

(defn modal [cursor owner]
  (reify
    om/IInitState
    (init-state [_]
      {:modal-opts nil})
    om/IWillMount
    (will-mount [_]
      (go
        (loop []
          (let [{:keys [close] :as modal-opts} (<! modal-chan)]
            (om/set-state! owner :modal-opts (when-not close modal-opts))
            (recur)))))
    om/IRenderState
    (render-state [_ {:keys [modal-opts]}]
      (when modal-opts
        (let [{:keys [title content options]} modal-opts
              click-on-bg? #(= (.-target %) (.-currentTarget %))]
          (dom/div nil
            (dom/div #js {:className "modal fade in"
                          :onClick #(when (click-on-bg? %) (close-modal))}
              (dom/div #js {:className "modal-dialog"}
                (dom/div #js {:className "modal-content"}
                  (dom/div #js {:className "modal-header"}
                    (dom/button #js {:type "button" :className "close"
                                     :onClick close-modal}
                      (dom/i #js {:className "fa fa-times"}))
                    (dom/h4 #js {:className "modal-title"} title))
                  (dom/div #js {:className "modal-body"} content)
                  (apply dom/div #js {:className "modal-footer"}
                    (map option->button options)))))
            (dom/div #js {:className "modal-backdrop fade in"})))))))
