package com.example.macroeconomics_ai.domain.port;

import com.example.macroeconomics_ai.domain.model.fred.FredLaborIndicator;

import java.time.LocalDate;
import java.util.List;

public interface FredLaborDataPort {
    List<FredLaborIndicator> getObservations(String indicator, String sector, String region, LocalDate start);
}
