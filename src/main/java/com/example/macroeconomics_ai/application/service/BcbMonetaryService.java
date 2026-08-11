package com.example.macroeconomics_ai.application.service;

import com.example.macroeconomics_ai.domain.model.MonetaryIndicator;
import com.example.macroeconomics_ai.infrastructure.persistence.repository.BcbObservationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class BcbMonetaryService {

    private final BcbObservationRepository repository;

    public BcbMonetaryService(BcbObservationRepository repository) {
        this.repository = repository;
    }

    public List<MonetaryIndicator> getSeries(String seriesId, int monthsBack) {
        var start = LocalDate.now().minusMonths(monthsBack);
        return repository.findObservations(seriesId, start);
    }
}