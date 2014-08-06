(ns om-pages.core
  (:require [clojure.set :refer [select]]
            [ring.util.response :refer [file-response redirect]]
            [ring.adapter.jetty :refer [run-jetty]]
            [compojure.core :refer [defroutes GET PUT]]
            [compojure.route :as route]
            [om-pages.util :refer [json-response]]))


(def page-db
  (atom
    {:pages
     {1 {:id 1 :branch-id 1 :template "main" :url "/intros/hi"
         :content "<div>Hi there</div>" :author "jdoe"
         :date  "Jan 3, 2014 12:04 pm"}
      2 {:id 2 :branch-id 1 :template "main" :url "/intros/hi"
         :content "<h1>A Title</h1><div>Hi there</div>" :author "jdoe"
         :date "Feb 5, 2014 3:06 pm"}
      3 {:id 3 :branch-id 2 :template "main" :url "/intros/bye"
         :content "<div>See you later</div>" :author "jdoe"
         :date  "Mar 3, 2014 11:34 pm"}
      4 {:id 4 :branch-id 2 :template "main" :url "/intros/bye"
         :content "<h1>Bye!</h1><div>See you later</div>" :author "jdoe"
         :date "Apr 5, 2014 8:20 am"}
      5 {:id 5 :branch-id 3 :template "sub" :url "/contact"
         :content "<div>See you later</div>" :author "anobody"
         :date  "May 3, 2013 11:34 pm"}
      6 {:id 6 :branch-id 4 :template "sub" :url "/about"
         :content "<h1>About</h1><div>No much yet.</div>" :author "someone"
         :date "Dec 25, 2012 8:20 am"}}
     :current #{2 4 5 6}
     :published #{2 3 5}}))


;;;
; Index page
;;;
(defn index []
  (file-response "index.html" {:root "resources/public"}))


;;;
; Get current page info
;;;
(defn published? [page db]
  (boolean ((:published db) (:id page))))

(defn prev-published? [page db]
  (let [pub-ids (:published db)
        pub-br-ids (set (map #(get-in (:pages db) [% :branch-id]) pub-ids))]
    (boolean (and (not (pub-ids (:id page))) (pub-br-ids (:branch-id page))))))

(defn current-pages [db]
  (->> (:pages db)
       (filter (fn [[id _]] ((:current db) id)))
       (map (fn [[_ data]] data))
       (map #(assoc % :published (published? % db)))
       (map #(assoc % :prev-published (prev-published? % db)))))

(defn get-pages-list []
  (json-response {:pages (current-pages @page-db)}))


;;;
; Get specified page info
;;;
(defn get-page [id]
  (let [{:keys [pages published]} @page-db
        page (get pages id)]
    (json-response
      (if page
        (let [branch-id (:branch-id page)
              versions (filterv #(= (:branch-id %) branch-id) (vals pages))]
          {:branch-id branch-id :published (some published (map :id versions)) :versions versions})
        {:error "No such page was found."}))))


;;;
; Publish specified page
;;;
(defn update-published [{:keys [pages published] :as db} page]
  (let [{:keys [id branch-id]} page]
    (assoc-in db [:published]
              (-> (select #(not= branch-id (get-in pages [% :branch-id])) published)
                  (conj id)))))

(defn publish-page! [id]
  (let [{:keys [pages published]} @page-db
        {:keys [branch-id] :as page} (get pages id)]
    (json-response
      (cond
        (published id) {:published true}
        page (do (swap! page-db update-published page) {:published true}) 
        :else {:error "No such page was found."}))))


;;;
; Publish specified page
;;;
(defn unpublish-page! [id]
  (swap! page-db #(update-in % [:published] disj id))
  (json-response {:published false}))

;;;
; Routes
;;;
(defroutes routes
  (GET "/" [] (redirect "/pages"))
  (GET "/pages" [] (index))
  (GET "/api/pages" [] (get-pages-list))
  (GET ["/api/pages/:id" :id #"[0-9]+"] [id] (get-page (read-string id)))
  (PUT ["/api/pages/:id/publish" :id #"[0-9]+"] [id] (publish-page! (read-string id)))
  (PUT ["/api/pages/:id/unpublish" :id #"[0-9]+"] [id] (unpublish-page! (read-string id)))
  (route/files "/" {:root "resources/public"}))


(defn -main []
  (run-jetty routes {:port 3000}))
