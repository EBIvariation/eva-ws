/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2014-2016 EMBL - European Bioinformatics Institute
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

package uk.ac.ebi.eva.server.ws;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.base.Splitter;
import org.opencb.biodata.models.feature.Genotype;
import org.opencb.biodata.models.variant.VariantSource;
import org.opencb.biodata.models.variant.VariantSourceEntry;
import org.opencb.biodata.models.variant.stats.VariantStats;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResponse;
import org.opencb.datastore.core.QueryResult;
import org.opencb.opencga.storage.core.variant.io.json.GenotypeJsonMixin;
import org.opencb.opencga.storage.core.variant.io.json.VariantSourceEntryJsonMixin;
import org.opencb.opencga.storage.core.variant.io.json.VariantSourceJsonMixin;
import org.opencb.opencga.storage.core.variant.io.json.VariantStatsJsonMixin;
import org.opencb.opencga.storage.core.variant.io.json.VariantStatsJsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import uk.ac.ebi.eva.lib.utils.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.utils.QueryUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EvaWSServer {

    protected final String version = "v1";

    @Autowired
    private QueryUtils queryUtils;

    protected static Logger logger = LoggerFactory.getLogger(EvaWSServer.class);

    @Bean
    public Jackson2ObjectMapperBuilder jacksonBuilder() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder()
                       .mixIn(VariantSourceEntry.class, VariantSourceEntryJsonMixin.class)
                       .mixIn(Genotype.class, GenotypeJsonMixin.class)
                       .mixIn(VariantStats.class, VariantStatsJsonMixin.class)
                       .mixIn(VariantSource.class, VariantSourceJsonMixin.class)
                       .serializationInclusion(Include.NON_NULL);
        
        SimpleModule module = new SimpleModule();
        module.addSerializer(VariantStats.class, new VariantStatsJsonSerializer());
        builder.modules(module);
        
        return builder;
    }
    
    public EvaWSServer() { }

    protected QueryOptions getQueryOptions() {
        return queryUtils.getQueryOptions();
    }

    protected void initializeQuery() {
        queryUtils.initializeQuery();
    }

    protected <T> QueryResponse<T> setQueryResponse(T obj) {
        return queryUtils.setQueryResponse(obj, version);
    }

    protected <T> QueryResponse<T> setErrorQueryResponse(String message) {
        return queryUtils.setErrorQueryResponse(message, version);
    }

    protected <T> QueryResult<T> buildQueryResult(List<T> results) {
        return buildQueryResult(results, results.size());
    }

    protected <T> QueryResult<T> buildQueryResult(List<T> results, long numTotalResults) {
        return queryUtils.buildQueryResult(results, numTotalResults);
    }
}
