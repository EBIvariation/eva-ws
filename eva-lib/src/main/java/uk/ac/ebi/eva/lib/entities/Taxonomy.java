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
package uk.ac.ebi.eva.lib.entities;


import uk.ac.ebi.eva.lib.models.Assembly;

import javax.persistence.*;

/**
 * Created by jorizci on 03/10/16.
 */
@Entity
@SqlResultSetMappings({
        @SqlResultSetMapping(
                name = "assembly",
                classes = @ConstructorResult(
                        targetClass = Assembly.class,
                        columns = {
                                @ColumnResult(name = "assembly_accession", type = String.class),
                                @ColumnResult(name = "assembly_chain", type = String.class),
                                @ColumnResult(name = "assembly_version", type = String.class),
                                @ColumnResult(name = "assembly_name", type = String.class),
                                @ColumnResult(name = "assembly_code", type = String.class),
                                @ColumnResult(name = "taxonomy_id", type = Integer.class),
                                @ColumnResult(name = "common_name", type = String.class),
                                @ColumnResult(name = "scientific_name", type = String.class),
                                @ColumnResult(name = "taxonomy_code", type = String.class),
                                @ColumnResult(name = "eva_name", type = String.class)
                        }
                )
        )
})
@NamedNativeQueries({
        @NamedNativeQuery(
                name = "Taxonomy.getBrowsableSpecies",
                query = "select distinct(assembly.*), taxonomy.* " +
                        "from assembly left join browsable_file bf on assembly.assembly_set_id=bf.assembly_set_id " +
                        " and assembly.assembly_accession = bf.loaded_assembly " +
                        "left join dbsnp_assemblies dbs on assembly.assembly_set_id = dbs.assembly_set_id " +
                        "join taxonomy on assembly.taxonomy_id=taxonomy.taxonomy_id " +
                        "where (bf.loaded = true and bf.deleted = false) or (dbs.loaded = true)",
                resultSetMapping = "assembly"
        ),
        @NamedNativeQuery(
                name = "Taxonomy.getAccessionedSpecies",
                query = "select distinct(assembly.*), taxonomy.* " +
                        "from assembly join taxonomy on assembly.taxonomy_id=taxonomy.taxonomy_id " +
                        "where assembly_in_accessioning_store = true",
                resultSetMapping = "assembly"
        )
})
@Table(name = "taxonomy")
public class Taxonomy {

    @Id
    @Column(name = "taxonomy_id")
    private Long taxonomyId;

    @Column(length = 45, name = "common_name")
    private String commonName;

    @Column(length = 45, name = "scientific_name")
    private String scientificName;

    @Column(length = 100, name = "taxonomy_code")
    private String taxonomyCode;

    @Column(length = 25, name = "eva_name")
    private String evaName;

    public Taxonomy(Long taxonomyId, String commonName, String scientificName, String taxonomyCode,
                    String evaName) {
        this.taxonomyId = taxonomyId;
        this.commonName = commonName;
        this.scientificName = scientificName;
        this.taxonomyCode = taxonomyCode;
        this.evaName = evaName;
    }
}
