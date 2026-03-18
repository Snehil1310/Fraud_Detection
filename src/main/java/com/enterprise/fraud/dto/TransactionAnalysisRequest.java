package com.enterprise.fraud.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Request DTO for transaction analysis.
 * Contains all required fields from the Kaggle dataset for ML scoring.
 */
public class TransactionAnalysisRequest {

    @NotNull(message = "Time is required")
    private Double time;

    @NotNull(message = "V1 is required")
    private Double v1;
    @NotNull(message = "V2 is required")
    private Double v2;
    @NotNull(message = "V3 is required")
    private Double v3;
    @NotNull(message = "V4 is required")
    private Double v4;
    @NotNull(message = "V5 is required")
    private Double v5;
    @NotNull(message = "V6 is required")
    private Double v6;
    @NotNull(message = "V7 is required")
    private Double v7;
    @NotNull(message = "V8 is required")
    private Double v8;
    @NotNull(message = "V9 is required")
    private Double v9;
    @NotNull(message = "V10 is required")
    private Double v10;
    @NotNull(message = "V11 is required")
    private Double v11;
    @NotNull(message = "V12 is required")
    private Double v12;
    @NotNull(message = "V13 is required")
    private Double v13;
    @NotNull(message = "V14 is required")
    private Double v14;
    @NotNull(message = "V15 is required")
    private Double v15;
    @NotNull(message = "V16 is required")
    private Double v16;
    @NotNull(message = "V17 is required")
    private Double v17;
    @NotNull(message = "V18 is required")
    private Double v18;
    @NotNull(message = "V19 is required")
    private Double v19;
    @NotNull(message = "V20 is required")
    private Double v20;
    @NotNull(message = "V21 is required")
    private Double v21;
    @NotNull(message = "V22 is required")
    private Double v22;
    @NotNull(message = "V23 is required")
    private Double v23;
    @NotNull(message = "V24 is required")
    private Double v24;
    @NotNull(message = "V25 is required")
    private Double v25;
    @NotNull(message = "V26 is required")
    private Double v26;
    @NotNull(message = "V27 is required")
    private Double v27;
    @NotNull(message = "V28 is required")
    private Double v28;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", message = "Amount must be non-negative")
    private BigDecimal amount;

    // Getters & Setters
    public Double getTime() {
        return time;
    }

    public void setTime(Double time) {
        this.time = time;
    }

    public Double getV1() {
        return v1;
    }

    public void setV1(Double v1) {
        this.v1 = v1;
    }

    public Double getV2() {
        return v2;
    }

    public void setV2(Double v2) {
        this.v2 = v2;
    }

    public Double getV3() {
        return v3;
    }

    public void setV3(Double v3) {
        this.v3 = v3;
    }

    public Double getV4() {
        return v4;
    }

    public void setV4(Double v4) {
        this.v4 = v4;
    }

    public Double getV5() {
        return v5;
    }

    public void setV5(Double v5) {
        this.v5 = v5;
    }

    public Double getV6() {
        return v6;
    }

    public void setV6(Double v6) {
        this.v6 = v6;
    }

    public Double getV7() {
        return v7;
    }

    public void setV7(Double v7) {
        this.v7 = v7;
    }

    public Double getV8() {
        return v8;
    }

    public void setV8(Double v8) {
        this.v8 = v8;
    }

    public Double getV9() {
        return v9;
    }

    public void setV9(Double v9) {
        this.v9 = v9;
    }

    public Double getV10() {
        return v10;
    }

    public void setV10(Double v10) {
        this.v10 = v10;
    }

    public Double getV11() {
        return v11;
    }

    public void setV11(Double v11) {
        this.v11 = v11;
    }

    public Double getV12() {
        return v12;
    }

    public void setV12(Double v12) {
        this.v12 = v12;
    }

    public Double getV13() {
        return v13;
    }

    public void setV13(Double v13) {
        this.v13 = v13;
    }

    public Double getV14() {
        return v14;
    }

    public void setV14(Double v14) {
        this.v14 = v14;
    }

    public Double getV15() {
        return v15;
    }

    public void setV15(Double v15) {
        this.v15 = v15;
    }

    public Double getV16() {
        return v16;
    }

    public void setV16(Double v16) {
        this.v16 = v16;
    }

    public Double getV17() {
        return v17;
    }

    public void setV17(Double v17) {
        this.v17 = v17;
    }

    public Double getV18() {
        return v18;
    }

    public void setV18(Double v18) {
        this.v18 = v18;
    }

    public Double getV19() {
        return v19;
    }

    public void setV19(Double v19) {
        this.v19 = v19;
    }

    public Double getV20() {
        return v20;
    }

    public void setV20(Double v20) {
        this.v20 = v20;
    }

    public Double getV21() {
        return v21;
    }

    public void setV21(Double v21) {
        this.v21 = v21;
    }

    public Double getV22() {
        return v22;
    }

    public void setV22(Double v22) {
        this.v22 = v22;
    }

    public Double getV23() {
        return v23;
    }

    public void setV23(Double v23) {
        this.v23 = v23;
    }

    public Double getV24() {
        return v24;
    }

    public void setV24(Double v24) {
        this.v24 = v24;
    }

    public Double getV25() {
        return v25;
    }

    public void setV25(Double v25) {
        this.v25 = v25;
    }

    public Double getV26() {
        return v26;
    }

    public void setV26(Double v26) {
        this.v26 = v26;
    }

    public Double getV27() {
        return v27;
    }

    public void setV27(Double v27) {
        this.v27 = v27;
    }

    public Double getV28() {
        return v28;
    }

    public void setV28(Double v28) {
        this.v28 = v28;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
