(ns game.core
  (:use folha.core))

(defapi GameEvents
  (end-game [this]
   (swap!! this #(assoc % :game-over true)))
  (restart [this]
   (swap!! this #(assoc % :game-over false
                          :restart true))))

(defn game-over [] (end-game (the "Data")))
