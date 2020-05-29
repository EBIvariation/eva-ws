package uk.ac.ebi.eva.server.test.rule;

import com.lordofthejars.nosqlunit.mongodb.MongoDbConfiguration;
import com.lordofthejars.nosqlunit.mongodb.SpringMongoDbRule;

/**
 * Temporary fix until nosql unit rc-6 or final is released
 */
public class FixSpringMongoDbRule extends SpringMongoDbRule {

    public FixSpringMongoDbRule(MongoDbConfiguration mongoDbConfiguration) {
        super(mongoDbConfiguration);
    }

    public FixSpringMongoDbRule(MongoDbConfiguration mongoDbConfiguration, Object object) {
        super(mongoDbConfiguration, object);
    }

    @Override
    public void close() {
        // DO NOT CLOSE the connection (Spring will do it when destroying the context)
    }

}
