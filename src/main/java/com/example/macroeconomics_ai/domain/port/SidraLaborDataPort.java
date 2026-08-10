package com.example.macroeconomics_ai.domain.port;

import com.example.macroeconomics_ai.domain.model.sidra.SidraLaborIndicator;
import com.example.macroeconomics_ai.domain.model.sidra.SidraLaborQuery;

import java.time.LocalDate;
import java.util.List;

public interface SidraLaborDataPort {

    List<SidraLaborIndicator> getObservations(
            SidraLaborQuery query
    );

    void saveAll(List<SidraLaborObservation> observations);
}