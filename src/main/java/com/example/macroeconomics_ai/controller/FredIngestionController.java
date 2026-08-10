package com.example.macroeconomics_ai.controller;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/jobs")
public class FredIngestionController {

    private final JobOperator jobOperator;
    private final JobRepository jobRepository;
    private final Job fredIngestionJob;

    public FredIngestionController(JobOperator jobOperator, JobRepository jobRepository, Job fredIngestionJob) {
        this.jobOperator = jobOperator;
        this.jobRepository = jobRepository;
        this.fredIngestionJob = fredIngestionJob;
    }

    @PostMapping("/fred-ingestion")
    public ResponseEntity<Map<String, Object>> trigger() throws Exception {

        var params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        var execution = jobOperator.start(
                fredIngestionJob,
                params
        );

        return ResponseEntity
                .accepted()
                .body(Map.of(
                        "jobExecutionId", execution.getId(),
                        "status", execution.getStatus().toString()
                ));
    }

    @GetMapping("/fred-ingestion/{executionId}")
    public ResponseEntity<JobStatusResponse> status(
            @PathVariable Long executionId) {

        var execution = jobRepository.getJobExecution(executionId);

        if (execution == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                new JobStatusResponse(
                        execution.getId(),
                        execution.getStatus().toString(),
                        execution.getStartTime(),
                        execution.getEndTime()
                )
        );
    }

    public record JobStatusResponse(
            Long jobExecutionId,
            String status,
            java.time.LocalDateTime startTime,
            java.time.LocalDateTime endTime
    ) {
    }
}

