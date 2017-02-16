/*
 * Copyright 2014-2017 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.eva.lib.metadata;

import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;
import org.opencb.opencga.storage.core.adaptors.StudyDBAdaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import uk.ac.ebi.eva.lib.models.VariantStudy;
import uk.ac.ebi.eva.lib.entity.DgvaStudyBrowser;
import uk.ac.ebi.eva.lib.repository.DgvaStudyBrowserRepository;
import uk.ac.ebi.eva.lib.utils.DgvaDBUtils;

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
