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
package uk.ac.ebi.eva.lib.utils;

import java.util.List;

public class QueryResponse<T>{

    private static final long serialVersionUID = -2978952531219554024L;

    private int time;
    private String apiVersion;
    private String warning;
    private String error;

    private QueryOptions queryOptions;
    private List<T> response;

    public QueryResponse() {
        this(null, null);
    }

    public QueryResponse(QueryOptions queryOptions, List<T> response) {
        this(queryOptions, response, null, null, -1);
    }

    public QueryResponse(QueryOptions queryOptions, List<T> response, String version, String species, int time) {
        this.apiVersion = "v2";
        this.warning = "";
        this.error = "";
        this.queryOptions = queryOptions;
        this.response = response;
        this.time = time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public QueryOptions getQueryOptions() {
        return queryOptions;
    }

    public void setQueryOptions(QueryOptions queryOptions) {
        this.queryOptions = queryOptions;
    }

    public List<T> getResponse() {
        return response;
    }

    public void setResponse(List<T> response) {
        this.response = response;
    }
}