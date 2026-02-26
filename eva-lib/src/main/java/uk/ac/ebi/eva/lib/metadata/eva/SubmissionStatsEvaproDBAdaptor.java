package uk.ac.ebi.eva.lib.metadata.eva;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.ac.ebi.eva.lib.repositories.AnalysisRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component
public class SubmissionStatsEvaproDBAdaptor {

    @Autowired
    private AnalysisRepository analysisRepository;

    public Map<String, Long> getCountByMonth() {
        List<Object[]> rows = analysisRepository.getSubmissionFileSizes();
        return rows.stream()
                .filter(row -> row[1] != null)
                .collect(Collectors.groupingBy(
                        row -> ((LocalDate) row[1]).format(DateTimeFormatter.ofPattern("yyyyMM")),
                        () -> new TreeMap<>(Comparator.reverseOrder()),
                        Collectors.counting()
                ));
    }

    public Map<String, Long> getBytesByMonth() {
        List<Object[]> rows = analysisRepository.getSubmissionFileSizes();
        return rows.stream()
                .filter(row -> row[1] != null)
                .collect(Collectors.groupingBy(
                        row -> ((LocalDate) row[1]).format(DateTimeFormatter.ofPattern("yyyyMM")),
                        () -> new TreeMap<>(Comparator.reverseOrder()),
                        Collectors.summingLong(row -> row[2] != null ? (Long) row[2] : 0L)
                ));
    }
}
