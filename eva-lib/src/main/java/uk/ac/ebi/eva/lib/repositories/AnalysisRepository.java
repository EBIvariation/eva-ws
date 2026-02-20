package uk.ac.ebi.eva.lib.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import uk.ac.ebi.eva.lib.entities.Analysis;

import java.util.List;

public interface AnalysisRepository extends JpaRepository<Analysis, String> {

    @Query("SELECT a.submission.submissionAccession, a.submission.date, SUM(f.fileSize) " +
           "FROM Analysis a JOIN a.files f " +
           "GROUP BY a.submission.submissionAccession, a.submission.date")
    List<Object[]> getSubmissionFileSizes();

}
