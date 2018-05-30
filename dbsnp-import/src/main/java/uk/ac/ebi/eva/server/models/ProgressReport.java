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
package uk.ac.ebi.eva.server.models;

import javax.persistence.Column;
import javax.persistence.Convert;
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

    @Column(nullable = false)
    private int taxId;

    @Column(nullable = false)
    private String scientificName;

    @Column(nullable = false)
    private String commonName;

    private String genbankAssemblyAccession;

    @Column(nullable = false)
    private int lastDbsnpBuild;

    @Column(nullable = false)
    private boolean inEnsembl;

    private Boolean toVariantWarehouse;

    private Boolean assemblyFullyMatches;

    @Enumerated(EnumType.STRING)
    @Convert(converter = StatusConverter.class)
    private Status variantsImported;

    @Enumerated(EnumType.STRING)
    @Convert(converter = StatusConverter.class)
    private Status variantsWithoutEvidenceImported;

    @Enumerated(EnumType.STRING)
    @Convert(converter = StatusConverter.class)
    private Status rsSynonymsImported;

    private Date variantsImportedDate;

    private Date variantsWithoutEvidenceImportedDate;

    private Date rsSynonymsImportedDate;

    ProgressReport() {

    }

    public ProgressReport(String databaseName, int taxId, String scientificName, String commonName,
                          String genbankAssemblyAccession, int lastDbsnpBuild, boolean inEnsembl,
                          Boolean toVariantWarehouse, Boolean assemblyFullyMatches, Status variantsImported,
                          Status variantsWithoutEvidenceImported, Status rsSynonymsImported, Date variantsImportedDate,
                          Date variantsWithoutEvidenceImportedDate, Date rsSynonymsImportedDate) {
        this.databaseName = databaseName;
        this.taxId = taxId;
        this.scientificName = scientificName;
        this.commonName = commonName;
        this.genbankAssemblyAccession = genbankAssemblyAccession;
        this.lastDbsnpBuild = lastDbsnpBuild;
        this.inEnsembl = inEnsembl;
        this.toVariantWarehouse = toVariantWarehouse;
        this.assemblyFullyMatches = assemblyFullyMatches;
        this.variantsImported = variantsImported;
        this.variantsWithoutEvidenceImported = variantsWithoutEvidenceImported;
        this.rsSynonymsImported = rsSynonymsImported;
        this.variantsImportedDate = variantsImportedDate;
        this.variantsWithoutEvidenceImportedDate = variantsWithoutEvidenceImportedDate;
        this.rsSynonymsImportedDate = rsSynonymsImportedDate;
    }

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

    public String getDatabaseName() {
        return databaseName;
    }

    public int getTaxId() {
        return taxId;
    }

    public String getScientificName() {
        return scientificName;
    }

    public String getCommonName() {
        return commonName;
    }

    public String getGenbankAssemblyAccession() {
        return genbankAssemblyAccession;
    }

    public int getLastDbsnpBuild() {
        return lastDbsnpBuild;
    }

    public boolean isInEnsembl() {
        return inEnsembl;
    }

    public Boolean isToVariantWarehouse() {
        return toVariantWarehouse;
    }

    public Boolean isAssemblyFullyMatches() {
        return assemblyFullyMatches;
    }

    public Status getVariantsImported() {
        return variantsImported;
    }

    public Status getVariantsWithoutEvidenceImported() {
        return variantsWithoutEvidenceImported;
    }

    public Status getRsSynonymsImported() {
        return rsSynonymsImported;
    }

    public Date getVariantsImportedDate() {
        return variantsImportedDate;
    }

    public Date getVariantsWithoutEvidenceImportedDate() {
        return variantsWithoutEvidenceImportedDate;
    }

    public Date getRsSynonymsImportedDate() {
        return rsSynonymsImportedDate;
    }
}
