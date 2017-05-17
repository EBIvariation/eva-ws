/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2016 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.eva.lib.repository;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.opencb.biodata.models.feature.Region;
import org.opencb.biodata.models.variant.VariantSourceEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import uk.ac.ebi.eva.commons.models.data.VariantSourceEntity;
import uk.ac.ebi.eva.commons.models.metadata.VariantEntity;
import uk.ac.ebi.eva.lib.filter.VariantEntityRepositoryFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Concrete implementation of the VariantEntityRepository interface (relationship inferred by Spring),
 * due to a custom DBObject to VariantEntity conversion
 *
 * <p>It also implements the VariantEntityRepositoryCustom interface,
 * to provide an explicit implementation of the region query, using a margin for efficiency.
 */
public class VariantEntityRepositoryImpl implements VariantEntityRepositoryCustom {

    @Autowired
    private VariantSourceEntityRepository variantSourceEntityRepository;

    private MongoTemplate mongoTemplate;

    protected static Logger logger = LoggerFactory.getLogger(VariantEntityRepositoryImpl.class);

    private final int MARGIN = 5000;

    @Autowired
    public VariantEntityRepositoryImpl(MongoDbFactory mongoDbFactory, MappingMongoConverter mappingMongoConverter) {
        mongoTemplate = new MongoTemplate(mongoDbFactory, mappingMongoConverter);
    }

    @Override
    public List<VariantEntity> findByIdsAndComplexFilters(String id, List<VariantEntityRepositoryFilter> filters,
                                                          List<String> exclude, Pageable pageable) {
        Query query = new Query(Criteria.where("ids").is(id));
        return findByComplexFiltersHelper(query, filters, exclude, pageable);
    }

    @Override
    public Long countByIdsAndComplexFilters(String id, List<VariantEntityRepositoryFilter> filters) {
        Criteria criteria = Criteria.where("ids").is(id);
        return countByComplexFiltersHelper(criteria, filters);
    }

    @Override
    public List<VariantEntity> findByRegionsAndComplexFilters(List<Region> regions,
                                                              List<VariantEntityRepositoryFilter> filters,
                                                              List<String> exclude, Pageable pageable) {
        Query query = new Query();
        Criteria criteria = getRegionsCriteria(regions);
        query.addCriteria(criteria);
        return findByComplexFiltersHelper(query, filters, exclude, pageable);
    }

    @Override
    public Long countByRegionsAndComplexFilters(List<Region> regions, List<VariantEntityRepositoryFilter> filters) {
        Criteria criteria = getRegionsCriteria(regions);
        return countByComplexFiltersHelper(criteria, filters);
    }

    @Override
    public Set<String> findDistinctChromosomes() {
        return new HashSet<>(
                (List<String>) mongoTemplate.getCollection(mongoTemplate.getCollectionName(VariantEntity.class))
                                            .distinct("chr"));
    }

    private List<VariantEntity> findByComplexFiltersHelper(Query query, List<VariantEntityRepositoryFilter> filters,
                                                           List<String> exclude, Pageable pageable) {

        addFilterCriteriaToQuery(query, filters);

        ArrayList<String> sortProperties = new ArrayList<String>();
        sortProperties.add("chr");
        sortProperties.add("start");
        query.with(new Sort(Sort.Direction.ASC, sortProperties));

        Pageable pageable1 = (pageable != null) ? pageable : new PageRequest(0, 10);
        query.with(pageable1);

        if (exclude != null && !exclude.isEmpty()) {
            exclude.forEach(e -> query.fields().exclude(e));
        }

        List<VariantEntity> variantEntities = mongoTemplate.find(query, VariantEntity.class);

        Table<String, String, Map<String, String>> studyFileIdsToPositionSamples = studyFileIdsToPositionSamples();

        variantEntities = updateVariantEntitiesSampleNames(variantEntities, studyFileIdsToPositionSamples);

        return variantEntities;
    }

    private Table<String, String, Map<String, String>> studyFileIdsToPositionSamples() {
        List<VariantSourceEntity> variantSourceEntities = variantSourceEntityRepository.findAll();

        Table<String, String, Map<String, String>> studyFileIdsToPositionSamples = HashBasedTable.create();

        for (VariantSourceEntity variantSourceEntity : variantSourceEntities) {
            if (variantSourceEntity.getSamplesPosition() == null) {
                continue;
            }

            String studyId = variantSourceEntity.getStudyId();
            String fileId = variantSourceEntity.getFileId();

            Map<String, Integer> samplesPosition = variantSourceEntity.getSamplesPosition();
            Map<String, String> positionSamples = new HashMap<>();
            for(Map.Entry<String, Integer> entry : samplesPosition.entrySet()) {
                positionSamples.put(Integer.toString(entry.getValue()), entry.getKey());
            }

            studyFileIdsToPositionSamples.put(studyId, fileId, positionSamples);
        }

        return studyFileIdsToPositionSamples;
    }

