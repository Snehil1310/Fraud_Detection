package com.enterprise.fraud.ml;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for fraud detection strategies and the strategy pattern.
 */
class FraudDetectionStrategyTest {

    @Test
    @DisplayName("RuleBasedStrategy - should detect high amount fraud")
    void ruleBasedStrategy_HighAmount_DetectsFraud() {
        RuleBasedStrategy strategy = new RuleBasedStrategy();

        double[] pcaFeatures = new double[28];
        // Set key PCA features to extreme values (V14 = index 13)
        pcaFeatures[13] = -10.0; // V14 extreme negative

        TransactionFeatures features = new TransactionFeatures(pcaFeatures, 5.0, 3600, true);
        FraudPrediction prediction = strategy.predict(features);

        assertNotNull(prediction);
        assertEquals("RULE_BASED", prediction.getModelUsed());
        assertTrue(prediction.getRiskScore() > 0.0);
    }

    @Test
    @DisplayName("RuleBasedStrategy - should classify normal transaction as legitimate")
    void ruleBasedStrategy_NormalTransaction_ClassifiesLegitimate() {
        RuleBasedStrategy strategy = new RuleBasedStrategy();

        double[] pcaFeatures = new double[28]; // All zeros = normal
        TransactionFeatures features = new TransactionFeatures(pcaFeatures, 0.5, 50000, false);
        FraudPrediction prediction = strategy.predict(features);

        assertNotNull(prediction);
        assertFalse(prediction.isFraudulent());
        assertTrue(prediction.getRiskScore() < 0.5);
    }

    @Test
    @DisplayName("RuleBasedStrategy - should always be ready")
    void ruleBasedStrategy_IsAlwaysReady() {
        RuleBasedStrategy strategy = new RuleBasedStrategy();
        assertTrue(strategy.isReady());
    }

    @Test
    @DisplayName("TransactionFeatures - should validate PCA features length")
    void transactionFeatures_InvalidLength_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new TransactionFeatures(new double[10], 0.5, 0, false));
    }

    @Test
    @DisplayName("TransactionFeatures - should create valid feature array")
    void transactionFeatures_ToFeatureArray_Returns30Elements() {
        double[] pcaFeatures = new double[28];
        pcaFeatures[0] = 1.5;
        TransactionFeatures features = new TransactionFeatures(pcaFeatures, 0.8, 100, false);

        double[] array = features.toFeatureArray();
        assertEquals(30, array.length);
        assertEquals(1.5, array[0]);
        assertEquals(0.8, array[28]);
        assertEquals(100, array[29]);
    }

    @Test
    @DisplayName("FraudPrediction - should hold prediction values correctly")
    void fraudPrediction_HoldsValues() {
        FraudPrediction prediction = new FraudPrediction(0.95, true, "RANDOM_FOREST", 0.95);

        assertEquals(0.95, prediction.getRiskScore());
        assertTrue(prediction.isFraudulent());
        assertEquals("RANDOM_FOREST", prediction.getModelUsed());
        assertEquals(0.95, prediction.getConfidence());
    }

    @Test
    @DisplayName("RuleBasedStrategy - time anomaly should increase risk score")
    void ruleBasedStrategy_TimeAnomaly_IncreasesRisk() {
        RuleBasedStrategy strategy = new RuleBasedStrategy();

        double[] pcaFeatures = new double[28];
        TransactionFeatures noAnomaly = new TransactionFeatures(pcaFeatures, 0.5, 50000, false);
        TransactionFeatures withAnomaly = new TransactionFeatures(pcaFeatures, 0.5, 3600, true);

        FraudPrediction noAnomalyPred = strategy.predict(noAnomaly);
        FraudPrediction withAnomalyPred = strategy.predict(withAnomaly);

        assertTrue(withAnomalyPred.getRiskScore() > noAnomalyPred.getRiskScore());
    }
}
