(ns id-resolver.prod
  (:require [id-resolver.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
