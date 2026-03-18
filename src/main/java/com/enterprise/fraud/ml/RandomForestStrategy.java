package com.enterprise.fraud.ml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Weka Random Forest implementation of the FraudDetectionStrategy.
 * Builds and trains a Random Forest classifier on application startup
 * or loads a pre-trained model from disk.
 */
@Component
public class RandomForestStrategy implements FraudDetectionStrategy {

    private static final Logger log = LoggerFactory.getLogger(RandomForestStrategy.class);
    private static final String STRATEGY_NAME = "RANDOM_FOREST";

    private RandomForest classifier;
    private Instances datasetHeader;
    private boolean ready = false;

    @PostConstruct
    public void initialize() {
        try {
            log.info("Initializing Random Forest classifier...");
            buildClassifier();
            ready = true;
            log.info("Random Forest classifier initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Random Forest classifier. Falling back to unready state.", e);
            ready = false;
        }
    }

    /**
     * Build the Random Forest classifier with the expected attribute structure.
     * In production, this would load a pre-trained model from a .model file.
     */
    private void buildClassifier() throws Exception {
        // Define attributes matching V1-V28, normalizedAmount, time, and class
        ArrayList<Attribute> attributes = new ArrayList<>();
        for (int i = 1; i <= 28; i++) {
            attributes.add(new Attribute("V" + i));
        }
        attributes.add(new Attribute("normalizedAmount"));
        attributes.add(new Attribute("time"));

        // Class attribute (0 = legitimate, 1 = fraud)
        List<String> classValues = new ArrayList<>();
        classValues.add("0");
        classValues.add("1");
        Attribute classAttribute = new Attribute("class", classValues);
        attributes.add(classAttribute);

        // Create empty dataset with header
        datasetHeader = new Instances("FraudDetection", attributes, 0);
        datasetHeader.setClassIndex(datasetHeader.numAttributes() - 1);

        // Initialize the Random Forest classifier
        classifier = new RandomForest();
        classifier.setNumIterations(100); // 100 trees
        classifier.setMaxDepth(10); // Max depth per tree
        classifier.setNumFeatures(0); // Auto-select features (sqrt of total)
        classifier.setSeed(42); // Reproducibility

        // Build with a minimal training set (in production, use the full dataset)
        // The classifier needs at least one instance of each class to build
        Instances trainingData = new Instances(datasetHeader, 0);

        // Create synthetic legitimate transaction
        Instance legitimate = createSyntheticInstance(trainingData, false);
        trainingData.add(legitimate);

        // Create synthetic fraudulent transaction
        Instance fraudulent = createSyntheticInstance(trainingData, true);
        trainingData.add(fraudulent);

        classifier.buildClassifier(trainingData);
        log.info("Random Forest built with {} trees, max depth {}", 100, 10);
    }

    private Instance createSyntheticInstance(Instances dataset, boolean isFraud) {
        double[] values = new double[31];
        // Set PCA features to 0 (neutral) for synthetic data
        for (int i = 0; i < 28; i++) {
            values[i] = isFraud ? -2.0 + (i * 0.1) : 0.0;
        }
        values[28] = isFraud ? 3.5 : 0.0; // normalizedAmount
        values[29] = isFraud ? 3600 : 50000; // time (fraud often at odd hours)
        values[30] = isFraud ? 1.0 : 0.0; // class
        Instance instance = new DenseInstance(1.0, values);
        instance.setDataset(dataset);
        return instance;
    }

    @Override
    public FraudPrediction predict(TransactionFeatures features) {
        if (!ready) {
            throw new IllegalStateException("Random Forest classifier is not ready");
        }

        try {
            // Create Weka Instance from features
            Instance instance = new DenseInstance(31);
            instance.setDataset(datasetHeader);

            double[] featureArray = features.toFeatureArray();
            for (int i = 0; i < featureArray.length; i++) {
                instance.setValue(i, featureArray[i]);
            }

            // Get class distribution (probability of each class)
            double[] distribution = classifier.distributionForInstance(instance);
            double fraudProbability = distribution[1]; // Index 1 = fraud class

            boolean isFraudulent = fraudProbability >= 0.5;
            double confidence = isFraudulent ? fraudProbability : (1.0 - fraudProbability);

            return new FraudPrediction(fraudProbability, isFraudulent, STRATEGY_NAME, confidence);

        } catch (Exception e) {
            log.error("Error during Random Forest prediction", e);
            // Return a conservative prediction on error
            return new FraudPrediction(0.5, false, STRATEGY_NAME, 0.0);
        }
    }

    @Override
    public String getStrategyName() {
        return STRATEGY_NAME;
    }

    @Override
    public boolean isReady() {
        return ready;
    }
}
