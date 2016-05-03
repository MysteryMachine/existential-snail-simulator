(ns game.hooks
  (:use folha.core)
  (:require [clojure.edn :as edn]
            [game.player :as player]))

(defn sync-game-over-screen [this state]
  (when (:game-over state)
    (active! (:game-over-text state) true)))

(def nihilistic-text
  ["Life is hell"
   "You have skeletons inside you"])

(defn sync-flops [this state]
  (case (:flop-state state)
    :new-flop (let [txt (:existential-text state)]
                (active! txt true)
                (text! txt (rand-nth nihilistic-text)))
    :stop-flop (active! (:existential-text state) false)
    nil))

(defhook on-start [this state]
  (sync! this (the "Game Over UI") sync-game-over-screen)
  (sync! this (the "Existential UI") sync-flops)
  (let [go-txt (the "Game Over Text")
        e-txt (the "Existential Text")
        state
        {:ctrls
         {:right false :left :false
          :up false    :down false}
         :flop 0
         :game-over-text go-txt
         :existential-text e-txt
         :heading nil
         :last-foot nil
         :current-flop 0
         :flop-counter 1
         :speed 0.08
         :player (the "Player")
         :difficulty (or (:difficulty
                          (load! edn/read-string :difficulty))
                         1.1)}]
    (active! go-txt false)
    (active! e-txt false)
    state))

(defn tick [{difficulty :difficulty
             ct :flop-counter
             :as state}]
  (-> state
      (update :flop-counter inc)
      (assoc  :current-flop
              (int (logp ct difficulty)))))

(defhook on-update [this state]
  (if (:restart state)
    (load-scene! 1)
    (-> state
        (tick)
        (player/controls)
        (player/act))))
