/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2014, 2015 EMBL - European Bioinformatics Institute
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

package uk.ac.ebi.eva.lib.datastore;


import uk.ac.ebi.eva.lib.models.VariantStudy;
import uk.ac.ebi.eva.lib.storage.metadata.ArchiveEvaproDBAdaptor;

/**
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
public class EvaproUtils {

    public static VariantStudy.StudyType stringToStudyType(String studyType) {
        switch (studyType) {
            case "Collection":
            case "Curated Collection":
                return VariantStudy.StudyType.COLLECTION;
            case "Control Set":
            case "Control-Set":
                return VariantStudy.StudyType.CONTROL;
            case "Case Control":
            case "Case-Control":
                return VariantStudy.StudyType.CASE_CONTROL;
            case "Case Set":
            case "Case-Set":
                return VariantStudy.StudyType.CASE;
            case "Tumor vs. Matched-Normal":
                return VariantStudy.StudyType.PAIRED_TUMOR;
            case "Aggregate":
                return VariantStudy.StudyType.AGGREGATE;
            default:
                throw new IllegalArgumentException("Study type " + studyType + " is not valid");
        }
    }


    public static String studyTypeToString(VariantStudy.StudyType studyType) {
        switch (studyType) {
            case COLLECTION:
                return "Collection";
            case CONTROL:
                return "Control Set";
            case CASE_CONTROL:
                return "Case-Control";
            case CASE:
                return "Case-Set";
            case PAIRED:
                return "Tumor vs. Matched-Normal";
            default:
                StringBuilder lower = new StringBuilder(studyType.name().toLowerCase());
                lower.replace(0, 1, studyType.name().substring(0, 1)); // First letter uppercase
                return lower.toString();
        }
    }
}
