(ns demo.main
  (:require [rum.core :as r]
            [demo.utils :as u]))

(def start-time (atom nil))
(def last-measure (atom nil))

(defn start-measure [name]
  (reset! start-time (.now js/performance))
  (reset! last-measure name))

(defn stop-measure []
  (if-let [last @last-measure]
    (.setTimeout js/window
                 (fn []
                   (reset! last-measure nil)
                   (let [stop (.now js/performance)]
                     (.log js/console (str last " took " (- stop @start-time)))))
                 0)))

(r/defc row < r/static
  [props]
  (let [data (:data props)]
    [:tr
     {:class (:style-class props)}
     [:td.col-md-1 (:id data)]
     [:td.col-md-4
      [:a {:on-click (fn [e]
                       ((:on-click props) (:id data)))}
       (:label data)]]
     [:td.col-md-1
      [:a {:on-click (fn [e]
                       ((:on-delete props) (:id data)))}
       [:span.glyphicon.glyphicon-remove
        {:aria-hidden "true"}]]]
     [:td.col-md-6]]))

(def id-atom (atom 0))
(def data (atom []))
(def selected (atom nil))


(defn run [_]
  (start-measure "run")
  (reset! data (u/build-data id-atom 1000))
  (reset! selected nil))

(defn run-lots [_]
  (start-measure "runLots")
  (reset! data (u/build-data id-atom 10000))
  (reset! selected nil))

(defn add [_]
  (start-measure "add")
  (swap! data u/add id-atom))

(defn update-some []
  (start-measure "update")
  (swap! data u/update-some))

(defn clear []
  (start-measure "clear")
  (reset! selected nil)
  (reset! data []))

(defn swap-rows []
  (start-measure "swapRows")
  (swap! data u/swap-rows))

(defn select [id]
  (start-measure "select")
  (reset! selected id))

(defn delete [id]
  (start-measure "delete")
  (swap! data u/delete-row id))

(r/defc main <
  r/static
  r/reactive
  {:did-mount (fn [state]
                (stop-measure)
                state)
   :did-update (fn [state]
                 (stop-measure)
                 state)}
  []
  [:div.container
   [:div.jumbotron
    [:div.row
     [:div.col-md-6
      [:h1 "Rum"]]
     [:div.col-md-6
      [:div.row
       [:div.col-sm-6.smallpad
        [:button.btn.btn-primary.btn-block
         {:type "button"
          :id "run"
          :on-click run}
         "Create 1,000 rows"]]
       [:div.col-sm-6.smallpad
        [:button.btn.btn-primary.btn-block
         {:type "button"
          :id "runlots"
          :on-click run-lots}
         "Create 10,000 rows"]]
       [:div.col-sm-6.smallpad
        [:button.btn.btn-primary.btn-block
         {:type "button"
          :id "add"
          :on-click add}
         "Append 1,000 rows"]]
       [:div.col-sm-6.smallpad
        [:button.btn.btn-primary.btn-block
         {:type "button"
          :id "update"
          :on-click update-some}
         "Update every 10th row"]]
       [:div.col-sm-6.smallpad
        [:button.btn.btn-primary.btn-block
         {:type "button"
          :id "clear"
          :on-click clear}
         "Clear"]]
       [:div.col-sm-6.smallpad
        [:button.btn.btn-primary.btn-block
         {:type "button"
          :id "swaprows"
          :on-click swap-rows}
         "Swap rows"]]]]]]
   [:table.table.table-hover.table-striped.test-data
    [:tbody
     (let [s (r/react selected)]
       (for [d (r/react data)]
         (r/with-key
           (row
             {:data d
              :on-click select
              :on-delete delete
              :style-class (if (= (:id d) s) "danger")})
           (:id d))))]]
   [:span.preloadicon.glyphicon.glyphicon-remove
    {:aria-hidden "true"}]])

(r/mount (main) (.getElementById js/document "main"))
