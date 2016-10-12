package uk.ac.ebi.variation.eva.lib.spring.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.ac.ebi.variation.eva.lib.spring.data.ResultClasses.FileFtpReference;
import uk.ac.ebi.variation.eva.lib.spring.data.entity.File;

import java.util.List;

/**
 * Created by jorizci on 03/10/16.
 */
public interface FileRepository extends JpaRepository<File, Long> {

    Long countByFileTypeIn(List<String> strings);

    //named query
    FileFtpReference getFileFtpReferenceByFilename(@Param("filename") String filename);

    //named query
    List<FileFtpReference> getFileFtpReferenceByNames(@Param("filenames") List<String> filenames);
}
