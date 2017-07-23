(ns lt.plugins.test_runner
  (:require [lt.object :as object]
            [lt.objs.tabs :as tabs]
            [lt.objs.command :as cmd]
            [lt.objs.sidebar.workspace :as workspace])
  (:require-macros [lt.macros :refer [defui behavior]]))

(defui midje-tester-panel [this]
  [:div
    [:h1 "Hello from test_runner more test"]
    [:h2 (:path this)]])

(object/object* ::test_runner.midje-tester
                :tags [:test_runner.midje-tester]
                :behaviors [::on-close-destroy]
                :name "Midje Tester"
                :init (fn [this]
                        (midje-tester-panel this)))

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

(behavior ::on-open-tester
          :triggers #{:menu-selected}
          :reaction (fn [this path]
                      (object/update! midje-tester [:path] (constantly path))
                      (object/update! midje-tester [:content] (constantly (midje-tester-panel (deref midje-tester))))
                      (prn (deref midje-tester))
                      (tabs/add-or-focus! midje-tester)))

(def midje-tester (object/create ::test_runner.midje-tester))

(cmd/command {:command ::say-hello
              :desc "test_runner: Say Hello world"
              :exec (fn []
                      (prn "Test")
                      (object/update! midje-tester [:path] (constantly (-> @(lt.objs.editor.pool/last-active) :info :path)))
                      (object/update! midje-tester [:content] (constantly (midje-tester-panel (deref midje-tester))))
                      (tabs/add-or-focus! midje-tester))})

workspace/root


(deref workspace/tree)

