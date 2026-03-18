package com.enterprise.fraud.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity representing a credit card transaction.
 * Maps to the Kaggle Credit Card Fraud Detection dataset columns.
 * Contains 28 PCA-transformed features (V1-V28), Time, Amount, and Class.
 */
@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_transaction_fraud_class", columnList = "fraudClass"),
    @Index(name = "idx_transaction_amount", columnList = "amount"),
    @Index(name = "idx_transaction_created_at", columnList = "createdAt")
})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Time elapsed is required")
    @Column(nullable = false)
    private Double time;

    // PCA-transformed features V1 through V28
    @Column(nullable = false) private Double v1;
    @Column(nullable = false) private Double v2;
    @Column(nullable = false) private Double v3;
    @Column(nullable = false) private Double v4;
    @Column(nullable = false) private Double v5;
    @Column(nullable = false) private Double v6;
    @Column(nullable = false) private Double v7;
    @Column(nullable = false) private Double v8;
    @Column(nullable = false) private Double v9;
    @Column(nullable = false) private Double v10;
    @Column(nullable = false) private Double v11;
    @Column(nullable = false) private Double v12;
    @Column(nullable = false) private Double v13;
    @Column(nullable = false) private Double v14;
    @Column(nullable = false) private Double v15;
    @Column(nullable = false) private Double v16;
    @Column(nullable = false) private Double v17;
    @Column(nullable = false) private Double v18;
    @Column(nullable = false) private Double v19;
    @Column(nullable = false) private Double v20;
    @Column(nullable = false) private Double v21;
    @Column(nullable = false) private Double v22;
    @Column(nullable = false) private Double v23;
    @Column(nullable = false) private Double v24;
    @Column(nullable = false) private Double v25;
    @Column(nullable = false) private Double v26;
    @Column(nullable = false) private Double v27;
    @Column(nullable = false) private Double v28;

    @NotNull(message = "Transaction amount is required")
    @DecimalMin(value = "0.0", message = "Amount must be non-negative")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @NotNull(message = "Fraud classification is required")
    @Column(nullable = false)
    private Integer fraudClass;

    // Normalized amount computed during feature engineering
    @Column
    private Double normalizedAmount;

    // Time-based anomaly flag
    @Column
    private Boolean timeAnomaly;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime processedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ==================== Getters & Setters ====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getTime() { return time; }
    public void setTime(Double time) { this.time = time; }

    public Double getV1() { return v1; }
    public void setV1(Double v1) { this.v1 = v1; }
    public Double getV2() { return v2; }
    public void setV2(Double v2) { this.v2 = v2; }
    public Double getV3() { return v3; }
    public void setV3(Double v3) { this.v3 = v3; }
    public Double getV4() { return v4; }
    public void setV4(Double v4) { this.v4 = v4; }
    public Double getV5() { return v5; }
    public void setV5(Double v5) { this.v5 = v5; }
    public Double getV6() { return v6; }
    public void setV6(Double v6) { this.v6 = v6; }
    public Double getV7() { return v7; }
    public void setV7(Double v7) { this.v7 = v7; }
    public Double getV8() { return v8; }
    public void setV8(Double v8) { this.v8 = v8; }
    public Double getV9() { return v9; }
    public void setV9(Double v9) { this.v9 = v9; }
    public Double getV10() { return v10; }
    public void setV10(Double v10) { this.v10 = v10; }
    public Double getV11() { return v11; }
    public void setV11(Double v11) { this.v11 = v11; }
    public Double getV12() { return v12; }
    public void setV12(Double v12) { this.v12 = v12; }
    public Double getV13() { return v13; }
    public void setV13(Double v13) { this.v13 = v13; }
    public Double getV14() { return v14; }
    public void setV14(Double v14) { this.v14 = v14; }
    public Double getV15() { return v15; }
    public void setV15(Double v15) { this.v15 = v15; }
    public Double getV16() { return v16; }
    public void setV16(Double v16) { this.v16 = v16; }
    public Double getV17() { return v17; }
    public void setV17(Double v17) { this.v17 = v17; }
    public Double getV18() { return v18; }
    public void setV18(Double v18) { this.v18 = v18; }
    public Double getV19() { return v19; }
    public void setV19(Double v19) { this.v19 = v19; }
    public Double getV20() { return v20; }
    public void setV20(Double v20) { this.v20 = v20; }
    public Double getV21() { return v21; }
    public void setV21(Double v21) { this.v21 = v21; }
    public Double getV22() { return v22; }
    public void setV22(Double v22) { this.v22 = v22; }
    public Double getV23() { return v23; }
    public void setV23(Double v23) { this.v23 = v23; }
    public Double getV24() { return v24; }
    public void setV24(Double v24) { this.v24 = v24; }
    public Double getV25() { return v25; }
    public void setV25(Double v25) { this.v25 = v25; }
    public Double getV26() { return v26; }
    public void setV26(Double v26) { this.v26 = v26; }
    public Double getV27() { return v27; }
    public void setV27(Double v27) { this.v27 = v27; }
    public Double getV28() { return v28; }
    public void setV28(Double v28) { this.v28 = v28; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Integer getFraudClass() { return fraudClass; }
    public void setFraudClass(Integer fraudClass) { this.fraudClass = fraudClass; }

    public Double getNormalizedAmount() { return normalizedAmount; }
    public void setNormalizedAmount(Double normalizedAmount) { this.normalizedAmount = normalizedAmount; }

    public Boolean getTimeAnomaly() { return timeAnomaly; }
    public void setTimeAnomaly(Boolean timeAnomaly) { this.timeAnomaly = timeAnomaly; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    @Override
    public String toString() {
        return "Transaction{id=" + id + ", time=" + time + ", amount=" + amount +
               ", fraudClass=" + fraudClass + ", createdAt=" + createdAt + "}";
    }
}
