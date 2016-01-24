(ns onyx.plugin.http-output
  (:require [clojure.core.async :as async :refer [<!! <! go]]
            [onyx.extensions :as extensions]
            [onyx.log.commands.peer-replica-view :refer [peer-site]]
            [onyx.peer.function :as function]
            [onyx.peer.operation :refer [kw->fn]]
            [onyx.peer.pipeline-extensions :as p-ext]
            [onyx.static.default-vals :refer [defaults arg-or-default]]
            [onyx.types :as t :refer [dec-count! inc-count!]]
            [qbits.jet.client.http :as http]
            [taoensso.timbre :refer [debug info error] :as timbre]))

(defn- process-message [client success? message ack-fn]
  (go
    (try
      (let [rch      (http/post client (:url message) (:args message))
            response (<! rch)]
        (if (:error response)
          (error "Request failed" {:request message :response response})
          (let [body (<! (:body response))]
            (if (and (< (:status response) 400) (success? body))
              (ack-fn)
              (error "Request isn't successful"
                {:request message :response response :body body})))))
      (catch Exception e
        (error "Exception" {:request message :exception e})))
    (ack-fn)))

(defrecord JetWriter [client success?]
  p-ext/Pipeline
  (read-batch [_ event]
    (onyx.peer.function/read-batch event))

  ;; written after onyx-bookkeeper plugin
  (write-batch
    [_ {:keys [onyx.core/results onyx.core/peer-replica-view onyx.core/messenger]
        :as event}]
    (doall
      (map (fn [[result ack]]
             (run! (fn [_] (inc-count! ack)) (:leaves result))
             (let [ack-fn (fn []
                            (when (dec-count! ack)
                              (when-let [site (peer-site peer-replica-view (:completion-id ack))]
                                (extensions/internal-ack-segment messenger event site ack))))]
               (run! (fn [leaf]
                       (process-message client success? (:message leaf) ack-fn))
                 (:leaves result))))
        (map list (:tree results) (:acks results))))
    {:onyx.core/written? true})

  (seal-resource [_ _]
    (.destroy client)
    {}))


;; Builder function for your output plugin.
;; Instantiates a record.
;; It is highly recommended you inject and pre-calculate frequently used data
;; from your task-map here, in order to improve the performance of your plugin
;; Extending the function below is likely good for most use cases.
(defn output [{:keys [onyx.core/task-map] :as pipeline-data}]
  (let [client (http/client)
        success? (kw->fn (:http-output/success-fn task-map))]
   (->JetWriter client success?)))
