(ns lt.plugins.test_runner
  (:require [lt.object :as object]
            [lt.objs.tabs :as tabs]
            [lt.objs.command :as cmd])
  (:require-macros [lt.macros :refer [defui behavior]]))

(defui hello-panel [this]
  [:h1 "Hello from test_runner"])

(object/object* ::test_runner.hello
                :tags [:test_runner.hello]
                :behaviors [::on-close-destroy]
                :name "test_runner"
                :init (fn [this]
                        (hello-panel this)))

(behavior ::on-close-destroy
          :triggers #{:close}
          :desc "test_runner: Close tab and tabset as well if last tab"
          :reaction (fn [this]
                      (when-let [ts (:lt.objs.tabs/tabset @this)]
                        (when (= (count (:objs @ts)) 1)
                          (tabs/rem-tabset ts)))
                      (object/raise this :destroy)))

(def hello (object/create ::test_runner.hello))

(cmd/command {:command ::say-hello
              :desc "test_runner: Say Hello"
              :exec (fn []
                      (tabs/add-or-focus! hello))})
