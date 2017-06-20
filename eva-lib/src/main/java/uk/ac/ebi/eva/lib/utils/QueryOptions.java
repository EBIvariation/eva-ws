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
package uk.ac.ebi.eva.lib.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QueryOptions extends ObjectMap {


    public QueryOptions() {

    }

    public QueryOptions(int size) {
        super(size);
    }

    public QueryOptions(final String key, final Object value) {
        super(key, value);
    }

    public QueryOptions(final Map<String, Object> inputOptions) {
        super(inputOptions);
    }

    public QueryOptions(final Map<String, ?> inputOptions, boolean pickFirstValue) {
        if (pickFirstValue) {
            for (Map.Entry<String, ?> option : inputOptions.entrySet()) {
                if (option.getValue() instanceof List) {
                    this.put(option.getKey(), ((List) option.getValue()).get(0));
                }
            }
        } else {
            this.putAll(inputOptions);
        }
    }

    /**
     * This method safely add new options. If the key already exists it does not overwrite the current value.
     * You can use put for overwritten the value.
     *
     * @param key
     * @param value
     * @return null if the key was not present, or the existing object if the key exists.
     */
    public Object add(String key, Object value) {
        if (!this.containsKey(key)) {
            this.put(key, value);
            return null;
        }
        return this.get(key);
    }

    /**
     * This method safely add a new Object to an exiting option which type is List.
     *
     * @param key
     * @param value
     * @return the list with the new Object inserted.
     */
    public Object addToListOption(String key, Object value) {
        if (key != null && !key.equals("")) {
            if (this.containsKey(key) && this.get(key) != null) {
                if (!(this.get(key) instanceof List)) { //If was not a list, getAsList returns an Unmodifiable List.
                    // Create new modifiable List with the content, and replace.
                    this.put(key, new ArrayList<>(this.getAsList(key)));
                }
                this.getList(key).add(value);
            } else {
                List<Object> list = new ArrayList<>();  //New List instead of "Arrays.asList" or "Collections.singletonList" to avoid unmodifiable list.
                list.add(value);
                this.put(key, list);
            }
            return this.getList(key);
        }
        return null;
    }
}
