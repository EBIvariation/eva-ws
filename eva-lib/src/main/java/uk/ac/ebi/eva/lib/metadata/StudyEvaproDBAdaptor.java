package uk.ac.ebi.eva.lib.metadata;

import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;
import org.opencb.opencga.storage.core.adaptors.StudyDBAdaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import uk.ac.ebi.eva.lib.models.VariantStudy;
import uk.ac.ebi.eva.lib.entity.EvaStudyBrowser;
import uk.ac.ebi.eva.lib.repository.EvaStudyBrowserRepository;

import java.util.ArrayList;
import java.util.List;

import static uk.ac.ebi.eva.lib.utils.EvaproDbUtils.getSpeciesAndTypeFilters;


/**
 * Created by jorizci on 04/10/16.
 */
@Component
public class StudyEvaproDBAdaptor implements StudyDBAdaptor {

    @Autowired
    private EvaStudyBrowserRepository evaStudyBrowserRepository;

    @Override
    public QueryResult getAllStudies(QueryOptions queryOptions) {
        long start = System.currentTimeMillis();
        Specification filterSpecification = getSpeciesAndTypeFilters(queryOptions);
        List<EvaStudyBrowser> dgvaStudies = evaStudyBrowserRepository.findAll(filterSpecification);
        List<VariantStudy> variantstudies = new ArrayList<>();
        for (EvaStudyBrowser dgvaStudy : dgvaStudies) {
            if (dgvaStudy != null) {
                variantstudies.add(dgvaStudy.generateVariantStudy());
            }
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
    public QueryResult getStudyById(String s, QueryOptions queryOptions) {
        long start = System.currentTimeMillis();
        EvaStudyBrowser study = evaStudyBrowserRepository.findOne(s);
        List<VariantStudy> variantStudy = new ArrayList<>();
        if (study != null) {
            variantStudy.add(study.generateVariantStudy());
        }
        long end = System.currentTimeMillis();
        return new QueryResult(null, ((Long) (end - start)).intValue(), variantStudy.size(), variantStudy.size(), null, null, variantStudy);
    }

    @Override
    public boolean close() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
