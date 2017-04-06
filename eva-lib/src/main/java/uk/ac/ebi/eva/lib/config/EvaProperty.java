package uk.ac.ebi.eva.lib.config;

import com.sun.istack.internal.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "eva")
@Component
//@Configuration
public class EvaProperty {

//    eva.mongo.host=@eva.mongo.host@
//    eva.mongo.user=@eva.mongo.user@
//    eva.mongo.passwd=@eva.mongo.passwd@
//    eva.mongo.auth.db=@eva.mongo.auth.db@
//    eva.mongo.read-preference=@eva.mongo.read-preference@
//    eva.mongo.collections.variants=@eva.mongo.collections.variants@
//    eva.mongo.collections.files=@eva.mongo.collections.files@
//    eva.version=@eva.version@

    private String version;

    private Mongo mongo;

    public static class Mongo {
        private String host;
        private String user;
        private String passwd;
        private String readPreference;

        private Auth auth;

        private Collections collections;

        public static class Auth {
            private String db;

            public String getDb() {
                return db;
            }

            public void setDb(String db) {
                this.db = db;
            }
        }

        public static class Collections {
            private String variants;
            private String files;

            public String getVariants() {
                return variants;
            }

            public void setVariants(String variants) {
                this.variants = variants;
            }

            public String getFiles() {
                return files;
            }

            public void setFiles(String files) {
                this.files = files;
            }
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPasswd() {
            return passwd;
        }

        public void setPasswd(String passwd) {
            this.passwd = passwd;
        }

        public String getReadPreference() {
            return readPreference;
        }

        public void setReadPreference(String readPreference) {
            this.readPreference = readPreference;
        }

        public Auth getAuth() {
            return auth;
        }

        public void setAuth(Auth auth) {
            this.auth = auth;
        }

        public Collections getCollections() {
            return collections;
        }

        public void setCollections(Collections collections) {
            this.collections = collections;
        }
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Mongo getMongo() {
        return mongo;
    }

    public void setMongo(Mongo mongo) {
        this.mongo = mongo;
    }
}
