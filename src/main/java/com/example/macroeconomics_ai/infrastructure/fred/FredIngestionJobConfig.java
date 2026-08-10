package com.example.macroeconomics_ai.infrastructure.fred;

import com.example.macroeconomics_ai.domain.model.fred.FredObservation;
import com.example.macroeconomics_ai.domain.model.fred.FredSeriesRequest;
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
public class FredIngestionJobConfig {

    @Bean
    public ListItemReader<FredSeriesRequest> fredSeriesReader() {
        return new ListItemReader<>(List.of(
                new FredSeriesRequest("UNRATE", null, LocalDate.now().minusYears(1)),
                new FredSeriesRequest("ICSA", null, LocalDate.now().minusMonths(6)),
                new FredSeriesRequest("JTSJOL", null, LocalDate.now().minusYears(1)),
                new FredSeriesRequest("PAYEMS", "chg", LocalDate.now().minusYears(1))
        ));
    }

    @Bean
    public Step fredIngestionStep(
            JobRepository jobRepository,
            ListItemReader<FredSeriesRequest> fredSeriesReader,
            FredObservationProcessor processor,
            FredObservationWriter writer) {

        return new StepBuilder("fredIngestionStep", jobRepository)
                .<FredSeriesRequest, List<FredObservation>>chunk(2)
                .reader(fredSeriesReader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job fredIngestionJob(JobRepository jobRepository, Step fredIngestionStep) {
        return new JobBuilder("fredIngestionJob", jobRepository)
                .start(fredIngestionStep)
                .build();
    }
}