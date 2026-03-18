package com.enterprise.fraud.ml;

/**
 * Encapsulates the features extracted from a transaction for ML scoring.
 * Maps the Kaggle dataset's anonymized variables (V1-V28) plus engineered
 * features.
 */
public class TransactionFeatures {

    private final double[] pcaFeatures; // V1-V28
    private final double normalizedAmount;
    private final double time;
    private final boolean timeAnomaly;

    public TransactionFeatures(double[] pcaFeatures, double normalizedAmount, double time, boolean timeAnomaly) {
        if (pcaFeatures == null || pcaFeatures.length != 28) {
            throw new IllegalArgumentException("PCA features must contain exactly 28 values (V1-V28)");
        }
        this.pcaFeatures = pcaFeatures;
        this.normalizedAmount = normalizedAmount;
        this.time = time;
        this.timeAnomaly = timeAnomaly;
    }

    public double[] getPcaFeatures() {
        return pcaFeatures;
    }

    public double getNormalizedAmount() {
        return normalizedAmount;
    }

    public double getTime() {
        return time;
    }

    public boolean isTimeAnomaly() {
        return timeAnomaly;
    }

    /**
     * Convert to a flat feature array suitable for Weka Instance creation.
     * Order: V1, V2, ..., V28, normalizedAmount, time
     *
     * @return double array of all 30 features
     */
    public double[] toFeatureArray() {
        double[] features = new double[30];
        System.arraycopy(pcaFeatures, 0, features, 0, 28);
        features[28] = normalizedAmount;
        features[29] = time;
        return features;
    }
}
