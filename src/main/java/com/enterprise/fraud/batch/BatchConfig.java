package com.enterprise.fraud.batch;

import com.enterprise.fraud.dto.TransactionCsvDto;
import com.enterprise.fraud.entity.Transaction;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring Batch configuration for bulk CSV ingestion.
 * <p>
 * Pipeline Architecture:
 * CSV File → FlatFileItemReader → TransactionItemProcessor (Feature
 * Engineering) → JpaItemWriter → MySQL
 * <p>
 * Memory-safe chunk processing with configurable chunk size (default: 1000).
 * Processes the 284,807-row Kaggle dataset in manageable batches.
 */
@Configuration
public class BatchConfig {

    private static final Logger log = LoggerFactory.getLogger(BatchConfig.class);

    @Value("${fraud.detection.batch.chunk-size:1000}")
    private int chunkSize;

    @Value("${fraud.detection.batch.csv-path:classpath:data/creditcard.csv}")
    private Resource csvResource;

    /**
     * FlatFileItemReader configured to read the Kaggle CSV.
     * Maps CSV columns to TransactionCsvDto fields.
     */
    @Bean
    public FlatFileItemReader<TransactionCsvDto> csvReader() {
        BeanWrapperFieldSetMapper<TransactionCsvDto> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(TransactionCsvDto.class);

        return new FlatFileItemReaderBuilder<TransactionCsvDto>()
                .name("transactionCsvReader")
                .resource(csvResource)
                .delimited()
                .names("time", "v1", "v2", "v3", "v4", "v5", "v6", "v7", "v8", "v9",
                        "v10", "v11", "v12", "v13", "v14", "v15", "v16", "v17", "v18", "v19",
                        "v20", "v21", "v22", "v23", "v24", "v25", "v26", "v27", "v28",
                        "amount", "fraudClass")
                .linesToSkip(1) // Skip CSV header row
                .fieldSetMapper(fieldSetMapper)
                .build();
    }

    /**
     * JpaItemWriter to persist Transaction entities to MySQL via JPA.
     */
    @Bean
    public JpaItemWriter<Transaction> jpaWriter(EntityManagerFactory entityManagerFactory) {
        return new JpaItemWriterBuilder<Transaction>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    /**
     * Batch Step: Read CSV → Process (Feature Engineering) → Write to DB.
     * Uses chunk-oriented processing for memory safety.
     */
    @Bean
    public Step csvIngestionStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<TransactionCsvDto> reader,
            TransactionItemProcessor processor,
            JpaItemWriter<Transaction> writer) {
        log.info("Configuring CSV ingestion step with chunk size: {}", chunkSize);

        return new StepBuilder("csvIngestionStep", jobRepository)
                .<TransactionCsvDto, Transaction>chunk(chunkSize, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skipLimit(100) // Allow up to 100 malformed rows
                .skip(Exception.class)
                .build();
    }

    /**
     * Batch Job: Orchestrates the CSV ingestion pipeline.
     * Uses RunIdIncrementer to allow re-running the same job.
     */
    @Bean
    public Job csvIngestionJob(JobRepository jobRepository, Step csvIngestionStep) {
        return new JobBuilder("csvIngestionJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(csvIngestionStep)
                .end()
                .build();
    }
}
