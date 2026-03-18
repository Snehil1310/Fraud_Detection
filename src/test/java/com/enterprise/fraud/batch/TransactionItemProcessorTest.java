package com.enterprise.fraud.batch;

import com.enterprise.fraud.dto.TransactionCsvDto;
import com.enterprise.fraud.entity.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TransactionItemProcessor (Feature Engineering).
 */
class TransactionItemProcessorTest {

    private TransactionItemProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new TransactionItemProcessor();
    }

    @Test
    @DisplayName("Should normalize amount using z-score")
    void process_NormalizesAmount() throws Exception {
        TransactionCsvDto dto = createDto(0.0, 149.62);

        Transaction result = processor.process(dto);

        assertNotNull(result);
        assertNotNull(result.getNormalizedAmount());
        // Expected: (149.62 - 88.35) / 250.12 ≈ 0.245
        assertEquals(0.245, result.getNormalizedAmount(), 0.01);
    }

    @Test
    @DisplayName("Should detect time anomaly for early morning transactions")
    void process_DetectsTimeAnomaly() throws Exception {
        TransactionCsvDto dto = createDto(3600.0, 100.0); // 1 AM

        Transaction result = processor.process(dto);

        assertNotNull(result);
        assertTrue(result.getTimeAnomaly());
    }

    @Test
    @DisplayName("Should not flag afternoon transactions as anomalies")
    void process_NoAnomalyForAfternoon() throws Exception {
        TransactionCsvDto dto = createDto(50000.0, 100.0); // Afternoon

        Transaction result = processor.process(dto);

        assertNotNull(result);
        assertFalse(result.getTimeAnomaly());
    }

    @Test
    @DisplayName("Should map all PCA features correctly")
    void process_MapsAllPcaFeatures() throws Exception {
        TransactionCsvDto dto = createDto(0.0, 100.0);
        dto.setV1(-1.36);
        dto.setV14(-5.0);

        Transaction result = processor.process(dto);

        assertEquals(-1.36, result.getV1());
        assertEquals(-5.0, result.getV14());
    }

    @Test
    @DisplayName("Should set correct amount with 2 decimal scale")
    void process_SetsAmountWithScale() throws Exception {
        TransactionCsvDto dto = createDto(0.0, 149.625);

        Transaction result = processor.process(dto);

        assertEquals(new BigDecimal("149.63"), result.getAmount());
    }

    @Test
    @DisplayName("Should track processed count")
    void process_TracksCount() throws Exception {
        assertEquals(0, processor.getProcessedCount());

        processor.process(createDto(0.0, 100.0));
        processor.process(createDto(0.0, 200.0));

        assertEquals(2, processor.getProcessedCount());
    }

    // ==================== Helper ====================

    private TransactionCsvDto createDto(double time, double amount) {
        TransactionCsvDto dto = new TransactionCsvDto();
        dto.setTime(time);
        dto.setAmount(amount);
        dto.setFraudClass(0);
        dto.setV1(0.0);
        dto.setV2(0.0);
        dto.setV3(0.0);
        dto.setV4(0.0);
        dto.setV5(0.0);
        dto.setV6(0.0);
        dto.setV7(0.0);
        dto.setV8(0.0);
        dto.setV9(0.0);
        dto.setV10(0.0);
        dto.setV11(0.0);
        dto.setV12(0.0);
        dto.setV13(0.0);
        dto.setV14(0.0);
        dto.setV15(0.0);
        dto.setV16(0.0);
        dto.setV17(0.0);
        dto.setV18(0.0);
        dto.setV19(0.0);
        dto.setV20(0.0);
        dto.setV21(0.0);
        dto.setV22(0.0);
        dto.setV23(0.0);
        dto.setV24(0.0);
        dto.setV25(0.0);
        dto.setV26(0.0);
        dto.setV27(0.0);
        dto.setV28(0.0);
        return dto;
    }
}
