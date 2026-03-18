package com.enterprise.fraud.repository;

import com.enterprise.fraud.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Spring Data JPA repository for Transaction entities.
 * Provides custom query methods for fraud analysis and reporting.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Find all transactions classified as fraudulent.
     */
    List<Transaction> findByFraudClass(Integer fraudClass);

    /**
     * Find transactions by fraud class with pagination.
     */
    Page<Transaction> findByFraudClass(Integer fraudClass, Pageable pageable);

    /**
     * Find transactions above a specified amount.
     */
    List<Transaction> findByAmountGreaterThan(BigDecimal amount);

    /**
     * Count total fraudulent transactions.
     */
    long countByFraudClass(Integer fraudClass);

    /**
     * Find transactions flagged with time anomalies.
     */
    List<Transaction> findByTimeAnomalyTrue();

    /**
     * Get the average transaction amount.
     */
    @Query("SELECT AVG(t.amount) FROM Transaction t")
    BigDecimal findAverageAmount();

    /**
     * Get the maximum transaction amount.
     */
    @Query("SELECT MAX(t.amount) FROM Transaction t")
    BigDecimal findMaxAmount();

    /**
     * Find transactions within an amount range.
     */
    @Query("SELECT t FROM Transaction t WHERE t.amount BETWEEN :minAmount AND :maxAmount")
    List<Transaction> findByAmountRange(@Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount);

    /**
     * Get fraud rate as a percentage.
     */
    @Query("SELECT (COUNT(CASE WHEN t.fraudClass = 1 THEN 1 END) * 100.0 / COUNT(t)) FROM Transaction t")
    Double calculateFraudRate();
}
