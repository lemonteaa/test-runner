(ns lt.plugins.test_runner
  (:require [lt.object :as object]
            [lt.objs.tabs :as tabs]
            [lt.util.dom :as dom]
            [lt.objs.command :as cmd]
            [lt.objs.sidebar.workspace :as workspace]
            [lt.objs.clients :as clients]
            [lt.plugins.clojure.nrepl :as nrepl])
  (:require-macros [lt.macros :refer [defui behavior]]))

(defui midje-tester-panel [this proj-name]
  [:div
    (config-panel this proj-name)
    (result-tree this)])

(defui config-panel [this proj-name]
  [:div
    [:h2 (str "Project: " proj-name)]
    [:div
      "Testing Library: "
      [:select {:name "test-lib"}
        [:option {:value :clojure.test } "clojure.test"]
        [:option {:value :midje } "midje"]]
      [:input {:type "checkbox" :id "auto-test"}]
      [:label {:for "auto-test"} "Auto-test On Save?"]
      [:button "Run!"]]])

(defui result-tree [this]
  [:div
    [:ul {:id "root" :style "margin-left: 18px;" }
      [:li {:id "123"} [:span "a"] "Item 1"]
      [:li {:id "124"} [:span "a"] "Item 2"
        [:ul { :style "margin-left: 18px;" }
          [:li {:id "101"} [:span "a"] "SubItem 1"]
          [:li {:id "102"} [:span "a"] "SubItem 2"]
          [:li {:id "103"} [:span "a"] "SubItem 3"
            [:ul { :style "margin-left: 18px;" }
              [:li {:id "207"} [:span "a"] "SubSubItem 1"]
              [:li {:id "208"} [:span "a"] "SubSubItem 2"]]]
          [:li {:id "104"} [:span "a"] "SubItem 4"]]]
      [:li {:id "125"} [:span "a"] "Item 3"]]])

(object/object* ::test_runner.midje-tester
                :tags [:test_runner.midje-tester]
                :behaviors [::on-close-destroy]
                :name "Midje Tester"
                :init (fn [this proj-name]
                        (midje-tester-panel this proj-name)))

(behavior ::on-close-destroy
          :triggers #{:close}
          :desc "test_runner: Close tab and tabset as well if last tab"
          :reaction (fn [this]
                      (when-let [ts (:lt.objs.tabs/tabset @this)]
                        (when (= (count (:objs @ts)) 1)
                          (tabs/rem-tabset ts)))
                      (object/raise this :destroy)))

(behavior ::on-root-menu
          :triggers #{:menu-items}
          :reaction (fn [this items]
                      (conj items
                            {:type "separator"
                             :order 11}
                            {:label "Test plugin"
                             :order 12
                             :click (fn []
                                      (prn (:path (deref this)))
                                      (object/raise this :menu-selected (:path (deref this))))})
                      ))

(behavior ::my-file-save
          :triggers #{:save}
          :reaction (fn [editor]
                      (prn "saving.....")
                      (prn "Path: " (-> @editor :info (get :path) (or "")))))

(behavior ::on-open-tester
          :triggers #{:menu-selected}
          :reaction (fn [this path]
                      (object/update! midje-tester [:path] (constantly path))
                      (object/update! midje-tester [:content] (constantly (midje-tester-panel (deref midje-tester) "My project")))
                      (prn (deref midje-tester))
                      (tabs/add-or-focus! midje-tester)))

(behavior ::on-recv-response
          :triggers #{:client.ping}
          :reaction (fn [client info]
                      (prn "Res: " info)))

;(behavior ::nrepl-send-custom
;          :triggers #{:send-custom}
;          :reaction (fn [client payload]
;                      ))

(object/add-behavior! (-> @(nth (object/by-tag :editor) 3) :client :default) ::on-recv-response)


(def midje-tester (object/create ::test_runner.midje-tester "My project"))

(cmd/command {:command ::say-hello
              :desc "test_runner: Say Hello world"
              :exec (fn []
                      (prn "Test")
                      (object/update! midje-tester [:path] (constantly (-> @(lt.objs.editor.pool/last-active) :info :path)))
                      (object/update! midje-tester [:content] (constantly (midje-tester-panel (deref midje-tester) "My project")))
                      (tabs/add-or-focus! midje-tester))})

workspace/root


(deref workspace/tree)

(-> @(lt.objs.editor.pool/last-active) :reaction)

(object/by-tag :evaler)

(map #(-> @% :info (get :path) (or "")) (object/by-tag :editor))


(object/->id (-> @(nth (object/by-tag :editor) 3) :client :default))

(:behaviors @(nth (object/by-tag :editor) 0))

(object/->id (nth (object/by-tag :editor) 3))

;;;;

(clients/->message "op-test" { :foo 1 })

(defn t [info]
  (prn info))

@(keys clients/callbacks)

clients/cb-id

(nrepl/send (-> @(nth (object/by-tag :editor) 3) :client :default)
            { :id 507 :op "client.ping" :data (pr-str { :foo 1 :times 4 }) :rtn-fmt "lt" })

@clients/callbacks

(clients/send (-> @(nth (object/by-tag :editor) 3) :client :default)
              "ping"
              { :foo 1 :times 3 })

(keyword "client.ping")
