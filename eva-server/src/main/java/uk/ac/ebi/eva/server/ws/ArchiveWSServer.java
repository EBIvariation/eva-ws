/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2014-2017 EMBL - European Bioinformatics Institute
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

package uk.ac.ebi.eva.server.ws;

import io.swagger.annotations.Api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.ebi.eva.commons.mongodb.entities.projections.VariantStudySummary;
import uk.ac.ebi.eva.commons.mongodb.services.VariantStudySummaryService;

import uk.ac.ebi.eva.lib.metadata.eva.ArchiveEvaproDBAdaptor;
import uk.ac.ebi.eva.lib.metadata.eva.StudyEvaproDBAdaptor;
import uk.ac.ebi.eva.lib.metadata.shared.ArchiveWSServerHelper;
import uk.ac.ebi.eva.lib.eva_utils.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.eva_utils.MultiMongoDbFactory;
import uk.ac.ebi.eva.lib.utils.QueryResponse;
import uk.ac.ebi.eva.lib.utils.QueryResult;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(value = "/v1/meta", produces = "application/json")
@Api(tags = {"archive"})
public class ArchiveWSServer extends EvaWSServer {

    @Autowired
    private ArchiveEvaproDBAdaptor archiveEvaproDbAdaptor;

    @Autowired
    private StudyEvaproDBAdaptor studyEvaproDbAdaptor;

    @Autowired
    private VariantStudySummaryService variantStudySummaryService;

    private ArchiveWSServerHelper archiveWSServerHelper;


    public ArchiveWSServer() {
        archiveWSServerHelper = new ArchiveWSServerHelper();
    }

    @RequestMapping(value = "/files/count", method = RequestMethod.GET)
    public QueryResponse countFiles() {
        return setQueryResponse(archiveEvaproDbAdaptor.countFiles());
    }

    @RequestMapping(value = "/species/count", method = RequestMethod.GET)
    public QueryResponse countSpecies() {
        return setQueryResponse(archiveEvaproDbAdaptor.countSpecies());
    }

    @RequestMapping(value = "/species/list", method = RequestMethod.GET)
    public QueryResponse getBrowsableSpecies() {
        return setQueryResponse(archiveEvaproDbAdaptor.getBrowsableSpecies());
    }

    @RequestMapping(value = "/species/accessioned", method = RequestMethod.GET)
    public QueryResponse getAccessionedSpecies() {
        return setQueryResponse(archiveEvaproDbAdaptor.getAccessionedSpecies());
    }

    @RequestMapping(value = "/studies/count", method = RequestMethod.GET)
    public QueryResponse countStudies() {
        return setQueryResponse(archiveEvaproDbAdaptor.countStudies());
    }

    @RequestMapping(value = "/studies/all", method = RequestMethod.GET)
    public QueryResponse getStudies(@RequestParam(name = "species", required = false) List<String> species,
                                    @RequestParam(name = "type", required = false) List<String> types) {
        return archiveWSServerHelper.getStudies(species, types, queryUtils, studyEvaproDbAdaptor, version);
    }

    @RequestMapping(value = "/studies/list", method = RequestMethod.GET)
    public QueryResponse getBrowsableStudies(@RequestParam(name = "species") String species,
                                             @RequestParam(name = "fromDate", required = false)
                                                    @DateTimeFormat(pattern="yyyy-MM-dd") Date date)
            throws IOException {
        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName(species));
        List<VariantStudySummary> uniqueStudies;
        if (date == null) {
            uniqueStudies = variantStudySummaryService.findAll();
        } else {
            uniqueStudies = variantStudySummaryService.findByFromDate(date);
        }
        QueryResult<VariantStudySummary> result = buildQueryResult(uniqueStudies);
        return setQueryResponse(result);
    }

    @RequestMapping(value = "/studies/stats", method = RequestMethod.GET)
    public QueryResponse getStudiesStats(@RequestParam(name = "species", required = false) List<String> species,
                                         @RequestParam(name = "type", required = false) List<String> types) {
        return archiveWSServerHelper.getStudiesStats(species, types, queryUtils, archiveEvaproDbAdaptor, version);
    }
}
