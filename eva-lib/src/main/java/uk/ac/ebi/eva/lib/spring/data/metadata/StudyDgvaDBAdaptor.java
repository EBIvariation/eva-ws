package uk.ac.ebi.eva.lib.spring.data.metadata;

import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;
import org.opencb.opencga.storage.core.adaptors.StudyDBAdaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import uk.ac.ebi.eva.lib.models.VariantStudy;
import uk.ac.ebi.eva.lib.spring.data.entity.DgvaStudyBrowser;
import uk.ac.ebi.eva.lib.spring.data.repository.DgvaStudyBrowserRepository;
import uk.ac.ebi.eva.lib.spring.data.utils.DgvaDBUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jorizci on 03/10/16.
 */
@Component
public class StudyDgvaDBAdaptor implements StudyDBAdaptor {

    @Autowired
    private DgvaStudyBrowserRepository dgvaStudyBrowserRepository;

    @Override
    public QueryResult getAllStudies(QueryOptions queryOptions) {
        long start = System.currentTimeMillis();
        Specification filterSpecification = DgvaDBUtils.getSpeciesAndTypeFilters(queryOptions);
        List<DgvaStudyBrowser> dgvaStudies = dgvaStudyBrowserRepository.findAll(filterSpecification);
        List<VariantStudy> variantstudies = new ArrayList<>();
        for (DgvaStudyBrowser dgvaStudy : dgvaStudies) {
            variantstudies.add(dgvaStudy.generateVariantStudy());
        }
        long end = System.currentTimeMillis();
        return new QueryResult(null, ((Long) (end - start)).intValue(), variantstudies.size(), variantstudies.size(), null, null, variantstudies);
    }


    @Override
    public QueryResult listStudies() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public QueryResult findStudyNameOrStudyId(String s, QueryOptions queryOptions) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public QueryResult getStudyById(String studyId, QueryOptions queryOptions) {
        long start = System.currentTimeMillis();
        Specification filterSpecification = DgvaDBUtils.getSpeciesAndTypeFilters(queryOptions);
        DgvaStudyBrowser dgvaStudy = dgvaStudyBrowserRepository.getOne(studyId);
        List<VariantStudy> variantStudy = new ArrayList<>();
        if (dgvaStudy != null) {
            variantStudy.add(dgvaStudy.generateVariantStudy());
        }
        long end = System.currentTimeMillis();
        return new QueryResult(null, ((Long) (end - start)).intValue(), variantStudy.size(), variantStudy.size(), null, null, variantStudy);
    }

    @Override
    public boolean close() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
