/*
 * Copyright 2016 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.eva.lib.datastore;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

/**
 * Simplified version of https://github.com/Loki-Afro/multi-tenant-spring-mongodb/blob/master/src/main/java/com/github/zarathustra/mongo/MultiTenantMongoDbFactory.java
 *
 * This is another implementation to MongoDbFactory, similar to SimpleMongoDbFactory, but allows to use several DBs.
 *
 * To use this class, you must @Autowire it in some component, or any place that loads beans into the environment.
 *
 * This class is used with the static method setDatabaseNameForCurrentThread, which uses a static ThreadLocal variable,
 * in order to make this change only visible to the current thread.
 *
 * @author Jose Miguel Mut Lopez &lt;jmmut@ebi.ac.uk&gt;
 */
public class MultiMongoDbFactory extends SimpleMongoDbFactory {

    protected static Logger logger = LoggerFactory.getLogger(MultiMongoDbFactory.class);

    private final String defaultName;
    private static final ThreadLocal<String> dbName = new ThreadLocal<>();

    public MultiMongoDbFactory(final MongoClient mongo, final String defaultDatabaseName) {
        super(mongo, defaultDatabaseName);
        logger.debug("Instantiating " + MultiMongoDbFactory.class.getName() + " with default database name: " + defaultDatabaseName);
        this.defaultName = defaultDatabaseName;
    }

    /**
     * This method allows to change the mongo connection to another database, for example, for reusing a
     * FeatureRepository across several DBs.
     * @param databaseName the DB that will be used next time someone does "mongoDbFactory.getDB()" (note empty parameter)
     */
    public static void setDatabaseNameForCurrentThread(final String databaseName) {
        logger.debug("Switching to database: " + databaseName);
        dbName.set(databaseName);
    }

    public static void clearDatabaseNameForCurrentThread() {
        if (logger.isDebugEnabled()) {
            logger.debug("Removing database [" + dbName.get() + "]");
        }
        dbName.remove();
    }

    @Override
    public DB getDb() {
        final String tlName = dbName.get();
        final String dbToUse = (tlName != null ? tlName : this.defaultName);
        logger.debug("Acquiring database: " + dbToUse);
        return super.getDb(dbToUse);
    }
}

