package com.enterprise.fraud.ml;

/**
 * Represents the prediction result from a fraud detection strategy.
 */
public class FraudPrediction {

    private final double riskScore; // 0.0 (legitimate) to 1.0 (fraud)
    private final boolean fraudulent; // true if predicted as fraud
    private final String modelUsed; // Name of the model/strategy
    private final double confidence; // Model confidence in the prediction

    public FraudPrediction(double riskScore, boolean fraudulent, String modelUsed, double confidence) {
        this.riskScore = riskScore;
        this.fraudulent = fraudulent;
        this.modelUsed = modelUsed;
        this.confidence = confidence;
    }

    public double getRiskScore() {
        return riskScore;
    }

    public boolean isFraudulent() {
        return fraudulent;
    }

    public String getModelUsed() {
        return modelUsed;
    }

    public double getConfidence() {
        return confidence;
    }

    @Override
    public String toString() {
        return "FraudPrediction{riskScore=" + riskScore +
                ", fraudulent=" + fraudulent +
                ", modelUsed='" + modelUsed + '\'' +
                ", confidence=" + confidence + '}';
    }
}
