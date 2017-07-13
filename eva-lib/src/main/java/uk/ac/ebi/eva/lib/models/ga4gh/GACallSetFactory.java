/*
 * Copyright 2017 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.eva.lib.models.ga4gh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class GACallSetFactory {

    public static List<GACallSet> create(List<String> variantSetNames, List<List<String>> callSets) {
        List<GACallSet> sets = new ArrayList<>();

        Iterator<String> variantSetNamesIterator = variantSetNames.iterator();
        Iterator<List<String>> callSetsIterator = callSets.iterator();

        while (variantSetNamesIterator.hasNext() && callSetsIterator.hasNext()) {
            String fileName = variantSetNamesIterator.next();
            List<String> callsInFile = callSetsIterator.next();
            // Add all samples in the file
            for (String callName : callsInFile) {
                GACallSet callset = new GACallSet(callName, callName, callName, Collections.singletonList(fileName),
                        System.currentTimeMillis(), System.currentTimeMillis(), null);
                sets.add(callset);
            }
        }

        return sets;
    }
}

