package com.example.macroeconomics_ai.infrastructure.web.controller;

import com.example.macroeconomics_ai.infrastructure.web.dto.JobStatusResponse;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Map;

@RestController
@RequestMapping("/jobs")
public class SidraIngestionController {

    private final JobOperator jobOperator;
    private final JobRepository jobRepository;
    private final Job sidraIngestionJob;

    public SidraIngestionController(
            JobOperator jobOperator,
            JobRepository jobRepository,
            @Qualifier("sidraIngestionJob") Job sidraIngestionJob) {

        this.jobOperator = jobOperator;
        this.jobRepository = jobRepository;
        this.sidraIngestionJob = sidraIngestionJob;
    }

    @PostMapping("/sidra-ingestion")
    public ResponseEntity<Map<String, Object>> trigger() throws Exception {

        var params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        var execution = jobOperator.start(sidraIngestionJob, params);

        return ResponseEntity
                .accepted()
                .body(Map.of(
                        "jobExecutionId", execution.getId(),
                        "status", execution.getStatus().toString()
                ));
    }

    @GetMapping("/sidra-ingestion/{executionId}")
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
}
