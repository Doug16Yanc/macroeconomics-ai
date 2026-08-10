package com.example.macroeconomics_ai.infrastructure.bcb;

import com.example.macroeconomics_ai.domain.model.BcbSeriesRequest;
import com.example.macroeconomics_ai.domain.model.MonetaryIndicator;
import com.example.macroeconomics_ai.domain.port.MonetaryDataPort;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BcbObservationProcessor implements ItemProcessor<BcbSeriesRequest, List<MonetaryIndicator>> {

    private final MonetaryDataPort bcbMonetaryAdapter;

    public BcbObservationProcessor(@Qualifier("bcbMonetaryAdapter") MonetaryDataPort bcbMonetaryAdapter) {
        this.bcbMonetaryAdapter = bcbMonetaryAdapter;
    }

    @Override
    public List<MonetaryIndicator> process(BcbSeriesRequest request) {
        return bcbMonetaryAdapter.getObservations(request.seriesId(), request.observationStart());
    }
}