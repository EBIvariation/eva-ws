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

package uk.ac.ebi.eva.commons.models.data;

import org.opencb.biodata.models.variant.VariantSource;
import org.opencb.biodata.models.variant.VariantStudy;
import org.opencb.biodata.models.variant.stats.VariantGlobalStats;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * Represents a file (VariantSource) in a database.
 * <p>
 * TODO jmmut: VariantSource also has pedigree
 */
@Document(collection = "#{mongoCollectionsFiles}")
public class VariantSourceEntity {

    public final static String FILEID_FIELD = "fid";

    public final static String FILENAME_FIELD = "fname";

    public final static String STUDYID_FIELD = "sid";

    public final static String STUDYNAME_FIELD = "sname";

    public final static String STUDYTYPE_FIELD = "stype";

    public final static String AGGREGATION_FIELD = "aggregation";

    public final static String DATE_FIELD = "date";

    public final static String SAMPLES_FIELD = "samp";

    public final static String STATISTICS_FIELD = "st";

    public final static String STATISTICS_NUMSAMPLES_FIELD = "nSamp";

    public final static String STATISTICS_NUMVARIANTS_FIELD = "nVar";

    public final static String STATISTICS_NUMSNPS_FIELD = "nSnp";

    public final static String STATISTICS_NUMINDELS_FIELD = "nIndel";

    public final static String STATISTICS_NUMSTRUCTURAL_FIELD = "nSv";

    public final static String STATISTICS_NUMPASSFILTERS_FIELD = "nPass";

    public final static String STATISTICS_NUMTRANSITIONS_FIELD = "nTi";

    public final static String STATISTICS_NUMTRANSVERSIONS_FIELD = "nTv";

    public final static String STATISTICS_MEANQUALITY_FIELD = "meanQ";

    public final static String METADATA_FIELD = "meta";

    public final static String METADATA_FILEFORMAT_FIELD = "fileformat";

    public final static String METADATA_HEADER_FIELD = "header";

    @Field(value = FILEID_FIELD)
    private String fileId;

    @Field(value = FILENAME_FIELD)
    private String fileName;

    @Field(value = STUDYID_FIELD)
    private String studyId;

    @Field(value = STUDYNAME_FIELD)
    private String studyName;

    @Field(value = STUDYTYPE_FIELD)
    private VariantStudy.StudyType type;

    @Field(value = AGGREGATION_FIELD)
    private VariantSource.Aggregation aggregation;

    @Field(value = DATE_FIELD)
    private Date date;

    @Field(value = SAMPLES_FIELD)
    private Map<String, Integer> samplesPosition;

    @Field(value = METADATA_FIELD)
    private Map<String, Object> metadata;

    @Field(value = STATISTICS_FIELD)
    private VariantGlobalStats stats;

    public VariantSourceEntity() {
    }

    public VariantSourceEntity(String fileId, String fileName, String studyId, String studyName,
            VariantStudy.StudyType type, VariantSource.Aggregation aggregation,
            Map<String, Integer> samplesPosition, Map<String, Object> metadata,
            VariantGlobalStats stats) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.studyId = studyId;
        this.studyName = studyName;
        this.type = type;
        this.aggregation = aggregation;
        this.samplesPosition = samplesPosition;
        this.metadata = metadata;
        this.stats = stats;
        this.date = Calendar.getInstance().getTime();
    }

    public VariantSourceEntity(VariantSource source) {
        this(source.getFileId(), source.getFileName(), source.getStudyId(), source.getStudyName(), source.getType(),
                source.getAggregation(), source.getSamplesPosition(), source.getMetadata(), source.getStats());
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getStudyId() {
        return studyId;
    }

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    public String getStudyName() {
        return studyName;
    }

    public void setStudyName(String studyName) {
        this.studyName = studyName;
    }

    public VariantStudy.StudyType getType() {
        return type;
    }

    public void setType(VariantStudy.StudyType type) {
        this.type = type;
    }

    public VariantSource.Aggregation getAggregation() {
        return aggregation;
    }

    public void setAggregation(VariantSource.Aggregation aggregation) {
        this.aggregation = aggregation;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Map<String, Integer> getSamplesPosition() {
        return samplesPosition;
    }

    public void setSamplesPosition(Map<String, Integer> samplesPosition) {
        this.samplesPosition = samplesPosition;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public VariantGlobalStats getStats() {
        return stats;
    }

    public void setStats(VariantGlobalStats stats) {
        this.stats = stats;
    }

}
