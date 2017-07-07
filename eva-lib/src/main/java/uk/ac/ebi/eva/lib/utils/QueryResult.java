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

import java.util.ArrayList;
import java.util.List;

public class QueryResult<T> {

    private String id;
    @Deprecated
    private int time;
    private int dbTime;
    private int numResults;
    private long numTotalResults;
    private String warningMsg;
    private String errorMsg;
    @Deprecated
    private String featureType;

    private String resultType;
    public Class<T> clazz;
    private List<T> result;

    public QueryResult() {
        this("", -1, -1, -1, "", "", new ArrayList<T>());
    }

    public QueryResult(String id) {
        this(id, -1, -1, -1, "", "", new ArrayList<T>());
    }

    public QueryResult(String id, int dbTime, int numResults, long numTotalResults, String warningMsg, String errorMsg, List<T> result) {
        this.id = id;
        this.dbTime = dbTime;
        this.numResults = numResults;
        this.numTotalResults = numTotalResults;
        this.warningMsg = warningMsg;
        this.errorMsg = errorMsg;
        this.resultType = result.size() > 0 ? result.get(0).getClass().getCanonicalName() : "";
        this.result = result;
    }

    public T first() {
        if(result != null && result.size() > 0) {
            return result.get(0);
        }
        return null;
    }


    @Override
    public String toString() {
        return "QueryResult{\n" +
                "id='" + id + '\'' + "\n" +
                ", dbTime=" + dbTime + "\n" +
                ", numResults=" + numResults + "\n" +
                ", warningMsg='" + warningMsg + '\'' + "\n" +
                ", errorMsg='" + errorMsg + '\'' + "\n" +
                ", resultType='" + resultType + '\'' + "\n" +
                ", result=" + result + "\n" +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Deprecated
    public int getTime() {
        return time;
    }

    @Deprecated
    public void setTime(int time) {
        this.time = time;
    }

    public int getDbTime() {
        return dbTime;
    }

    public void setDbTime(int dbTime) {
        this.dbTime = dbTime;
    }

    public int getNumResults() {
        return numResults;
    }

    public void setNumResults(int numResults) {
        this.numResults = numResults;
    }

    public long getNumTotalResults() {
        return numTotalResults;
    }

    public void setNumTotalResults(long numTotalResults) {
        this.numTotalResults = numTotalResults;
    }

    public String getWarningMsg() {
        return warningMsg;
    }

    public void setWarningMsg(String warningMsg) {
        this.warningMsg = warningMsg;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    @Deprecated
    public String getFeatureType() {
        return featureType;
    }

    @Deprecated
    public void setFeatureType(String featureType) {
        this.featureType = featureType;
    }

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public List<T> getResult() {
        return result;
    }

    public void setResult(List<T> result) {
        this.result = result;
//        if (result.size() > 0) {
//            this.resultType = result.get(0).getClass().getCanonicalName();
//        }
//        this.numResults = result.size();
    }

    @Deprecated
    public void addResult(T result) {
        this.resultType = result.getClass().getCanonicalName();
        this.result.add(result);
        this.numResults = this.result.size();
    }

    @Deprecated
    public void addAllResults(List<T> result) {
        if (result.size() > 0) {
            this.resultType = result.get(0).getClass().getCanonicalName();
        }
        this.result.addAll(result);
        this.numResults = this.result.size();
    }

}
