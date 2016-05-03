(ns game.player
  (:use folha.core))

(defn controls [{:keys [ctrls] :as state}]
  (->
   state
   (assoc :ctrls
          (-> ctrls
              (assoc :left  (key? "left"))
              (assoc :right (key? "right"))
              (assoc :up    (key? "up"))
              (assoc :down  (key? "down"))))
   (assoc :ctrls-down
          (-> ctrls
              (assoc :left  (key-down? "left"))
              (assoc :right (key-down? "right"))
              (assoc :up    (key-down? "up"))
              (assoc :down  (key-down? "down"))))))

(defn antidir [i]
  (cond
    (= i :left)  :right
    (= i :right) :left
    (= i :up)    :down
    (= i :down)  :up
    :else nil))

(defn act-dispatch [{:keys [ctrls heading last-foot ctrls-down]
                     :as state}]
  (let [{:keys [left right up down]} ctrls]
    (if (and heading (heading ctrls))
      (if (or (get ctrls-down (antidir heading))
              (get ctrls-down last-foot))
        :flop
        (if (= 1 (count (filter second ctrls-down)))
          :step
          :no-step))
      (let [pressed-keys (filter second ctrls)
            pkc (count pressed-keys)]
        (cond
          (= 0 pkc) :nothing
          (= 1 pkc) :turn
          :else     :flop)))))

(defn clear-ctrls [state]
  (-> state
      (assoc :heading   nil)
      (assoc :last-foot nil)))

(defn new-foot [{:keys [heading ctrls-down] :as state}]
  (ffirst (filter second (dissoc ctrls-down heading))))

(defn set-foot [{:keys [last-foot] :as state}]
  (assoc state :last-foot
         (or (antidir last-foot)
             (new-foot state))))

(def headings
  {:up    (v3  0  0  1)
   :down  (v3  0  0  -1)
   :left  (v3 -1  0  0)
   :right (v3  1  0  0)})

(def rotations
  {:down  (q4 0 0 0 1)
   :left  (q4 0.0 0.7071068 0.0 0.7071067)
   :up    (q4 0 1 0 0)
   :right (q4 0.0 0.7071066 0.0 -0.7071069)})

(defn move-snail! [player speed heading]
  (position! player
             (v3+ (v3* (get headings heading) speed)
                  (position player))))

(defmulti inner-act act-dispatch)

(defmethod inner-act
  :flop
  [{:keys [current-flop heading last-foot] :as state}]
  (-> state
      (assoc :flop current-flop
             :flop-state :new-flop)
      (clear-ctrls)))

(defmethod inner-act :no-step [state] state)
(defmethod inner-act :nothing [state] (clear-ctrls state))

(defmethod inner-act
  :turn
  [{:keys [ctrls player] :as state}]
  (let [{:keys [left right up down]} ctrls
        heading (cond
                  left  :left
                  right :right
                  up    :up
                  down  :down)]
    (rotation! player (rotations heading))
    (-> state
        (assoc :heading   heading)
        (assoc :last-foot nil))))

(defmethod inner-act
  :step
  [{:keys [speed player heading] :as state}]
  (move-snail! player speed heading)
  (-> (set-foot state)
      (assoc :flop-state nil)))

(defn act [{:keys [flop game-over] :as state}]
  (cond
    game-over state
    (zero? flop) (inner-act state)
    :else (let [state (update state :flop dec)]
            (if (zero? (:flop state))
              (assoc state :flop-state :stop-flop)
              (assoc state :flop-state nil)))))
