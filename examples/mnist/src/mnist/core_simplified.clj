(ns mnist.core-simplified
    (:require
        [clojure.core.matrix :as mat]
        [cortex.optimise :as opt]
        [thinktopic.datasets.mnist :as mnist]
        [cortex.network :as net]))

(defn- mnist-labels
    [class-labels]
    (let [n-labels (count class-labels)
          labels (mat/zero-array [n-labels 10])]
        (doseq [i (range n-labels)]
            (mat/mset! labels i (nth class-labels i) 1.0))
        labels))

(defn train [network training-data training-labels]
    (let [n-epochs 1
          learning-rate 0.3
          momentum 0.1
          batch-size 10
          loss-fn (opt/quadratic-loss)
          optimizer (net/sgd-optimizer network loss-fn learning-rate momentum)]
        (net/train-network optimizer n-epochs batch-size training-data training-labels)))

(defn evaluate [network test-data test-labels]
    (let [[results score] (net/evaluate network test-data test-labels)
          label-count (count test-data)
          score-percent (float (/ score label-count))]
        (println (format "Score: %f [%d of %d]" score-percent score label-count))))

(defn run-experiment []
    (let [training-data  (map #(vec [%]) @mnist/data-store)
          training-labels (mnist-labels @mnist/label-store)
          test-data  (map #(vec [%]) @mnist/test-data-store)
          test-labels (mnist-labels @mnist/test-label-store)

          input-width (last (mat/shape training-data))
          output-width (last (mat/shape training-labels))
          hidden-layer-size 30

          network (net/sequential-network
                      [(net/linear-layer :n-inputs input-width :n-outputs hidden-layer-size)
                       (net/sigmoid-activation hidden-layer-size)
                       (net/linear-layer :n-inputs hidden-layer-size :n-outputs output-width)])]


        (println "Training...")
        (train network training-data training-labels)
        (println "Testing...")
        (evaluate network test-data test-labels)))

(defn -main []
    (run-experiment))