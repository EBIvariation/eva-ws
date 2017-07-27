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

package uk.ac.ebi.dgva.server.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ebi.eva.lib.utils.QueryOptions;
import uk.ac.ebi.eva.lib.utils.QueryResponse;
import uk.ac.ebi.eva.lib.utils.QueryUtils;

public class DgvaWSServer {

    protected final String version = "v1";

    @Autowired
    private QueryUtils queryUtils;

    protected static Logger logger = LoggerFactory.getLogger(DgvaWSServer.class);
    
    public DgvaWSServer() { }

    protected QueryOptions getQueryOptions() {
        return queryUtils.getQueryOptions();
    }

    protected void initializeQuery() {
        queryUtils.initializeQuery();
    }

    protected <T> QueryResponse<T> setQueryResponse(T obj) {
        return queryUtils.setQueryResponse(obj, version);
    }
}
