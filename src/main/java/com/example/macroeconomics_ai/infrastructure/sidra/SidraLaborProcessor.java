package com.example.macroeconomics_ai.infrastructure.sidra;

import com.example.macroeconomics_ai.domain.model.sidra.SidraLaborIndicator;
import com.example.macroeconomics_ai.domain.model.sidra.SidraLaborQuery;
import com.example.macroeconomics_ai.domain.port.SidraLaborDataPort;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SidraLaborProcessor
        implements ItemProcessor<SidraLaborQuery, List<SidraLaborIndicator>> {

    private final SidraLaborDataPort sidraLaborDataPort;

    public SidraLaborProcessor(SidraLaborDataPort sidraLaborDataPort) {
        this.sidraLaborDataPort = sidraLaborDataPort;
    }

    @Override
    public List<SidraLaborIndicator> process(SidraLaborQuery query) {
        return sidraLaborDataPort.getObservations(query);
    }
}