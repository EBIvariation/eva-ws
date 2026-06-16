package uk.ac.ebi.eva.lib.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "experiment_type")
public class ExperimentType {

    @Id
    @Column(name = "experiment_type_id")
    private Long experimentTypeId;

    @Column(name = "experiment_type")
    private String experimentType;

    public ExperimentType() {
    }

    public ExperimentType(Long experimentTypeId, String experimentType) {
        this.experimentTypeId = experimentTypeId;
        this.experimentType = experimentType;
    }

    public String getExperimentType() {
        return experimentType;
    }

}
