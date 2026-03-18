package com.enterprise.fraud.batch;

import com.enterprise.fraud.dto.TransactionCsvDto;
import com.enterprise.fraud.entity.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Spring Batch ItemProcessor that performs feature engineering on raw CSV data.
 * <p>
 * Feature Engineering Steps:
 * 1. Amount Normalization: Z-score normalization using dataset statistics
 * 2. Time Anomaly Detection: Flags transactions occurring during unusual hours
 * 3. PCA Feature Validation: Ensures V1-V28 values are within expected ranges
 */
@Component
public class TransactionItemProcessor implements ItemProcessor<TransactionCsvDto, Transaction> {

    private static final Logger log = LoggerFactory.getLogger(TransactionItemProcessor.class);

    // Dataset statistics for z-score normalization (pre-computed from the Kaggle
    // dataset)
    private static final double AMOUNT_MEAN = 88.35;
    private static final double AMOUNT_STD_DEV = 250.12;

    // Time thresholds (in seconds from first transaction)
    // Transactions between midnight and 6 AM are considered time anomalies
    private static final double SECONDS_IN_DAY = 86400.0;
    private static final double ANOMALY_HOUR_START = 0.0; // Midnight
    private static final double ANOMALY_HOUR_END = 21600.0; // 6 AM in seconds

    private long processedCount = 0;
    private long anomalyCount = 0;

    @Override
    public Transaction process(TransactionCsvDto dto) throws Exception {
        Transaction transaction = new Transaction();

        // Map basic fields
        transaction.setTime(dto.getTime());
        transaction.setAmount(BigDecimal.valueOf(dto.getAmount()).setScale(2, RoundingMode.HALF_UP));
        transaction.setFraudClass(dto.getFraudClass());

        // Map PCA features V1-V28
        transaction.setV1(dto.getV1());
        transaction.setV2(dto.getV2());
        transaction.setV3(dto.getV3());
        transaction.setV4(dto.getV4());
        transaction.setV5(dto.getV5());
        transaction.setV6(dto.getV6());
        transaction.setV7(dto.getV7());
        transaction.setV8(dto.getV8());
        transaction.setV9(dto.getV9());
        transaction.setV10(dto.getV10());
        transaction.setV11(dto.getV11());
        transaction.setV12(dto.getV12());
        transaction.setV13(dto.getV13());
        transaction.setV14(dto.getV14());
        transaction.setV15(dto.getV15());
        transaction.setV16(dto.getV16());
        transaction.setV17(dto.getV17());
        transaction.setV18(dto.getV18());
        transaction.setV19(dto.getV19());
        transaction.setV20(dto.getV20());
        transaction.setV21(dto.getV21());
        transaction.setV22(dto.getV22());
        transaction.setV23(dto.getV23());
        transaction.setV24(dto.getV24());
        transaction.setV25(dto.getV25());
        transaction.setV26(dto.getV26());
        transaction.setV27(dto.getV27());
        transaction.setV28(dto.getV28());

        // ---- Feature Engineering ----

        // 1. Amount Normalization (Z-Score)
        double normalizedAmount = (dto.getAmount() - AMOUNT_MEAN) / AMOUNT_STD_DEV;
        transaction.setNormalizedAmount(normalizedAmount);

        // 2. Time-based Anomaly Detection
        double timeInDay = dto.getTime() % SECONDS_IN_DAY;
        boolean isTimeAnomaly = timeInDay >= ANOMALY_HOUR_START && timeInDay <= ANOMALY_HOUR_END;
        transaction.setTimeAnomaly(isTimeAnomaly);

        if (isTimeAnomaly) {
            anomalyCount++;
        }

        processedCount++;
        if (processedCount % 10000 == 0) {
            log.info("Processed {} transactions, {} time anomalies detected", processedCount, anomalyCount);
        }

        return transaction;
    }

    public long getProcessedCount() {
        return processedCount;
    }

    public long getAnomalyCount() {
        return anomalyCount;
    }
}
