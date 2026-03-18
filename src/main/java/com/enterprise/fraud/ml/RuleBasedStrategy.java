package com.enterprise.fraud.ml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Rule-based fallback strategy for fraud detection.
 * Uses configurable threshold rules when ML models are unavailable or during
 * warm-up.
 * <p>
 * Rules applied:
 * 1. High amount transactions (normalized amount > 2 std deviations)
 * 2. Time anomaly (transaction during unusual hours)
 * 3. PCA feature outliers (extreme values in key principal components)
 */
@Component
public class RuleBasedStrategy implements FraudDetectionStrategy {

    private static final Logger log = LoggerFactory.getLogger(RuleBasedStrategy.class);
    private static final String STRATEGY_NAME = "RULE_BASED";

    // Configurable thresholds
    private static final double HIGH_AMOUNT_THRESHOLD = 2.0; // 2 std deviations above mean
    private static final double PCA_OUTLIER_THRESHOLD = 5.0; // Extreme PCA values
    private static final double RISK_WEIGHT_AMOUNT = 0.3;
    private static final double RISK_WEIGHT_TIME = 0.2;
    private static final double RISK_WEIGHT_PCA = 0.5;

    // Key PCA components most correlated with fraud (V14, V12, V10, V17)
    private static final int[] KEY_PCA_INDICES = { 13, 11, 9, 16 }; // 0-indexed

    @Override
    public FraudPrediction predict(TransactionFeatures features) {
        double riskScore = 0.0;

        // Rule 1: High amount anomaly
        double amountRisk = calculateAmountRisk(features.getNormalizedAmount());
        riskScore += amountRisk * RISK_WEIGHT_AMOUNT;

        // Rule 2: Time-based anomaly
        double timeRisk = features.isTimeAnomaly() ? 1.0 : 0.0;
        riskScore += timeRisk * RISK_WEIGHT_TIME;

        // Rule 3: PCA feature outliers
        double pcaRisk = calculatePcaRisk(features.getPcaFeatures());
        riskScore += pcaRisk * RISK_WEIGHT_PCA;

        // Clamp risk score to [0, 1]
        riskScore = Math.max(0.0, Math.min(1.0, riskScore));

        boolean isFraudulent = riskScore >= 0.5;
        double confidence = Math.abs(riskScore - 0.5) * 2; // Confidence based on distance from threshold

        log.debug("Rule-based prediction: amount_risk={}, time_risk={}, pca_risk={}, total={}",
                amountRisk, timeRisk, pcaRisk, riskScore);

        return new FraudPrediction(riskScore, isFraudulent, STRATEGY_NAME, confidence);
    }

    private double calculateAmountRisk(double normalizedAmount) {
        if (Math.abs(normalizedAmount) > HIGH_AMOUNT_THRESHOLD) {
            return Math.min(1.0, Math.abs(normalizedAmount) / (HIGH_AMOUNT_THRESHOLD * 2));
        }
        return 0.0;
    }

    private double calculatePcaRisk(double[] pcaFeatures) {
        double maxOutlierScore = 0.0;

        for (int idx : KEY_PCA_INDICES) {
            if (idx < pcaFeatures.length) {
                double absValue = Math.abs(pcaFeatures[idx]);
                if (absValue > PCA_OUTLIER_THRESHOLD) {
                    double outlierScore = Math.min(1.0, absValue / (PCA_OUTLIER_THRESHOLD * 2));
                    maxOutlierScore = Math.max(maxOutlierScore, outlierScore);
                }
            }
        }

        return maxOutlierScore;
    }

    @Override
    public String getStrategyName() {
        return STRATEGY_NAME;
    }

    @Override
    public boolean isReady() {
        return true; // Rule-based engine is always ready
    }
}
