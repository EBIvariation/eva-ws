package uk.ac.ebi.eva.countstats.service;

import org.springframework.stereotype.Service;
import uk.ac.ebi.eva.countstats.model.Count;
import uk.ac.ebi.eva.countstats.repository.CountRepository;

import java.util.List;

@Service
public class CountService {
    private CountRepository countRepository;

    public CountService(CountRepository countRepository) {
        this.countRepository = countRepository;
    }

    public Count saveCount(Count count) {
        return countRepository.save(count);
    }

    public Iterable<Count> saveAllCount(List<Count> countList) {
        return countRepository.saveAll(countList);
    }

    public Iterable<Count> getAllCounts() {
        return countRepository.findAll();
    }

    public Long getCountForProcess(String process, String study) {
        return countRepository.getCountForProcess(process, study);
    }
}