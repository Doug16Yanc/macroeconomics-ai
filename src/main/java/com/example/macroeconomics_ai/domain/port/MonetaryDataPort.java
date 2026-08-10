package com.example.macroeconomics_ai.domain.port;

import com.example.macroeconomics_ai.domain.model.MonetaryIndicator;

import java.time.LocalDate;
import java.util.List;

public interface MonetaryDataPort {
    List<MonetaryIndicator> getObservations(String seriesId, LocalDate start);
}
