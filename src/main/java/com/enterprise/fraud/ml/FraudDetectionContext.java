package com.enterprise.fraud.ml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Strategy Context that manages and switches between fraud detection
 * strategies.
 * Supports runtime strategy switching without downtime via the Strategy Design
 * Pattern.
 */
@Component
public class FraudDetectionContext {

    private static final Logger log = LoggerFactory.getLogger(FraudDetectionContext.class);

    private final Map<String, FraudDetectionStrategy> strategies = new ConcurrentHashMap<>();
    private volatile FraudDetectionStrategy activeStrategy;

    public FraudDetectionContext(List<FraudDetectionStrategy> strategyList,
            @Value("${fraud.detection.default-strategy:RULE_BASED}") String defaultStrategy) {
        // Register all available strategies
        for (FraudDetectionStrategy strategy : strategyList) {
            strategies.put(strategy.getStrategyName(), strategy);
            log.info("Registered fraud detection strategy: {} (ready: {})",
                    strategy.getStrategyName(), strategy.isReady());
        }

        // Set the default active strategy
        activeStrategy = strategies.getOrDefault(defaultStrategy, null);
        if (activeStrategy == null || !activeStrategy.isReady()) {
            // Fallback to the first ready strategy
            activeStrategy = strategies.values().stream()
                    .filter(FraudDetectionStrategy::isReady)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No fraud detection strategy is ready"));
        }

        log.info("Active fraud detection strategy: {}", activeStrategy.getStrategyName());
    }

    /**
     * Execute fraud prediction using the active strategy.
     */
    public FraudPrediction predict(TransactionFeatures features) {
        return activeStrategy.predict(features);
    }

    /**
     * Switch to a different strategy at runtime.
     *
     * @param strategyName the name of the strategy to switch to
     * @return true if the switch was successful
     */
    public boolean switchStrategy(String strategyName) {
        FraudDetectionStrategy newStrategy = strategies.get(strategyName);
        if (newStrategy == null) {
            log.warn("Strategy '{}' not found. Available: {}", strategyName, strategies.keySet());
            return false;
        }
        if (!newStrategy.isReady()) {
            log.warn("Strategy '{}' is not ready", strategyName);
            return false;
        }

        FraudDetectionStrategy oldStrategy = activeStrategy;
        activeStrategy = newStrategy;
        log.info("Switched strategy from {} to {}", oldStrategy.getStrategyName(), newStrategy.getStrategyName());
        return true;
    }

    /**
     * Get the currently active strategy name.
     */
    public String getActiveStrategyName() {
        return activeStrategy.getStrategyName();
    }

    /**
     * Get all registered strategy names and their readiness status.
     */
    public Map<String, Boolean> getAvailableStrategies() {
        Map<String, Boolean> available = new ConcurrentHashMap<>();
        strategies.forEach((name, strategy) -> available.put(name, strategy.isReady()));
        return available;
    }
}
