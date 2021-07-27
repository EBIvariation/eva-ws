package uk.ac.ebi.eva.countstats.configuration;

import org.hibernate.dialect.PostgreSQL9Dialect;

import java.sql.Types;

public class JsonPostgreSQLDialect extends PostgreSQL9Dialect {
    public JsonPostgreSQLDialect() {
        super();
        this.registerColumnType(Types.JAVA_OBJECT, "json");
    }
}