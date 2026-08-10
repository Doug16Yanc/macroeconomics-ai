package com.example.macroeconomics_ai.domain.port;

import com.example.macroeconomics_ai.domain.model.LaborMarketIndicator;

import java.time.LocalDate;
import java.util.List;

public interface LaborMarketDataPort {
    /**
     * @param indicator identifica a série/indicador na fonte (ex: "PAYEMS" no FRED,
     *                   ou uma categoria específica do CAGED).
     * @param sector    setor econômico (CNAE), null quando não aplicável (ex: FRED).
     * @param region    UF ou região, null quando não aplicável (ex: FRED).
     */
    List<LaborMarketIndicator> getObservations(String indicator, String sector, String region, LocalDate start);
}
