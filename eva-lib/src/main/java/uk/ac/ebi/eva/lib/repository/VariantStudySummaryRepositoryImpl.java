/*
 * Copyright 2016-2017 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.eva.lib.repository;

import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.query.Criteria;

import uk.ac.ebi.eva.commons.models.data.VariantSourceEntity;
import uk.ac.ebi.eva.lib.repository.projections.VariantStudySummary;

import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;

/**
 * Implementation of the {@link VariantStudySummaryRepositoryCustom} interface (relationship with
 * {@link VariantStudySummaryRepository} inferred by Spring).
 * <p>
 * The repository implementation could not be made implicitly by spring, due to use of MongoDB Aggregation Framework.
 */
public class VariantStudySummaryRepositoryImpl implements VariantStudySummaryRepositoryCustom {

    private static final String STUDY_ID = "studyId";

    private static final String STUDY_NAME = "studyName";

    private static final String FILES_COUNT = "filesCount";

    private static final String ID = "_id.";

    private MongoTemplate mongoTemplate;

    public VariantStudySummaryRepositoryImpl(MongoDbFactory mongoDbFactory, MappingMongoConverter mappingMongoConverter) {
        mongoTemplate = new MongoTemplate(mongoDbFactory, mappingMongoConverter);
    }

    /**
     * the equivalent intended query is:
     * db.files.aggregate([
     *  {$match : { $or:[{"sid" : "studyNameOrId"} , {"sname": "studyNameOrId"}]} },
     *  {$group:{_id: {studyId:"$sid",studyName:"$sname"}, filesCount:{$sum:1}}},
     *  {$project:{"studyId" : "$_id.studyId", "studyName" : "$_id.studyName", "_id" : 0, "filesCount":"$filesCount" }}
     *  ])
     * See also the inner explanation of those 3 stages
     *  @see #matchByNameOrId
     *  @see #groupAndCount
     *  @see #projectAndFlatten
     */
    public VariantStudySummary findByStudyNameOrStudyId(String studyNameOrId) {
        Aggregation aggregation = Aggregation.newAggregation(
                matchByNameOrId(studyNameOrId),
                groupAndCount(),
                projectAndFlatten()
        );

        AggregationResults<VariantStudySummary> studies = mongoTemplate.aggregate(aggregation,
                VariantSourceEntity.class,
                VariantStudySummary.class);

        VariantStudySummary variantStudySummary;
        if (studies.getMappedResults().size() == 0) {
            variantStudySummary = null;
        } else {
            variantStudySummary = studies.getMappedResults().get(0);
        }

        return variantStudySummary;
    }

    /**
     * the equivalent intended query is:
     * db.files.aggregate([
     *  {$group:{_id: {studyId:"$sid",studyName:"$sname"}, filesCount:{$sum:1}}},
     *  {$project:{"studyId" : "$_id.studyId", "studyName" : "$_id.studyName", "_id" : 0, "filesCount":"$filesCount" }}
     *  ])
     * See also the inner explanation of those 2 stages
     *  @see #groupAndCount
     *  @see #projectAndFlatten
     */
    public List<VariantStudySummary> findBy() {
        Aggregation aggregation = Aggregation.newAggregation(
                groupAndCount(),
                projectAndFlatten()
        );

        AggregationResults<VariantStudySummary> studies = mongoTemplate.aggregate(aggregation,
                VariantSourceEntity.class,
                VariantStudySummary.class);

        return studies.getMappedResults();
    }

    /**
     * Match only the documents that contain the argument as studyName or studyId.
     */
    private MatchOperation matchByNameOrId(String studyNameOrId) {
        return match(new Criteria().orOperator(
                Criteria.where(STUDY_ID).is(studyNameOrId),
                Criteria.where(STUDY_NAME).is(studyNameOrId)));
    }

    /**
     * Group by both "studyName" and "studyId", and count the documents in the group into an accumulator field
     * "filesCount". The idea behind grouping by 2 fields is to include both fields without further projections or
     * queries (this group operation is not intended for having the combinations because a studyId will have
     * always the same studyName).
     */
    private GroupOperation groupAndCount() {
        return group(STUDY_ID, STUDY_NAME).count().as(FILES_COUNT);
    }

    /**
     * Flatten the document from {_id: {sid, sname}, filesCount} to {sid, sname, filesCount} which is ready to
     * be mapped into a {@link VariantStudySummary}.
     */
    private ProjectionOperation projectAndFlatten() {
        return project(FILES_COUNT)
                .and(ID + STUDY_ID).as(STUDY_ID)
                .and(ID + STUDY_NAME).as(STUDY_NAME);
    }

}

