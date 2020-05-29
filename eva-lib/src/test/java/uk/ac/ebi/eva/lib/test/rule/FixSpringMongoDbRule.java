/*
 *
 * Copyright 2019 EMBL - European Bioinformatics Institute
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
 *
 */
package uk.ac.ebi.eva.lib.test.rule;

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
