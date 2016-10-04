package uk.ac.ebi.variation.eva.lib.spring.data.metadata;

import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;
import org.opencb.opencga.storage.core.adaptors.StudyDBAdaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import uk.ac.ebi.variation.eva.lib.datastore.EvaproUtils;
import uk.ac.ebi.variation.eva.lib.models.VariantStudy;
import uk.ac.ebi.variation.eva.lib.spring.data.entity.DgvaStudyBrowser;
import uk.ac.ebi.variation.eva.lib.spring.data.repository.DGvaStudyBrowserRepository;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static uk.ac.ebi.variation.eva.lib.spring.data.utils.DgvaDBUtils.getSpeciesAndTypeFilters;

/**
 * Created by jorizci on 03/10/16.
 */
public class SpringStudyDgvaDBAdaptor implements StudyDBAdaptor {

    @Autowired
    private DGvaStudyBrowserRepository dGvaStudyBrowserRepository;

    @Override
    public QueryResult getAllStudies(QueryOptions queryOptions) {
        long start = System.currentTimeMillis();
        Specification filterSpecification = getSpeciesAndTypeFilters(queryOptions);
        List<DgvaStudyBrowser> dgvaStudies = dGvaStudyBrowserRepository.findAll(filterSpecification);
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
        Specification filterSpecification = getSpeciesAndTypeFilters(queryOptions);
        DgvaStudyBrowser dgvaStudy = dGvaStudyBrowserRepository.getOne(studyId);
        List<VariantStudy> variantStudy = new ArrayList<>();
        if(dgvaStudy!=null){
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
