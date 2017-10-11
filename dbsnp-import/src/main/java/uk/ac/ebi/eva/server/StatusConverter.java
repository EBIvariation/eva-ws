/*
 * Copyright 2017 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.eva.server;

import javax.persistence.AttributeConverter;

public class StatusConverter implements AttributeConverter<Status, String> {

    @Override
    public String convertToDatabaseColumn(Status attribute) {
        switch(attribute) {
            case pending:
                return "pending";
            case in_progress:
                return "in_progress";
            case done:
                return "done";
            default:
                throw new IllegalArgumentException("Unknown status value: " + attribute);
        }
    }

    @Override
    public Status convertToEntityAttribute(String dbData) {
        switch (dbData) {
            case "pending":
                return Status.pending;
            case "in_progress":
                return Status.in_progress;
            case "done":
                return Status.done;
            default:
                throw new IllegalArgumentException("Unknown status value " + dbData);
        }
    }
}
