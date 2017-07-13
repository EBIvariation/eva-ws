package uk.ac.ebi.eva.lib.metadata;

import uk.ac.ebi.eva.commons.core.models.stats.VariantSourceStats;
import uk.ac.ebi.eva.lib.utils.QueryOptions;
import uk.ac.ebi.eva.lib.utils.QueryResult;

import java.util.List;

public interface VariantSourceDBAdaptor {
    
    QueryResult countSources();

    QueryResult getAllSources(QueryOptions var1);

    QueryResult getAllSourcesByStudyId(String var1, QueryOptions var2);

    QueryResult getAllSourcesByStudyIds(List<String> var1, QueryOptions var2);

    QueryResult getSamplesBySource(String var1, QueryOptions var2);

    QueryResult getSamplesBySources(List<String> var1, QueryOptions var2);

    QueryResult getSourceDownloadUrlByName(String var1);

    List<QueryResult> getSourceDownloadUrlByName(List<String> var1);

    QueryResult getSourceDownloadUrlById(String var1, String var2);

    QueryResult updateSourceStats(VariantSourceStats var1, QueryOptions var2);

    boolean close();
    
}