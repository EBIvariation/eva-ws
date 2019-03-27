/*
 * Copyright 2019 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.eva.server.models;

import java.io.Serializable;

public class ProgressReportPK implements Serializable {

    private String databaseName;

    private String genbankAssemblyAccession;

    public ProgressReportPK() {
    }

    public ProgressReportPK(String databaseName, String genbankAssemblyAccession) {
        this.databaseName = databaseName;
        this.genbankAssemblyAccession = genbankAssemblyAccession;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getGenbankAssemblyAccession() {
        return genbankAssemblyAccession;
    }

    public void setGenbankAssemblyAccession(String genbankAssemblyAccession) {
        this.genbankAssemblyAccession = genbankAssemblyAccession;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProgressReportPK that = (ProgressReportPK) o;

        if (!databaseName.equals(that.databaseName)) return false;
        return genbankAssemblyAccession.equals(that.genbankAssemblyAccession);
    }

    @Override
    public int hashCode() {
        int result = databaseName.hashCode();
        result = 31 * result + genbankAssemblyAccession.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return databaseName + "," + genbankAssemblyAccession;
    }

}
