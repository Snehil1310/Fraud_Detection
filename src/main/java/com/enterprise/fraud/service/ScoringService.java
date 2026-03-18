package com.enterprise.fraud.service;

import com.enterprise.fraud.entity.FraudAlert;
import com.enterprise.fraud.entity.Transaction;
import com.enterprise.fraud.ml.FraudDetectionContext;
import com.enterprise.fraud.ml.FraudPrediction;
import com.enterprise.fraud.ml.TransactionFeatures;
import com.enterprise.fraud.repository.FraudAlertRepository;
import com.enterprise.fraud.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Service orchestrating fraud scoring operations.
 * Accepts transaction data, extracts features, delegates to the active ML
 * strategy,
 * and persists fraud alerts.
 */
@Service
public class ScoringService {

    private static final Logger log = LoggerFactory.getLogger(ScoringService.class);

    private final FraudDetectionContext detectionContext;
    private final TransactionRepository transactionRepository;
    private final FraudAlertRepository fraudAlertRepository;

    public ScoringService(FraudDetectionContext detectionContext,
            TransactionRepository transactionRepository,
            FraudAlertRepository fraudAlertRepository) {
        this.detectionContext = detectionContext;
        this.transactionRepository = transactionRepository;
        this.fraudAlertRepository = fraudAlertRepository;
    }

    /**
     * Analyze a transaction for fraud. Extracts features, runs ML prediction,
     * and creates a FraudAlert record.
     *
     * @param transaction the transaction to analyze
     * @return the fraud alert with prediction results
     */
    @Transactional
    public FraudAlert analyzeTransaction(Transaction transaction) {
        log.info("Analyzing transaction ID: {}", transaction.getId());

        // Extract features from the transaction
        TransactionFeatures features = extractFeatures(transaction);

        // Run prediction through the active strategy
        FraudPrediction prediction = detectionContext.predict(features);

        // Mark transaction as processed
        transaction.setProcessedAt(LocalDateTime.now());
        transactionRepository.save(transaction);

        // Create and persist the fraud alert
        FraudAlert alert = new FraudAlert();
        alert.setTransaction(transaction);
        alert.setRiskScore(prediction.getRiskScore());
        alert.setPrediction(prediction.isFraudulent());
        alert.setModelUsed(prediction.getModelUsed());
        alert.setStatus(prediction.isFraudulent() ? FraudAlert.AlertStatus.PENDING : FraudAlert.AlertStatus.DISMISSED);

        FraudAlert savedAlert = fraudAlertRepository.save(alert);

        log.info("Transaction {} analyzed: fraud={}, riskScore={}, model={}",
                transaction.getId(), prediction.isFraudulent(),
                prediction.getRiskScore(), prediction.getModelUsed());

        return savedAlert;
    }

    /**
     * Analyze a transaction without persisting (for real-time API calls).
     */
    public FraudPrediction scoreTransaction(Transaction transaction) {
        TransactionFeatures features = extractFeatures(transaction);
        return detectionContext.predict(features);
    }

    /**
     * Switch the active fraud detection strategy.
     */
    public boolean switchStrategy(String strategyName) {
        return detectionContext.switchStrategy(strategyName);
    }

    /**
     * Get the currently active strategy info.
     */
    public Map<String, Object> getStrategyInfo() {
        return Map.of(
                "activeStrategy", detectionContext.getActiveStrategyName(),
                "availableStrategies", detectionContext.getAvailableStrategies());
    }

    /**
     * Extract ML features from a Transaction entity.
     * Maps the Kaggle dataset's anonymized variables (V1-V28) to model inputs.
     */
    private TransactionFeatures extractFeatures(Transaction transaction) {
        double[] pcaFeatures = new double[] {
                transaction.getV1(), transaction.getV2(), transaction.getV3(),
                transaction.getV4(), transaction.getV5(), transaction.getV6(),
                transaction.getV7(), transaction.getV8(), transaction.getV9(),
                transaction.getV10(), transaction.getV11(), transaction.getV12(),
                transaction.getV13(), transaction.getV14(), transaction.getV15(),
                transaction.getV16(), transaction.getV17(), transaction.getV18(),
                transaction.getV19(), transaction.getV20(), transaction.getV21(),
                transaction.getV22(), transaction.getV23(), transaction.getV24(),
                transaction.getV25(), transaction.getV26(), transaction.getV27(),
                transaction.getV28()
        };

        double normalizedAmount = transaction.getNormalizedAmount() != null ? transaction.getNormalizedAmount() : 0.0;
        boolean timeAnomaly = transaction.getTimeAnomaly() != null ? transaction.getTimeAnomaly() : false;

        return new TransactionFeatures(pcaFeatures, normalizedAmount, transaction.getTime(), timeAnomaly);
    }
}
