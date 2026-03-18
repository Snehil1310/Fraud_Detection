package com.enterprise.fraud.controller;

import com.enterprise.fraud.dto.TransactionAnalysisRequest;
import com.enterprise.fraud.entity.FraudAlert;
import com.enterprise.fraud.entity.Transaction;
import com.enterprise.fraud.ml.FraudPrediction;
import com.enterprise.fraud.repository.FraudAlertRepository;
import com.enterprise.fraud.repository.TransactionRepository;
import com.enterprise.fraud.service.ScoringService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for TransactionController using MockMvc.
 */
@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ScoringService scoringService;

    @MockBean
    private TransactionRepository transactionRepository;

    @MockBean
    private FraudAlertRepository fraudAlertRepository;

    private TransactionAnalysisRequest validRequest;
    private Transaction testTransaction;
    private FraudAlert testAlert;

    @BeforeEach
    void setUp() {
        validRequest = createValidRequest();
        testTransaction = createTestTransaction();
        testAlert = createTestAlert();
    }

    @Test
    @DisplayName("POST /analyze - should analyze transaction successfully")
    void analyzeTransaction_ValidRequest_ReturnsAnalysisResult() throws Exception {
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        when(scoringService.analyzeTransaction(any(Transaction.class))).thenReturn(testAlert);

        mockMvc.perform(post("/api/v1/transactions/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(1L))
                .andExpect(jsonPath("$.riskScore").exists())
                .andExpect(jsonPath("$.fraudulent").exists())
                .andExpect(jsonPath("$.modelUsed").value("RANDOM_FOREST"));
    }

    @Test
    @DisplayName("POST /analyze - should return 400 for invalid request")
    void analyzeTransaction_InvalidRequest_ReturnsBadRequest() throws Exception {
        TransactionAnalysisRequest invalidRequest = new TransactionAnalysisRequest();
        // Missing required fields

        mockMvc.perform(post("/api/v1/transactions/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /score - should score transaction without persisting")
    void scoreTransaction_ValidRequest_ReturnsScore() throws Exception {
        FraudPrediction prediction = new FraudPrediction(0.85, true, "RANDOM_FOREST", 0.85);
        when(scoringService.scoreTransaction(any(Transaction.class))).thenReturn(prediction);

        mockMvc.perform(post("/api/v1/transactions/score")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.riskScore").value(0.85))
                .andExpect(jsonPath("$.fraudulent").value(true));
    }

    @Test
    @DisplayName("GET /{id} - should return transaction by ID")
    void getTransaction_ExistingId_ReturnsTransaction() throws Exception {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        mockMvc.perform(get("/api/v1/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("GET /{id} - should return 404 for non-existent ID")
    void getTransaction_NonExistentId_ReturnsNotFound() throws Exception {
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/transactions/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET / - should return paginated transactions")
    void listTransactions_DefaultPagination_ReturnsPage() throws Exception {
        Page<Transaction> page = new PageImpl<>(List.of(testTransaction));
        when(transactionRepository.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /alerts - should return fraud alerts")
    void getAlerts_NoFilter_ReturnsAllAlerts() throws Exception {
        Page<FraudAlert> page = new PageImpl<>(List.of(testAlert));
        when(fraudAlertRepository.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/transactions/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /stats - should return dashboard statistics")
    void getStats_ReturnsSystemStats() throws Exception {
        when(transactionRepository.count()).thenReturn(284807L);
        when(transactionRepository.countByFraudClass(1)).thenReturn(492L);
        when(transactionRepository.countByFraudClass(0)).thenReturn(284315L);
        when(transactionRepository.calculateFraudRate()).thenReturn(0.172);
        when(transactionRepository.findAverageAmount()).thenReturn(BigDecimal.valueOf(88.35));
        when(transactionRepository.findMaxAmount()).thenReturn(BigDecimal.valueOf(25691.16));
        when(fraudAlertRepository.count()).thenReturn(492L);
        when(fraudAlertRepository.countByStatus(any())).thenReturn(100L);
        when(fraudAlertRepository.findAverageRiskScore()).thenReturn(0.65);
        when(scoringService.getStrategyInfo()).thenReturn(Map.of("activeStrategy", "RANDOM_FOREST"));

        mockMvc.perform(get("/api/v1/transactions/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTransactions").value(284807L))
                .andExpect(jsonPath("$.totalFraudulent").value(492L));
    }

    @Test
    @DisplayName("POST /strategy/switch - should switch strategy")
    void switchStrategy_ValidStrategy_ReturnsSuccess() throws Exception {
        when(scoringService.switchStrategy("RULE_BASED")).thenReturn(true);
        when(scoringService.getStrategyInfo()).thenReturn(
                Map.of("activeStrategy", "RULE_BASED"));

        mockMvc.perform(post("/api/v1/transactions/strategy/switch")
                .param("strategyName", "RULE_BASED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ==================== Helper Methods ====================

    private TransactionAnalysisRequest createValidRequest() {
        TransactionAnalysisRequest req = new TransactionAnalysisRequest();
        req.setTime(0.0);
        req.setV1(-1.36);
        req.setV2(-0.07);
        req.setV3(2.54);
        req.setV4(1.38);
        req.setV5(-0.34);
        req.setV6(0.46);
        req.setV7(0.24);
        req.setV8(0.10);
        req.setV9(0.36);
        req.setV10(0.09);
        req.setV11(-0.55);
        req.setV12(-0.62);
        req.setV13(-0.99);
        req.setV14(-0.31);
        req.setV15(1.47);
        req.setV16(-0.47);
        req.setV17(0.21);
        req.setV18(0.03);
        req.setV19(0.40);
        req.setV20(0.25);
        req.setV21(-0.02);
        req.setV22(0.28);
        req.setV23(-0.11);
        req.setV24(0.07);
        req.setV25(0.13);
        req.setV26(-0.19);
        req.setV27(0.13);
        req.setV28(-0.02);
        req.setAmount(BigDecimal.valueOf(149.62));
        return req;
    }

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
        t.setCreatedAt(LocalDateTime.now());
        return t;
    }

    private FraudAlert createTestAlert() {
        FraudAlert alert = new FraudAlert();
        alert.setId(1L);
        alert.setTransaction(testTransaction);
        alert.setRiskScore(0.85);
        alert.setPrediction(true);
        alert.setModelUsed("RANDOM_FOREST");
        alert.setStatus(FraudAlert.AlertStatus.PENDING);
        alert.setAlertTimestamp(LocalDateTime.now());
        return alert;
    }
}
