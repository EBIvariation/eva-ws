package uk.ac.ebi.eva.countstats.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import uk.ac.ebi.eva.countstats.model.Count;

public interface CountRepository extends CrudRepository<Count, Long> {
    @Query(value = "SELECT sum(cs.count) FROM count_stats cs WHERE cs.process=?1 and cs.identifier->>'study'=?2", nativeQuery = true)
    Long getCountForProcess(String process, String study);
}
