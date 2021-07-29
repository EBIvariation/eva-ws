package uk.ac.ebi.eva.countstats.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import uk.ac.ebi.eva.countstats.configuration.StringJsonUserType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "count_stats")
@TypeDefs({@TypeDef(name = "StringJsonObject", typeClass = StringJsonUserType.class)})
public class Count {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @Column(nullable = false)
    private String process;

    @NonNull
    @Column(nullable = false)
    @Type(type = "StringJsonObject")
    private String identifier;

    @NonNull
    @Column(nullable = false)
    private String metric;

    @NonNull
    @Column(nullable = false)
    private long count;

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime timestamp;
}
