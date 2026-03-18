package com.enterprise.fraud.ml;

/**
 * Strategy interface for fraud detection algorithms.
 * Implements the Strategy Design Pattern to allow seamless switching
 * between different ML models or rule-based engines without downtime.
 */
public interface FraudDetectionStrategy {

    /**
     * Predict whether a transaction is fraudulent.
     *
     * @param features the transaction features (V1-V28, normalized amount, time)
     * @return prediction result containing risk score and fraud classification
     */
    FraudPrediction predict(TransactionFeatures features);

    /**
     * Get the name of this strategy/model.
     *
     * @return strategy name (e.g., "RANDOM_FOREST", "RULE_BASED")
     */
    String getStrategyName();

    /**
     * Check if this strategy is ready for predictions.
     *
     * @return true if the model is loaded and ready
     */
    boolean isReady();
}
