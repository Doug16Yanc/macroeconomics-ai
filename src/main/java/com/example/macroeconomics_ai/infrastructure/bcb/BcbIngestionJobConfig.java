package com.example.macroeconomics_ai.infrastructure.bcb;

import com.example.macroeconomics_ai.domain.model.bcb.BcbSeriesRequest;
import com.example.macroeconomics_ai.domain.model.MonetaryIndicator;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.List;

@Configuration
public class BcbIngestionJobConfig {

    @Bean
    public ListItemReader<BcbSeriesRequest> bcbSeriesReader(
            @Value("${bcb.series.selic}") String selicSeriesId,
            @Value("${bcb.series.ipca}") String ipcaSeriesId,
            @Value("${bcb.series.exchange-rate}") String exchangeRateSeriesId) {

        return new ListItemReader<>(List.of(
                new BcbSeriesRequest(
                        selicSeriesId,
                        LocalDate.now().minusMonths(6)
                ),
                new BcbSeriesRequest(
                        ipcaSeriesId,
                        LocalDate.now().minusYears(1)
                ),
                new BcbSeriesRequest(
                        exchangeRateSeriesId,
                        LocalDate.now().minusMonths(6)
                )
        ));
    }

    @Bean
    public Step bcbIngestionStep(
            JobRepository jobRepository,
            ListItemReader<BcbSeriesRequest> bcbSeriesReader,
            BcbObservationProcessor processor,
            BcbObservationWriter writer) {

        return new StepBuilder("bcbIngestionStep", jobRepository)
                .<BcbSeriesRequest, List<MonetaryIndicator>>chunk(2)
                .reader(bcbSeriesReader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job bcbIngestionJob(
            JobRepository jobRepository,
            Step bcbIngestionStep) {

        return new JobBuilder("bcbIngestionJob", jobRepository)
                .start(bcbIngestionStep)
                .build();
    }
}

