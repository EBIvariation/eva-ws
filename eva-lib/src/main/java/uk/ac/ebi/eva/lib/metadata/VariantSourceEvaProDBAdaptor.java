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
package uk.ac.ebi.eva.lib.metadata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.eva.commons.core.models.stats.VariantSourceStats;
import uk.ac.ebi.eva.lib.models.FileFtpReference;
import uk.ac.ebi.eva.lib.utils.QueryOptions;
import uk.ac.ebi.eva.lib.utils.QueryResult;
import uk.ac.ebi.eva.lib.repositories.FileRepository;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by jorizci on 04/10/16.
 */
@Component
public class VariantSourceEvaProDBAdaptor implements VariantSourceDBAdaptor {

    @Autowired
    private FileRepository fileRepository;

    @Override
    public QueryResult countSources() {
        long start = System.currentTimeMillis();
        long count = fileRepository.countByFileTypeIn(Arrays.asList("vcf", "vcf_aggregate"));
        long end = System.currentTimeMillis();
        return new QueryResult(null, ((Long) (end - start)).intValue(), 1, 1, null, null, Arrays.asList(count));
    }

    @Override
    public QueryResult getAllSources(QueryOptions queryOptions) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public QueryResult getAllSourcesByStudyId(String s, QueryOptions queryOptions) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public QueryResult getAllSourcesByStudyIds(List<String> list, QueryOptions queryOptions) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public QueryResult getSamplesBySource(String s, QueryOptions queryOptions) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public QueryResult getSamplesBySources(List<String> list, QueryOptions queryOptions) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public QueryResult getSourceDownloadUrlByName(String filename) {
        long start = System.currentTimeMillis();
        FileFtpReference fileFtpReference = fileRepository.getFileFtpReferenceByFilename(filename);

        try {
            long end = System.currentTimeMillis();
            URL url = new URL("ftp:/" + fileFtpReference.getFile_ftp());
            return new QueryResult(fileFtpReference.getFilename(), ((Long) (end - start)).intValue(), 1, 1, null, null, Arrays.asList(url));
        } catch (NullPointerException e) {
            long end = System.currentTimeMillis();
            return new QueryResult(null, ((Long) (end - start)).intValue(), 0, 0, null, null, new ArrayList<>());
        } catch (MalformedURLException ex) {
            Logger.getLogger(VariantSourceEvaProDBAdaptor.class.getName()).log(Level.SEVERE, null, ex);
            QueryResult qr = new QueryResult();
            qr.setErrorMsg(ex.getMessage());
            return qr;
        }
    }

    @Override
    public List<QueryResult> getSourceDownloadUrlByName(List<String> filenames) {
        long start = System.currentTimeMillis();
        List<QueryResult> results = new ArrayList<>();
        List<FileFtpReference> fileFtpReferences = fileRepository.getFileFtpReferenceByNames(filenames);

        for (FileFtpReference fileFtpReference : fileFtpReferences) {
            try {
                results.add(new QueryResult(fileFtpReference.getFilename(), ((Long) (System.currentTimeMillis() - start)).intValue(),
                        1, 1, null, null, Arrays.asList(new URL("ftp:/" + fileFtpReference.getFile_ftp()))));
            } catch (MalformedURLException ex) {
                Logger.getLogger(VariantSourceEvaProDBAdaptor.class.getName()).log(Level.SEVERE, null, ex);
                QueryResult qr = new QueryResult();
                qr.setErrorMsg(ex.getMessage());
                results.add(qr);
            }
        }
        return results;
    }

    @Override
    public QueryResult getSourceDownloadUrlById(String s, String s1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public QueryResult updateSourceStats(VariantSourceStats variantSourceStats, QueryOptions queryOptions) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean close() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
