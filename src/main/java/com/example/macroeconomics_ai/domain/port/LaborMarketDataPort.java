package com.example.macroeconomics_ai.domain.port;

import com.example.macroeconomics_ai.domain.model.LaborMarketIndicator;

import java.time.LocalDate;
import java.util.List;

public interface LaborMarketDataPort {
    List<LaborMarketIndicator> getObservations(String sector, String region, LocalDate start);
}
