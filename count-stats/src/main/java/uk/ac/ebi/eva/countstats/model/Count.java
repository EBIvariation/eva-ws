package uk.ac.ebi.eva.countstats.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Objects;


@Entity
@Table(name = "count_stats")
public class Count {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String process;


    @Column(nullable = false, columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private String identifier;

    @Column(nullable = false)
    private String metric;

    @Column(nullable = false)
    private long count;

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime timestamp;

    public Count() {
    }

    public Count(String process, String identifier, String metric, long count) {
        if (process == null) throw new NullPointerException("process can't be null");
        if (identifier == null) throw new NullPointerException("identifier can't be null");
        if (metric == null) throw new NullPointerException("metric can't be null");
        this.process = process;
        this.identifier = identifier;
        this.metric = metric;
        this.count = count;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Count count1 = (Count) o;
        return count == count1.count && Objects.equals(process, count1.process)
                && Objects.equals(identifier, count1.identifier) && Objects.equals(metric, count1.metric);
    }

    @Override
    public int hashCode() {
        return Objects.hash(process, identifier, metric, count);
    }

    @Override
    public String toString() {
        return "Count{"
                + "id=" + id
                + ", process='" + process + '\''
                + ", identifier='" + identifier + '\''
                + ", metric='" + metric + '\''
                + ", count=" + count
                + ", timestamp=" + timestamp
                + '}';
    }
}
