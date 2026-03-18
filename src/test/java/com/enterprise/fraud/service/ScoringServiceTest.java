package com.enterprise.fraud.service;

import com.enterprise.fraud.entity.FraudAlert;
import com.enterprise.fraud.entity.Transaction;
import com.enterprise.fraud.ml.FraudDetectionContext;
import com.enterprise.fraud.ml.FraudPrediction;
import com.enterprise.fraud.ml.TransactionFeatures;
import com.enterprise.fraud.repository.FraudAlertRepository;
import com.enterprise.fraud.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ScoringService with mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
class ScoringServiceTest {

    @Mock
    private FraudDetectionContext detectionContext;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private FraudAlertRepository fraudAlertRepository;

    @InjectMocks
    private ScoringService scoringService;

    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testTransaction = createTestTransaction();
    }

    @Test
    @DisplayName("analyzeTransaction - should create fraud alert for fraudulent transaction")
    void analyzeTransaction_FraudulentTransaction_CreatesPendingAlert() {
        FraudPrediction prediction = new FraudPrediction(0.92, true, "RANDOM_FOREST", 0.92);
        when(detectionContext.predict(any(TransactionFeatures.class))).thenReturn(prediction);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        when(fraudAlertRepository.save(any(FraudAlert.class))).thenAnswer(invocation -> {
            FraudAlert alert = invocation.getArgument(0);
            alert.setId(1L);
            return alert;
        });

        FraudAlert result = scoringService.analyzeTransaction(testTransaction);

        assertNotNull(result);
        assertTrue(result.getPrediction());
        assertEquals(0.92, result.getRiskScore());
        assertEquals("RANDOM_FOREST", result.getModelUsed());
        assertEquals(FraudAlert.AlertStatus.PENDING, result.getStatus());
    }

    @Test
    @DisplayName("analyzeTransaction - should dismiss alert for legitimate transaction")
    void analyzeTransaction_LegitimateTransaction_CreatesDismissedAlert() {
        FraudPrediction prediction = new FraudPrediction(0.1, false, "RANDOM_FOREST", 0.9);
        when(detectionContext.predict(any(TransactionFeatures.class))).thenReturn(prediction);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        when(fraudAlertRepository.save(any(FraudAlert.class))).thenAnswer(invocation -> {
            FraudAlert alert = invocation.getArgument(0);
            alert.setId(2L);
            return alert;
        });

        FraudAlert result = scoringService.analyzeTransaction(testTransaction);

        assertNotNull(result);
        assertFalse(result.getPrediction());
        assertEquals(FraudAlert.AlertStatus.DISMISSED, result.getStatus());
    }

    @Test
    @DisplayName("scoreTransaction - should return prediction without persisting")
    void scoreTransaction_ReturnsScoreWithoutPersisting() {
        FraudPrediction prediction = new FraudPrediction(0.75, true, "RULE_BASED", 0.75);
        when(detectionContext.predict(any(TransactionFeatures.class))).thenReturn(prediction);

        FraudPrediction result = scoringService.scoreTransaction(testTransaction);

        assertEquals(0.75, result.getRiskScore());
        assertTrue(result.isFraudulent());
        verify(transactionRepository, never()).save(any());
        verify(fraudAlertRepository, never()).save(any());
    }

    @Test
    @DisplayName("analyzeTransaction - should extract all 28 PCA features correctly")
    void analyzeTransaction_ExtractsAllPcaFeatures() {
        FraudPrediction prediction = new FraudPrediction(0.5, false, "RANDOM_FOREST", 0.5);
        when(detectionContext.predict(any(TransactionFeatures.class))).thenReturn(prediction);
        when(transactionRepository.save(any())).thenReturn(testTransaction);
        when(fraudAlertRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ArgumentCaptor<TransactionFeatures> featuresCaptor = ArgumentCaptor.forClass(TransactionFeatures.class);

        scoringService.analyzeTransaction(testTransaction);

        verify(detectionContext).predict(featuresCaptor.capture());
        TransactionFeatures features = featuresCaptor.getValue();
        assertEquals(28, features.getPcaFeatures().length);
        assertEquals(-1.36, features.getPcaFeatures()[0], 0.01); // V1
    }

    @Test
    @DisplayName("switchStrategy - should delegate to context")
    void switchStrategy_DelegatesToContext() {
        when(detectionContext.switchStrategy("RULE_BASED")).thenReturn(true);

        boolean result = scoringService.switchStrategy("RULE_BASED");

        assertTrue(result);
        verify(detectionContext).switchStrategy("RULE_BASED");
    }

    @Test
    @DisplayName("getStrategyInfo - should return context info")
    void getStrategyInfo_ReturnsContextInfo() {
        when(detectionContext.getActiveStrategyName()).thenReturn("RANDOM_FOREST");
        when(detectionContext.getAvailableStrategies()).thenReturn(
                Map.of("RANDOM_FOREST", true, "RULE_BASED", true));

        Map<String, Object> info = scoringService.getStrategyInfo();

        assertEquals("RANDOM_FOREST", info.get("activeStrategy"));
        assertNotNull(info.get("availableStrategies"));
    }

    // ==================== Helper ====================

    private Transaction createTestTransaction() {
        Transaction t = new Transaction();
        t.setId(1L);
        t.setTime(0.0);
        t.setV1(-1.36);
        t.setV2(-0.07);
        t.setV3(2.54);
        t.setV4(1.38);
        t.setV5(-0.34);
        t.setV6(0.46);
        t.setV7(0.24);
        t.setV8(0.10);
        t.setV9(0.36);
        t.setV10(0.09);
        t.setV11(-0.55);
        t.setV12(-0.62);
        t.setV13(-0.99);
        t.setV14(-0.31);
        t.setV15(1.47);
        t.setV16(-0.47);
        t.setV17(0.21);
        t.setV18(0.03);
        t.setV19(0.40);
        t.setV20(0.25);
        t.setV21(-0.02);
        t.setV22(0.28);
        t.setV23(-0.11);
        t.setV24(0.07);
        t.setV25(0.13);
        t.setV26(-0.19);
        t.setV27(0.13);
        t.setV28(-0.02);
        t.setAmount(BigDecimal.valueOf(149.62));
        t.setFraudClass(0);
        t.setNormalizedAmount(0.24);
        t.setTimeAnomaly(true);
        return t;
    }
}
