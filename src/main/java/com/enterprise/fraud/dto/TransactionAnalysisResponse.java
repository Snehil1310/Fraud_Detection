package com.enterprise.fraud.dto;

/**
 * Response DTO for transaction analysis results.
 */
public class TransactionAnalysisResponse {

    private Long transactionId;
    private double riskScore;
    private boolean fraudulent;
    private String modelUsed;
    private double confidence;
    private Long alertId;

    public TransactionAnalysisResponse() {
    }

    public TransactionAnalysisResponse(Long transactionId, double riskScore, boolean fraudulent,
            String modelUsed, double confidence, Long alertId) {
        this.transactionId = transactionId;
        this.riskScore = riskScore;
        this.fraudulent = fraudulent;
        this.modelUsed = modelUsed;
        this.confidence = confidence;
        this.alertId = alertId;
    }

    // Getters & Setters
    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(double riskScore) {
        this.riskScore = riskScore;
    }

    public boolean isFraudulent() {
        return fraudulent;
    }

    public void setFraudulent(boolean fraudulent) {
        this.fraudulent = fraudulent;
    }

    public String getModelUsed() {
        return modelUsed;
    }

    public void setModelUsed(String modelUsed) {
        this.modelUsed = modelUsed;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public Long getAlertId() {
        return alertId;
    }

    public void setAlertId(Long alertId) {
        this.alertId = alertId;
    }
}
