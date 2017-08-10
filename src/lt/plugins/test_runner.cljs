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

(def wait-css "background-color: #c7e6ff; color: deepskyblue; ")
(def pass-css "background-color: #378b2e; color: #92d18b; ")
(def fail-css "background-color: #aa3939; color: #ffb9b9; ")

(def wait-style { :background-color "#c7e6ff" :color "deepskyblue" })
(def pass-style { :background-color "#378b2e" :color "#92d18b" })
(def fail-style { :background-color "#aa3939" :color "#ffb9b9" })

(defui test-item [item]
  (let [{:keys [id label open? result]} item]
    [:li {:id (str "test-" id)}
      [:span "a"]
      (case result
        :wait [:span { :class "test-result" :style wait-css } "Wait"]
        :pass [:span { :class "test-result" :style pass-css } "Pass"]
        :fail [:span { :class "test-result" :style fail-css } "Fail"]
        [:span { :class "test-result" :style wait-css } "Wait"])
      label
      [:ul { :style "display: none; margin-left: 18px"}]]))

(let [{:keys [foo bar baz]} {:foo 1 :bar "a"}]
  baz)

(def item1 { :id 10001 :label "Test Item 1" :result :pass })
(def item2 { :id 10002 :label "Test Item 2" :result :fail })
(def item3 { :id 10003 :label "Test Item 3" :result :wait })

(def item11 { :id 10101 :label "SubTest Item 1" })
(def item12 { :id 10102 :label "SubTest Item 2" })
(def item31 { :id 10301 :label "SubTest Item 3" })
(def item32 { :id 10302 :label "SubTest Item 4" })
(def item33 { :id 10303 :label "SubTest Item 5" })

(def item331 { :id 10331 :label "SubSubTest Item 1" })
(def item332 { :id 10332 :label "SubSubTest Item 2" })
(def item333 { :id 10333 :label "SubSubTest Item 3" })
(def item334 { :id 10334 :label "SubSubTest Item 4" })

(defui result-tree [this]
  [:div
    [:ul {:id "root" :style "margin-left: 18px;" }
      [:li {:id "123"} [:span "a"] [:span { :style wait-css } "Wait"] "Item 1"]
      [:li {:id "124"} [:span "a"] [:span { :style pass-css } "Pass"] "Item 2"
        [:ul { :style "margin-left: 18px;" }
          [:li {:id "101"} [:span "a"] "SubItem 1"]
          [:li {:id "102"} [:span "a"] "SubItem 2"]
          [:li {:id "103"} [:span "a"] "SubItem 3"
            [:ul { :style "margin-left: 18px;" }
              [:li {:id "207"} [:span "a"] "SubSubItem 1"]
              [:li {:id "208"} [:span "a"] "SubSubItem 2"]]]
          [:li {:id "104"} [:span "a"] "SubItem 4"]]]
      [:li {:id "125"} [:span "a"] [:span { :style fail-css } "Fail"] "Item 3"]]])

(object/object* ::test_runner.midje-tester
                :tags [:test_runner.midje-tester]
                :behaviors [::on-close-destroy]
                :name "Midje Tester"
                :init (fn [this proj-name]
                        (midje-tester-panel this proj-name)))

(dom/append (dom/$ "#root" (object/->content midje-tester))
            (test-item item1))

(str "#" 123)

(defn update-tree-result! [doc id result]
  (let [elem (dom/$ (str "#test-" id " > .test-result") doc)]
    (dom/html elem (case result
                     :pass "Pass"
                     :fail "Fail"
                     :wait "Wait"
                     "Wait"))
    (dom/set-css elem
                 (case result
                   :pass pass-style
                   :fail fail-style
                   :wait wait-style
                   wait-style))))

(update-tree-result! (object/->content midje-tester)
                     10001 :fail)

(dom/html (dom/$ (str "#test-" 10001 " > .test-result")
       (object/->content midje-tester)) "Bye")

(dom/$ "#hello" (object/->content midje-tester))

(if-let [x 2]
  x
  "bye")

(defn update-tree-item! [doc item-context]
  (reduce (fn [parent-elem item]
            (if-let [elem (dom/$ (str "#test-" (:id item) " > ul") doc)]
              elem
              (do
                (dom/set-css parent-elem {:display "block"})
                (dom/append parent-elem (test-item item))
                (dom/$ (str "#test-" (:id item) " > ul") doc))))
          (dom/$ "#root" doc)
          item-context))

(update-tree-item! (object/->content midje-tester)
                   [item1 item11])

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
