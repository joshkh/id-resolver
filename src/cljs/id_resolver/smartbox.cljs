(ns id-resolver.smartbox
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant])
  (:use [jayq.core :only [$ css html attr val]]))

;; -------------------------
;; Views

(def parsed (atom []))


(defn strip [coll chars]
  (apply str (remove #((set chars) %) coll)))

(defn think [e a force]
  (let [value (.-target.value e)
        $input ($ :#inputf)
        length (count value)]
    (-> $input (attr {:size length}))
    (println "force value is" force)
    (if (or (> (.indexOf value " ") -1)
            (> (.indexOf value ",") -1)
            (> (.indexOf value ";") -1)
            (true? force))
      (let [found  (strip (.trim value) " ,;")]
        (if (not (empty? found))
          (swap! parsed conj found))
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
                           (println "key press" (.-charCode e))
                           (if (= 13 (.-charCode e))
                             #(think % value true)))}])

(defn input-container []
  (let [val (atom "")]
    [:div.float.input-container [atom-input val]]))

(defn box []
  [:div.smartbox {:on-click setfocus}
   (map (fn [x] [:div.float.solid x]) @parsed)
   [:div.float [input-container]]])