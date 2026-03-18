package com.enterprise.fraud.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

/**
 * REST controller for triggering the Spring Batch CSV ingestion job.
 */
@RestController
@RequestMapping("/api/v1/batch")
@Tag(name = "Batch Operations", description = "Endpoints for managing data ingestion batch jobs")
public class BatchJobController {

    private static final Logger log = LoggerFactory.getLogger(BatchJobController.class);

    private final JobLauncher jobLauncher;
    private final Job csvIngestionJob;

    public BatchJobController(JobLauncher jobLauncher, Job csvIngestionJob) {
        this.jobLauncher = jobLauncher;
        this.csvIngestionJob = csvIngestionJob;
    }

    @PostMapping("/ingest")
    @Operation(summary = "Trigger CSV ingestion", description = "Starts the Spring Batch job to ingest the Kaggle CSV dataset into the database")
    public ResponseEntity<Map<String, String>> triggerIngestion() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("startTime", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(csvIngestionJob, params);

            log.info("CSV ingestion job triggered successfully");
            return ResponseEntity.ok(Map.of(
                    "status", "STARTED",
                    "message", "CSV ingestion job has been triggered successfully"));
        } catch (Exception e) {
            log.error("Failed to trigger CSV ingestion job", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "FAILED",
                    "message", "Failed to start ingestion: " + e.getMessage()));
        }
    }
}
