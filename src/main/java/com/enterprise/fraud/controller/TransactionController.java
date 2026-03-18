package com.enterprise.fraud.controller;

import com.enterprise.fraud.dto.TransactionAnalysisRequest;
import com.enterprise.fraud.dto.TransactionAnalysisResponse;
import com.enterprise.fraud.entity.FraudAlert;
import com.enterprise.fraud.entity.Transaction;
import com.enterprise.fraud.exception.ResourceNotFoundException;
import com.enterprise.fraud.ml.FraudPrediction;
import com.enterprise.fraud.repository.FraudAlertRepository;
import com.enterprise.fraud.repository.TransactionRepository;
import com.enterprise.fraud.service.ScoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for transaction analysis and fraud detection operations.
 * Provides standardized endpoints for analyzing transactions, retrieving
 * alerts,
 * and viewing system statistics.
 */
@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transaction Analysis", description = "Endpoints for fraud detection and transaction management")
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    private final ScoringService scoringService;
    private final TransactionRepository transactionRepository;
    private final FraudAlertRepository fraudAlertRepository;

    public TransactionController(ScoringService scoringService,
            TransactionRepository transactionRepository,
            FraudAlertRepository fraudAlertRepository) {
        this.scoringService = scoringService;
        this.transactionRepository = transactionRepository;
        this.fraudAlertRepository = fraudAlertRepository;
    }

    // ==================== Analyze Endpoints ====================

    @PostMapping("/analyze")
    @Operation(summary = "Analyze a single transaction", description = "Submit a transaction for fraud analysis using the active ML model")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Analysis completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "503", description = "ML model unavailable")
    })
    public ResponseEntity<TransactionAnalysisResponse> analyzeTransaction(
            @Valid @RequestBody TransactionAnalysisRequest request) {

        log.info("Received transaction for analysis, amount: {}", request.getAmount());

        // Convert request to entity
        Transaction transaction = mapRequestToEntity(request);
        transaction = transactionRepository.save(transaction);

        // Analyze and get alert
        FraudAlert alert = scoringService.analyzeTransaction(transaction);

        // Build response
        TransactionAnalysisResponse response = new TransactionAnalysisResponse(
                transaction.getId(),
                alert.getRiskScore(),
                alert.getPrediction(),
                alert.getModelUsed(),
                alert.getRiskScore(), // confidence
                alert.getId());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/score")
    @Operation(summary = "Score a transaction without persisting", description = "Quick scoring endpoint for real-time analysis")
    public ResponseEntity<Map<String, Object>> scoreTransaction(
            @Valid @RequestBody TransactionAnalysisRequest request) {

        Transaction transaction = mapRequestToEntity(request);
        FraudPrediction prediction = scoringService.scoreTransaction(transaction);

        Map<String, Object> result = new HashMap<>();
        result.put("riskScore", prediction.getRiskScore());
        result.put("fraudulent", prediction.isFraudulent());
        result.put("modelUsed", prediction.getModelUsed());
        result.put("confidence", prediction.getConfidence());

        return ResponseEntity.ok(result);
    }

    // ==================== Transaction Endpoints ====================

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID")
    public ResponseEntity<Transaction> getTransaction(@PathVariable Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", id));
        return ResponseEntity.ok(transaction);
    }

    @GetMapping
    @Operation(summary = "List transactions with pagination")
    public ResponseEntity<Page<Transaction>> listTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy) {

        Page<Transaction> transactions = transactionRepository.findAll(
                PageRequest.of(page, size, Sort.by(sortBy).descending()));
        return ResponseEntity.ok(transactions);
    }

    // ==================== Alerts Endpoints ====================

    @GetMapping("/alerts")
    @Operation(summary = "Get fraud alerts", description = "Retrieve fraud alerts with optional status filter")
    public ResponseEntity<Page<FraudAlert>> getAlerts(
            @RequestParam(required = false) FraudAlert.AlertStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<FraudAlert> alerts;
        if (status != null) {
            alerts = fraudAlertRepository.findByStatus(status,
                    PageRequest.of(page, size, Sort.by("alertTimestamp").descending()));
        } else {
            alerts = fraudAlertRepository.findAll(
                    PageRequest.of(page, size, Sort.by("alertTimestamp").descending()));
        }
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/alerts/recent")
    @Operation(summary = "Get most recent fraud alerts")
    public ResponseEntity<List<FraudAlert>> getRecentAlerts() {
        return ResponseEntity.ok(fraudAlertRepository.findTop10ByOrderByAlertTimestampDesc());
    }

    // ==================== Statistics Endpoint ====================

    @GetMapping("/stats")
    @Operation(summary = "Get dashboard statistics", description = "Returns system-wide statistics for the dashboard")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalTransactions", transactionRepository.count());
        stats.put("totalFraudulent", transactionRepository.countByFraudClass(1));
        stats.put("totalLegitimate", transactionRepository.countByFraudClass(0));
        stats.put("fraudRate", transactionRepository.calculateFraudRate());
        stats.put("averageAmount", transactionRepository.findAverageAmount());
        stats.put("maxAmount", transactionRepository.findMaxAmount());
        stats.put("totalAlerts", fraudAlertRepository.count());
        stats.put("pendingAlerts", fraudAlertRepository.countByStatus(FraudAlert.AlertStatus.PENDING));
        stats.put("averageRiskScore", fraudAlertRepository.findAverageRiskScore());
        stats.put("strategyInfo", scoringService.getStrategyInfo());

        return ResponseEntity.ok(stats);
    }

    // ==================== Strategy Management ====================

    @PostMapping("/strategy/switch")
    @Operation(summary = "Switch ML strategy", description = "Switch between fraud detection strategies at runtime")
    public ResponseEntity<Map<String, Object>> switchStrategy(
            @RequestParam String strategyName) {

        boolean switched = scoringService.switchStrategy(strategyName);
        Map<String, Object> result = new HashMap<>();
        result.put("success", switched);
        result.put("strategyInfo", scoringService.getStrategyInfo());

        return switched ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }

    // ==================== Helper Methods ====================

    private Transaction mapRequestToEntity(TransactionAnalysisRequest request) {
        Transaction t = new Transaction();
        t.setTime(request.getTime());
        t.setV1(request.getV1());
        t.setV2(request.getV2());
        t.setV3(request.getV3());
        t.setV4(request.getV4());
        t.setV5(request.getV5());
        t.setV6(request.getV6());
        t.setV7(request.getV7());
        t.setV8(request.getV8());
        t.setV9(request.getV9());
        t.setV10(request.getV10());
        t.setV11(request.getV11());
        t.setV12(request.getV12());
        t.setV13(request.getV13());
        t.setV14(request.getV14());
        t.setV15(request.getV15());
        t.setV16(request.getV16());
        t.setV17(request.getV17());
        t.setV18(request.getV18());
        t.setV19(request.getV19());
        t.setV20(request.getV20());
        t.setV21(request.getV21());
        t.setV22(request.getV22());
        t.setV23(request.getV23());
        t.setV24(request.getV24());
        t.setV25(request.getV25());
        t.setV26(request.getV26());
        t.setV27(request.getV27());
        t.setV28(request.getV28());
        t.setAmount(request.getAmount().setScale(2, RoundingMode.HALF_UP));
        t.setFraudClass(0); // Default to non-fraud; scoring determines actual class

        // Compute normalized amount
        double normalizedAmount = (request.getAmount().doubleValue() - 88.35) / 250.12;
        t.setNormalizedAmount(normalizedAmount);

        // Compute time anomaly
        double timeInDay = request.getTime() % 86400.0;
        t.setTimeAnomaly(timeInDay >= 0 && timeInDay <= 21600.0);

        return t;
    }
}
