/*
 * Copyright 2017 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.eva.lib.metadata.eva;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import uk.ac.ebi.eva.lib.metadata.StudyDBAdaptor;
import uk.ac.ebi.eva.lib.models.VariantStudy;
import uk.ac.ebi.eva.lib.entities.EvaStudyBrowser;
import uk.ac.ebi.eva.lib.utils.QueryOptions;
import uk.ac.ebi.eva.lib.utils.QueryResult;
import uk.ac.ebi.eva.lib.repositories.EvaStudyBrowserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.ac.ebi.eva.lib.eva_utils.EvaproDbUtils.getSpeciesAndTypeFilters;


@Component
public class StudyEvaproDBAdaptor implements StudyDBAdaptor {
    protected static Logger logger = LoggerFactory.getLogger(StudyEvaproDBAdaptor.class);

    @Autowired
    private EvaStudyBrowserRepository evaStudyBrowserRepository;

    @Override
    public QueryResult getAllStudies(QueryOptions queryOptions) {
        long start = System.currentTimeMillis();
        Specification filterSpecification = getSpeciesAndTypeFilters(queryOptions);
        List<EvaStudyBrowser> evaStudies = evaStudyBrowserRepository.findAll(filterSpecification);
        List<VariantStudy> variantstudies = new ArrayList<>();
        for (EvaStudyBrowser evaStudy : evaStudies) {
            if (evaStudy != null) {
                try {
                    variantstudies.add(evaStudy.generateVariantStudy());
                } catch (Exception ex) {
                    logger.error("EVA_Data_Error: Exception while generating variant study for {}. Exception {}", evaStudy, ex);
                }
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
        Optional<EvaStudyBrowser> study = evaStudyBrowserRepository.findById(s);
        List<VariantStudy> variantStudy = new ArrayList<>();
        if (study.isPresent()) {
            variantStudy.add(study.get().generateVariantStudy());
        }
        long end = System.currentTimeMillis();
        return new QueryResult(null, ((Long) (end - start)).intValue(), variantStudy.size(), variantStudy.size(), null, null, variantStudy);
    }

    @Override
    public boolean close() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
