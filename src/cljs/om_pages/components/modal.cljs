(ns om-pages.components.modal
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.dom :as gdom]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [chan timeout put! <!]]))


(def modal-opts-chan (chan))
(def modal-close-chan (chan))
(def fade-time 160)

(defn set-modal [modal-opts]
  (put! modal-opts-chan modal-opts))

(defn close-modal []
  (put! modal-close-chan true))

(defn option->button [{:keys [text style action]}]
  (let [className (str "btn btn-" (or style "default"))]
    (dom/button #js {:type "button" :className className :onClick action} text)))

;;;
; Modal component
; ---------------
; There are 4 states to the modal - :init, :in, :out, :clear
;;;
(defn modal [cursor owner]
  (reify
    om/IInitState
    (init-state [_]
      {:modal-opts nil})

    om/IWillMount
    (will-mount [_]
      (go (loop []
        (om/set-state! owner :modal {:opts (<! modal-opts-chan) :state :init})
        (<! modal-close-chan)
        (om/set-state! owner [:modal :state]  :out)
        (<! (timeout fade-time))
        (recur))))

    om/IDidUpdate
    (did-update [_ _ _]
      (let [state (om/get-state owner [:modal :state])]
        (cond
          (= state :init) (js/setTimeout
                            #(om/set-state! owner [:modal :state] :in))
          (= state :out) (go
                           (<! (timeout fade-time))
                           (om/set-state! owner [:modal] {:state :clear})))))

    om/IRenderState
    (render-state [_ {{:keys [opts state]} :modal}]
      (when opts
        (let [{:keys [title content options]} opts
              in (= state :in)
              click-on-bg? #(= (.-target %) (.-currentTarget %))]
          (dom/div nil
            (dom/div #js {:className (str "modal fade" (when in " in"))
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
            (dom/div #js {:className (str "modal-backdrop fade" (when in " in"))})))))))
