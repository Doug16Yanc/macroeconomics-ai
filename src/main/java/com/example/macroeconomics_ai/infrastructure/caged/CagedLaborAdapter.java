package com.example.macroeconomics_ai.infrastructure.caged;

import com.example.macroeconomics_ai.domain.model.LaborMarketIndicator;
import com.example.macroeconomics_ai.domain.port.LaborMarketDataPort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * TODO: CAGED does not expose a simple public REST/JSON API like FRED or BCB/SGS.
 * Data access requires either raw monthly file ingestion from the Ministério do
 * Trabalho portal, or querying via basedosdados (BigQuery-based, requires GCP
 * credentials). Needs its own ingestion strategy — likely a separate Batch job
 * downloading and parsing bulk files, not a live REST call per request.
 */
@Component
public class CagedLaborAdapter implements LaborMarketDataPort {

    @Override
    public List<LaborMarketIndicator> getObservations(String indicator, String sector, String region, LocalDate start) {
        throw new UnsupportedOperationException("CAGED adapter not yet implemented — see class Javadoc.");
    }
}