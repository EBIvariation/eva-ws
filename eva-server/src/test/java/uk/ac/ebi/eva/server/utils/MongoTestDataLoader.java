package uk.ac.ebi.eva.server.utils;

import com.mongodb.client.MongoCollection;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MongoTestDataLoader {
    private final MongoTemplate mongoTemplate;
    private final ResourceLoader resourceLoader;

    public MongoTestDataLoader(MongoTemplate mongoTemplate, ResourceLoader resourceLoader) {
        this.mongoTemplate = mongoTemplate;
        this.resourceLoader = resourceLoader;
    }

    public void load(String resourcePath) {
        load(resourcePath, null);
    }

    public void load(String resourcePath, String collectionName) {
        load(resourcePath, collectionName, false);
    }

    public void load(String resourcePath, String collectionName, boolean dropCollection) {
        try {
            BsonDocument bsonRoot = readBsonRoot(resourcePath);

            String resolvedCollectionName;
            BsonValue dataValue;

            if (collectionName != null && bsonRoot.containsKey(collectionName)) {
                resolvedCollectionName = collectionName;
                dataValue = bsonRoot.get(collectionName);
            } else if (bsonRoot.size() == 1) {
                Map.Entry<String, BsonValue> entry = bsonRoot.entrySet().iterator().next();
                resolvedCollectionName = entry.getKey();
                dataValue = entry.getValue();
            } else {
                throw new IllegalArgumentException("JSON file has multiple collections but no collectionName was specified. " +
                                "Use loadAll() to load all collections, or pass a collectionName. File: " + resourcePath);
            }

            insertIntoCollection(resolvedCollectionName, dataValue, dropCollection);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load test data from: " + resourcePath, e);
        }
    }

    public void loadAll(String resourcePath) {
        loadAll(resourcePath, false);
    }

    public void loadAll(String resourcePath, boolean dropCollection) {
        try {
            BsonDocument bsonRoot = readBsonRoot(resourcePath);

            for (Map.Entry<String, BsonValue> entry : bsonRoot.entrySet()) {
                insertIntoCollection(entry.getKey(), entry.getValue(), dropCollection);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to load test data from: " + resourcePath, e);
        }
    }
    private BsonDocument readBsonRoot(String resourcePath) throws Exception {
        Resource resource = resourceLoader.getResource("classpath:" + resourcePath);
        String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        // Normalize timezone format: +0000 -> +00:00 for BSON date parsing
        content = content.replaceAll("(\\+\\d{2})(\\d{2})\"\\)", "$1:$2\")");

        return BsonDocument.parse(content);
    }

    private void insertIntoCollection(String collectionName, BsonValue dataValue, boolean dropCollection) {
        List<BsonDocument> bsonDocs = new ArrayList<>();
        if (dataValue.isArray()) {
            for (BsonValue item : dataValue.asArray()) {
                if (item.isDocument()) {
                    bsonDocs.add(item.asDocument());
                }
            }
        } else if (dataValue.isDocument()) {
            bsonDocs.add(dataValue.asDocument());
        }

        MongoCollection<BsonDocument> collection = mongoTemplate.getDb().getCollection(collectionName, BsonDocument.class);

        if (dropCollection) {
            collection.deleteMany(new BsonDocument());
        }
        if (!bsonDocs.isEmpty()) {
            collection.insertMany(bsonDocs);
        }
    }
}