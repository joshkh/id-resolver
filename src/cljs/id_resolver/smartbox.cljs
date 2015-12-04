(ns id-resolver.smartbox
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]
            [intermine.imjs :as imjs]
            [cljs.pprint :refer [pprint]]
            [cljs.core.async :refer [put! chan <!]])
  (:use [jayq.core :only [$ css html attr val]]))

;; -------------------------
;; Views

(def flymine (js/imjs.Service. (clj->js {:root "www.flymine.org/query"})))

(def parsed (atom []))



(defn strip [coll chars]
  (apply str (remove #((set chars) %) coll)))

(defn update-match [s searchterm value]
  (vec (map (fn [item] (if (= searchterm (:input item))
                             (assoc item :status value)
                             item)) s)) )

;(defn resolve-id [id]
;  (let [id-job-promise (.resolveIds flymine (clj->js {:identifiers [id]
;                                                      :type "Gene"
;                                                      :extra "D. melanogaster"}))]
;    (-> id-job-promise
;        (.then (fn [id-job] (.fetchResults id-job)) (fn [r] (println "FAILED FIRST")))
;        (.then (fn [resolution]
;                 (println "HELLO")
;                 (let [match (first (.. resolution -matches -MATCH))
;                       input (first (.-input match))]
;                   (swap! parsed #(update-match % input))))
;               (fn [r] (println "FAILED SECOND" r))
;               ))))
(defn jsx->clj
  [x]
  (into {} (for [k (.keys js/Object x)] [k (aget x k)])))

(defn resolve-id [id]

  (let [id-job-promise (.resolveIds flymine (clj->js {:identifiers [id]
                                                      :type "Gene"
                                                      :extra "D. melanogaster"}))]
    (-> id-job-promise
        (.then (fn [id-job]
                 (.poll id-job
                        (fn [success]
                          (let [[matches duplicate other]
                                (map #(aget (aget success "matches") %)
                                     ["MATCH" "DUPLICATE" "OTHER"])
                                unresolved (.. success -unresolved)]
                            (.log js/console matches)
                            ;(map (fn [x]
                            ;       (let [input ])
                            ;       (println)
                            ;       (swap! parsed #(update-match % "test" "matched"))) matches)
                            ;(if (not (empty? matches))
                            ;  )
                            ))))

               (fn [r] (println "FAILED FIRST"))))))




;(swap! parsed #(update-match % "test" "matched"))
;(let [[A B C] (map #(str %) ["ONE" "TWO" "THREE"])]
;  (println A))

(defn think [e a force]
  (let [value (.. e -target -value)
        $input ($ :#inputf)
        length (count value)]
    (-> $input (attr {:size length}))
    ;(println "force value is" force)
    (if (or (> (.indexOf value " ") -1)
            (> (.indexOf value ",") -1)
            (> (.indexOf value ";") -1)
            (true? force))
      (let [found  (strip (.trim value) " ,;")]
        (if (not (empty? found))
          (do
            (swap! parsed conj {:input found :status "pending"})
            (resolve-id found)))
        (reset! a nil))
      (reset! a value))))

(defn setfocus [e]
  (-> js/document (.querySelector "#inputf") (.focus)))


(defn atom-input [value]
  [:input {:type "text"
           :id "inputf"
           :size 0
           :value @value
           :on-change #(think % value false)
           :on-key-press (fn [e]
                           ;(println "key press" (.-charCode e))
                           (if (= 13 (.-charCode e))
                             #(think % value true)))}])

(defn input-container []
  (let [val (atom "")]
    [:div.float.input-container [atom-input val]]))

(defn box []
  [:div.smartbox {:on-click setfocus}
   (map (fn [x] [:div.float.item {:class (:status x)} [:i.fa.fa-question.padded] (:input x)]) @parsed)
   [:div.float [input-container]]])







;(resolve-id "eve")
