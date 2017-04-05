package uk.ac.ebi.eva.lib.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "eva.mongo")
public class EvaMongoProperty {

    private String host;
    private String user;
    private String passwd;
    private String readPreference;
    private String variants;
    private String files;

//    eva.mongo.host=@eva.mongo.host@
//    eva.mongo.user=@eva.mongo.user@
//    eva.mongo.passwd=@eva.mongo.passwd@
//    eva.mongo.auth.db=@eva.mongo.auth.db@
//    eva.mongo.read-preference=@eva.mongo.read-preference@
//    eva.mongo.collections.variants=@eva.mongo.collections.variants@
//    eva.mongo.collections.files=@eva.mongo.collections.files@
//    eva.version=@eva.version@


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
