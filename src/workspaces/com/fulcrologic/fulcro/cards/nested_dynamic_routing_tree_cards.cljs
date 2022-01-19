(ns com.fulcrologic.fulcro.cards.nested-dynamic-routing-tree-cards
  (:require
    [nubank.workspaces.card-types.fulcro3 :as ct.fulcro]
    [nubank.workspaces.core :as ws]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.wsscode.pathom.connect :as pc]
    [com.wsscode.pathom.core :as p]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr :refer [defrouter]]
    [com.fulcrologic.fulcro.dom :as dom]
    [com.fulcrologic.fulcro.networking.mock-server-remote :refer [mock-http-server]]
    [com.fulcrologic.fulcro.mutations :as m]
    [taoensso.timbre :as log]
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]
    [com.fulcrologic.fulcro.algorithms.tx-processing :as txn]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [edn-query-language.core :as eql]
    [com.fulcrologic.fulcro.application :as app]))

(defsc A1 [this {:keys [:id] :as props}]
  {:query               [:id]
   :route-segment       ["a1"]
   :will-enter          (fn [app params]
                          (log/info "A1 will enter")
                          (dr/route-deferred [:id "a1"]
                            (fn [] (js/setTimeout #(dr/target-ready! app [:id "a1"]) 300))))
   :will-leave          (fn [cls props] (log/info "A1 will leave"))
   :allow-route-change? (fn [c] (log/info "A1 allow route change?") true)
   :initial-state       {:id "a1"}
   :ident               :id}
  (let [parent comp/*parent*]
    (dom/div "A1"
      (dom/button
        {:onClick #(dr/change-route-relative! this this [:.. "a2"])}
        "Go to sibling A2"))))

(defsc A2 [this {:keys [:id] :as props}]
  {:query               [:id]
   :route-segment       ["a2"]
   :will-enter          (fn [app params]
                          (log/info "A2 will enter")
                          (dr/route-deferred [:id "a2"]
                            (fn [] (js/setTimeout #(dr/target-ready! app [:id "a2"]) 300))))
   :will-leave          (fn [cls props] (log/info "A2 will leave"))
   :allow-route-change? (fn [c] (log/info "A2 allow route change?") true)
   :initial-state       {:id "a2"}
   :ident               :id}
  (dom/div "A2"))

(defsc B1 [this {:keys [:id] :as props}]
  {:query               [:id]
   :route-segment       ["b1"]
   :will-enter          (fn [app params]
                          (log/info "B1 will enter")
                          (dr/route-deferred [:id "b1"]
                            (fn [] (js/setTimeout #(dr/target-ready! app [:id "b1"]) 300))))
   :will-leave          (fn [cls props] (log/info "B1 will leave"))
   :allow-route-change? (fn [c] (log/info "B1 allow route change?") true)
   :initial-state       {:id "b1"}
   :ident               :id}
  (dom/div "B1"))

(defsc B2 [this {:keys [:id] :as props}]
  {:query               [:id]
   :route-segment       ["b2"]
   :will-enter          (fn [app params]
                          (log/info "B2 will enter")
                          (dr/route-deferred [:id "b2"]
                            (fn [] (js/setTimeout #(dr/target-ready! app [:id "b2"]) 300))))
   :will-leave          (fn [cls props] (log/info "B2 will leave"))
   :allow-route-change? (fn [c] (log/info "B2 allow route change?") true)
   :initial-state       {:id "b2"}
   :ident               :id}
  (dom/div "B2"))

(defrouter ARouter [this props]
  {:router-targets [A1 A2]})
(def ui-a-router (comp/factory ARouter))

(defrouter BRouter [this props]
  {:router-targets [B1 B2]})
(def ui-b-router (comp/factory BRouter))

(defsc B [this {:keys [id router] :as props}]
  {:query               [:id {:router (comp/get-query BRouter)}]
   :ident               :id
   :route-segment       ["b"]
   :will-enter          (fn [app params]
                          (log/info "B will enter")
                          (dr/route-immediate [:id "b"]))
   :will-leave          (fn [cls props] (log/info "B will leave"))
   :allow-route-change? (fn [c] (log/info "B allow route change?") true)
   :initial-state       {:id "b" :router {}}}
  (dom/div {}
    (dom/h2 "B")
    (dom/button {:onClick (fn [] (dr/change-route-relative! this B ["b1"]))} "B1 (relative)")
    (dom/button {:onClick (fn [] (dr/change-route-relative! this B ["b2"]))} "B2 (relative)")
    (ui-b-router router)))

(def ui-b (comp/factory B {:keyfn :id}))

(defsc A [this {:keys [id router] :as props}]
  {:query               [:id {:router (comp/get-query ARouter)}]
   :route-segment       ["a"]
   :will-enter          (fn [app params]
                          (log/info "A will enter")
                          (dr/route-immediate [:id "a"]))
   :will-leave          (fn [cls props] (log/info "A will leave"))
   :allow-route-change? (fn [c] (log/info "A allow route change?") true)
   :initial-state       {:id "a" :router {}}
   :ident               :id}
  (dom/div {}
    (dom/h2 "A")
    (dom/button {:onClick (fn [] (dr/change-route-relative! this A ["a1"]))} "A1 (relative)")
    (dom/button {:onClick (fn [] (dr/change-route-relative! this A ["a2"]))} "A2 (relative)")
    (ui-a-router router)))

(def ui-a (comp/factory A {:keyfn :id}))


(defsc Pic [this {:data/keys [id label pic txt] :as props}]
  {:query               [:data/id :data/label :data/pic :data/txt]
   :route-segment       ["pic" :data/id]
   :will-enter          (fn [app params]
                          (log/info "Pic will enter, params:" params)
                          (dr/route-immediate [:data/id (js/parseInt (:data/id params))]))
   :initial-state       {}
   :ident               :data/id}
  (dom/div "picture:" label pic))

(defsc Txt [this {:data/keys [id label pic txt] :as props}]
  {:query               [:data/id :data/label :data/pic :data/txt]
   :route-segment       ["txt" :data/id]
   :will-enter          (fn [app params]
                          (log/info "Txt will enter, params:" params)
                          (dr/route-immediate [:data/id (js/parseInt (:data/id params))]))
   :initial-state       {}
   :ident               :data/id}
  (dom/div "txt" label txt))

(defrouter TabRouter [this props]
  {:router-targets [Pic Txt]})

(def ui-tab-router (comp/factory TabRouter))

(defsc BigDetail [this {:data/keys [id label pic txt] :as props router :ui/router}]
  {:query               [:data/id :data/label :data/pic :data/txt {:ui/router (comp/get-query TabRouter)}]
   :route-segment       ["d" :data/id] 
   :will-enter          (fn [app params]
                          (log/info "BigDetail will enter, params:" params)
                          (dr/route-immediate [:data/id (js/parseInt (:data/id params))]))
   :initial-state       {:ui/router {}}
   :ident               :data/id}
  (dom/div {:style {:border "1px dotted green"}}
           (dom/h2 label)
           (dom/div "detail:" id " " label)

           (let [current-tab (some-> (dr/current-route this this) first keyword)]
             (dom/div :.ui.container
                      (dom/div :.ui.secondary.pointing.menu
                               (dom/a :.item {:classes [(when (= :pic current-tab) "active")]
                                              :onClick (fn [] (dr/change-route this ["pic" id]))} "Pic")
                               (dom/a :.item {:classes [(when (= :txt current-tab) "active")]
                                              :onClick (fn [] (dr/change-route this ["txt" id]))} "Txt"))))
           (ui-tab-router router)))


(defsc Data [this {:data/keys [id label pic txt] :as props}]
  {:query         [:data/id :data/label :data/pic :data/txt]
   :ident         :data/id}
  (dom/li (dom/a  {:onClick (fn [] (dr/change-route! this ["d" id "pic" id]))} label)))

(def ui-data (comp/factory Data {:keyfn :data/id}))

(defsc Datalist [this {:list/keys [data] :as props}]
  {:initial-state {:list/data []}
   :query         [:list/id {:list/data (comp/get-query Data)}]
   :will-enter          (fn [app params]
                          (log/info "Datalist will enter")
                          (dr/route-immediate [:list/id :data]))
   :route-segment ["c"]
   :ident        :list/id}
  (dom/div
   (dom/hr)
   (map ui-data data)
   (dom/hr)))

(defrouter RootRouter [this props]
  {:router-targets [A B Datalist BigDetail]})
(def ui-router (comp/factory RootRouter))


(def ui-datalist (comp/factory Datalist))

(defonce SPA (atom nil))

(defsc Root [this {:root/keys [:router] :as props}]
  {:query         [{:root/router (comp/get-query RootRouter)}]
   :initial-state (fn [_] {:root/router (comp/get-initial-state RootRouter {})
                          :root/datalist (comp/get-initial-state Datalist {:id :data})})}
  
    (let [current-tab (some-> (dr/current-route this this) first keyword)]
      (dom/div :.ui.container {:style {:border "1px dotted red"}}
               (dom/div :.ui.secondary.pointing.menu
                        (dom/a :.item {:classes [(when (= :a current-tab) "active")]
                                       :onClick (fn [] (dr/change-route this ["a"]))} "A")
                        (dom/a :.item {:classes [(when (= :b current-tab) "active")]
                                       :onClick (fn [] (dr/change-route this ["b"]))} "B")
                        (dom/a :.item {:classes [(when (= :c current-tab) "active")]
                                       :onClick (fn [] (dr/change-route this ["c"]))} "Datalist"))

               #_(ui-datalist datalist)
               (dom/hr)
               (ui-router router)
               (dom/hr)
               (dom/div
                (dom/h2 "Global navigation")
                (dom/button {:onClick (fn [] (dr/change-route-relative! this Root ["a" "a1"]))} "A1 ")
                (dom/button {:onClick (fn [] (dr/change-route-relative! this Root ["b" "b2"]))} "B2 ")
                (dom/button {:onClick (fn [] (dr/change-route-relative! this Root ["a"]))} "A")
                (dom/button {:onClick (fn [] (dr/change-route-relative! this Root ["b"]))} "B")
                (dom/button {:onClick (fn [] (dr/change-route-relative! this Root ["c"]))} "C")
                (dom/button {:onClick (fn [] (dr/change-route-relative! this Root ["d" "2"]))} "D")
                (dom/button {:onClick (fn [] (dr/change-route-relative! this Root ["d" "2" "pic" "2"]))} "D .pic")
                (dom/button {:onClick (fn [] (dr/change-route-relative! this Root ["d" "2" "txt" "2"]))} "D .txt")
                (dom/hr)
               (clojure.string/join "/" (dr/current-route SPA))
                ))))

(ws/defcard nested-routing-demo
  (ct.fulcro/fulcro-card
    {::ct.fulcro/wrap-root? false
     ::ct.fulcro/root       Root
     ::ct.fulcro/app
     {:client-will-mount
      (fn [app]
        (reset! SPA app)
        (dr/initialize! app)
        (merge/merge-component! app Data [{:data/id 1 :data/label "train" :data/pic "<train/>" :data/txt "train text"}
                                          {:data/id 2 :data/label "boat" :data/pic "<boat/>" :data/txt "boat text"}]
                                :replace [:list/id :data :list/data])
        (dr/change-route! app ["a" "a1"]))}}))
