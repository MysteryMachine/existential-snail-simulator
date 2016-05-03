(ns game.enemy
  (:use folha.core)
  (:require [game.core :refer [game-over]]))

(defn on-collision [this collision]
  (when (= (the "Snail") (.gameObject collision))
    (game-over)))

(defhook on-start [this state]
  {:life 600})

(defhook on-update [this state]
  (if (zero? (:life state))
    (destroy! this)
    (update state :life dec)))
