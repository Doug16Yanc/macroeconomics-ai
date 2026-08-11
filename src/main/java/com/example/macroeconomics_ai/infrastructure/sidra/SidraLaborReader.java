package com.example.macroeconomics_ai.infrastructure.sidra;

import com.example.macroeconomics_ai.domain.model.sidra.SidraLaborQuery;
import org.springframework.batch.infrastructure.item.support.ListItemReader;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class SidraLaborReader extends ListItemReader<SidraLaborQuery> {

    public SidraLaborReader() {
        super(List.of(
                new SidraLaborQuery(
                        "5434",
                        "4090",
                        "888",
                        "all",
                        null,
                        LocalDate.now().minusYears(15)
                ),
                new SidraLaborQuery(
                        "5434",
                        "4108",
                        "888",
                        "all",
                        null,
                        LocalDate.now().minusYears(15)
                )
        ));
    }
}