    private List<VariantEntity> updateVariantEntitiesSampleNames(List<VariantEntity> variantEntities,
                                                                 Table<String, String, Map<String, String>>
                                                                         studyFileIdsToPositionSamples) {

        variantEntities = variantEntities.stream().map(
                variantEntity -> updateVariantEntitySampleNames(variantEntity, studyFileIdsToPositionSamples)
        ).collect(Collectors.toList());
        return variantEntities;
    }

    private VariantEntity updateVariantEntitySampleNames(VariantEntity variantEntity,
                                                         Table<String, String, Map<String, String>>
                                                                 studyFileIdsToPositionSamples) {
        Map<String, VariantSourceEntry> variantSourceEntryMap = variantEntity.getSourceEntries().entrySet().stream().collect(
                Collectors.toMap(
                        Map.Entry::getKey,
                        e -> updateVariantEntitySampleNames(e.getValue(), studyFileIdsToPositionSamples)
                )
        );
        variantEntity.setSourceEntries(variantSourceEntryMap);
        return variantEntity;
    }

    private VariantSourceEntry updateVariantEntitySampleNames(VariantSourceEntry variantSourceEntry,
                                                              Table<String, String, Map<String, String>>
                                                                      studyFileIdsToPositionSamples) {
        // Get samples data from that variant entry
        Map<String,Map<String,String>> samplesData = variantSourceEntry.getSamplesData();
        if ((samplesData == null) || (samplesData.size() == 0)) {
            return variantSourceEntry;
        }

        // Get the default genotype string for that variant entry and remove it
        String defaultGt = samplesData.get("def").get("GT");
        samplesData.remove("def");

        String studyId = variantSourceEntry.getStudyId();
        String fileId = variantSourceEntry.getFileId();

        // Get the map of sample index to sample name for that study and file
        Map<String, String> indexesToNames = studyFileIdsToPositionSamples.get(studyId, fileId);
        for (Map.Entry<String, String> indexToName : indexesToNames.entrySet()) {
            String sampleIndex = indexToName.getKey();
            String sampleName = indexToName.getValue();

            // New sample data ("GT" -> gtString)
            Map<String, String> sampleData = new HashMap<>(1);
            // If the sample index is already in the samplesData then remove it
            if (samplesData.containsKey(sampleIndex)) {
                sampleData = samplesData.remove(sampleIndex);
            } else {
                // If sample index not in samplesData then infer it has the default gt string
                sampleData.put("GT", defaultGt);
            }
            // Put the new sample data into samplesData
            samplesData.put(sampleName, sampleData);
        }
        return variantSourceEntry;
    }

    private void addFilterCriteriaToQuery(Query query, List<VariantEntityRepositoryFilter> filters) {
        if (filters != null && filters.size() > 0){
            List<Criteria> criteriaList = getFiltersCriteria(filters);
            for (Criteria criteria : criteriaList) {
                query.addCriteria(criteria);
            }
        }
    }

    private long countByComplexFiltersHelper(Criteria existingCriteria, List<VariantEntityRepositoryFilter> filters) {
        List<Criteria> criteriaList = getFiltersCriteria(filters);
        criteriaList.add(existingCriteria);
        Criteria criteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[criteriaList.size()]));

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group().count().as("count")
        );

        AggregationResults<VariantAggregationCount> aggregationResults =
                mongoTemplate.aggregate(aggregation, VariantEntity.class, VariantAggregationCount.class);

        return aggregationResults.getMappedResults().size() > 0
                ? aggregationResults.getMappedResults().get(0).getCount() : 0;
    }

    private class VariantAggregationCount {
        private long count;

        public long getCount() {
            return count;
        }
    }

    private List<Criteria> getFiltersCriteria(List<VariantEntityRepositoryFilter> filters) {
        List<Criteria> criteriaList = new ArrayList<>();
        for (VariantEntityRepositoryFilter filter : filters) {
            criteriaList.add(filter.getCriteria());
        }
        return criteriaList;
    }

    private Criteria getRegionsCriteria(List<Region> regions) {
        List<Criteria> orRegionCriteria = new ArrayList<>();

        regions.forEach(region -> orRegionCriteria.add(
                Criteria.where("chr").is(region.getChromosome())
                        .and("start").lte(region.getEnd()).gt(region.getStart() - MARGIN)
                        .and("end").gte(region.getStart()).lt(region.getEnd() + MARGIN)));

        return new Criteria().orOperator(orRegionCriteria.toArray(new Criteria[orRegionCriteria.size()]));
    }

}
