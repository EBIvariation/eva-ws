/*
 * Copyright 2017 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.eva.server;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Converter;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "import_progress")
public class ProgressReport {

    @Id
    private String databaseName;

    private int taxId;

    private String scientificName;

    private String genbankAssemblyAccession;

    private int lastDbsnpBuild;

    private boolean inEnsembl;

    private boolean toVariantWarehouse;

    private boolean assemblyFullyMatches;

    @Enumerated(EnumType.STRING)
    @Convert(converter = StatusConverter.class)
    private Status variantsImported;

    @Enumerated(EnumType.STRING)
    @Convert(converter = StatusConverter.class)
    private Status rsSynonymsImported;

    private Date variantsImportedDate;

    private Date rsSynonymsImportedDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProgressReport that = (ProgressReport) o;

        if (taxId != that.taxId) return false;
        if (lastDbsnpBuild != that.lastDbsnpBuild) return false;
        if (!databaseName.equals(that.databaseName)) return false;
        if (!scientificName.equals(that.scientificName)) return false;
        return genbankAssemblyAccession != null ? genbankAssemblyAccession
                .equals(that.genbankAssemblyAccession) : that.genbankAssemblyAccession == null;
    }

    @Override
    public int hashCode() {
        int result = databaseName.hashCode();
        result = 31 * result + taxId;
        result = 31 * result + scientificName.hashCode();
        result = 31 * result + (genbankAssemblyAccession != null ? genbankAssemblyAccession.hashCode() : 0);
        result = 31 * result + lastDbsnpBuild;
        return result;
    }
}
