(ns clj-wamp.websocket
  (:require [clojure.string :as string :refer [trim blank?]]
            [goog.events :as events]
            [goog.net.WebSocket :as websocket]
            [goog.net.WebSocket.EventType :as websocket-event]
            [goog.net.WebSocket.MessageEvent :as websocket-message]))

(defn client
  "CLJS WebSocket client wrapper.

  Example usage:
    (let [ws (websocket/client \"ws://host:port/path\"
            {:protocol \"optional\"
             :on-open (fn [ws]
                        (println \"connected\"))
             :on-close (fn [ws code reason]
                        (println \"closed\" code reason))
             :on-message (fn [ws data]
                           (println \"got data:\" data))})]
       (websocket/send! ws \"my message\")
       (websocket/close! ws 1006 \"normal exit\"))"
  [uri & [{:keys [protocol on-open on-close on-message on-error
                  reconnect? reconnect-ms]
           :or {reconnect? true}}]]
  (let [ws (goog.net.WebSocket. reconnect? reconnect-ms)
        handler (events/EventHandler.)]
    ; Set up callback listeners
    (when on-open
      (.listen handler ws websocket-event/OPENED #(on-open ws)))
    (when on-message
      (.listen handler ws websocket-event/MESSAGE
        #(let [payload (trim (.-message %))]
           (.log js/console "RCV" payload)
           (when (not (blank? payload))
             (on-message ws payload)))))
    (when on-error
      (.listen handler ws websocket-event/ERROR on-error))
    (when on-close
      (.listen handler ws websocket-event/CLOSED #(on-close))) ; TODO get code & reason
    ; Connect to websocket server
    (.open ws uri protocol)
    ws))

(defn close!
  "Closes WebSocket"
  [ws]
  (.close ws))

(defn send!
  "Sends a message to server"
  [ws msg]
  (.log js/console "SND" msg)
  (.send ws msg))