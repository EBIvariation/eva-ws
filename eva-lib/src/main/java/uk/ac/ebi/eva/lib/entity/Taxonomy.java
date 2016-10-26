package uk.ac.ebi.eva.lib.entity;


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
                name = "Taxonomy.getSpecies",
                query = "select distinct(assembly.*), taxonomy.* " +
                        "from assembly join browsable_file bf on assembly.assembly_set_id=bf.assembly_set_id " +
                        "join taxonomy on assembly.taxonomy_id=taxonomy.taxonomy_id " +
                        "where bf.loaded = true and bf.deleted = false",
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

}
