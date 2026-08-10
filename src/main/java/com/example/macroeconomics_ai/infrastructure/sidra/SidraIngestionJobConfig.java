package com.example.macroeconomics_ai.infrastructure.sidra;

import com.example.macroeconomics_ai.domain.model.sidra.SidraLaborIndicator;
import com.example.macroeconomics_ai.domain.model.sidra.SidraLaborQuery;
import com.example.macroeconomics_ai.domain.model.sidra.SidraSeriesRequest;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.List;

@Configuration
public class SidraIngestionJobConfig {


    @Bean
    public Step sidraIngestionStep(
            JobRepository jobRepository,
            SidraLaborReader sidraLaborReader,
            SidraLaborProcessor processor,
            SidraLaborWriter writer) {

        return new StepBuilder("sidraIngestionStep", jobRepository)
                .<SidraLaborQuery, List<SidraLaborIndicator>>chunk(1)
                .reader(sidraLaborReader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job sidraIngestionJob(
            JobRepository jobRepository,
            Step sidraIngestionStep) {

        return new JobBuilder("sidraIngestionJob", jobRepository)
                .start(sidraIngestionStep)
                .build();
    }
}