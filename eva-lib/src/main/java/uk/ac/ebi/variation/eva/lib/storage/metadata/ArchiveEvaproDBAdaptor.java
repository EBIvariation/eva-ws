package uk.ac.ebi.variation.eva.lib.storage.metadata;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;
import org.opencb.opencga.storage.variant.ArchiveDBAdaptor;
import uk.ac.ebi.variation.eva.lib.datastore.EvaproUtils;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
public class ArchiveEvaproDBAdaptor implements ArchiveDBAdaptor {

    private DataSource ds;

    public ArchiveEvaproDBAdaptor() throws NamingException {
        InitialContext cxt = new InitialContext();
        ds = (DataSource) cxt.lookup( "java:/comp/env/jdbc/evapro" );
    }
    
    @Override
    public QueryResult countStudies() {
        try {
            return EvaproUtils.count(ds, "PROJECT");
        } catch (SQLException ex) {
            Logger.getLogger(VariantSourceEvaproDBAdaptor.class.getName()).log(Level.SEVERE, null, ex);
            QueryResult qr = new QueryResult();
            qr.setErrorMsg(ex.getMessage());
            return qr;
        }
    }

    @Override
    public QueryResult countStudiesPerSpecies(QueryOptions options) {
        StringBuilder query = new StringBuilder("select TAXONOMY.SPECIES_COMMON_NAME, count(*) as COUNT from PROJECT left join PROJECT_TAXONOMY on ")
                .append("PROJECT.PROJECT_ACCESSION = PROJECT_TAXONOMY.PROJECT_ACCESSION left join TAXONOMY on PROJECT_TAXONOMY.TAXONOMY_ID = TAXONOMY.TAXONOMY_ID ");
        if (options.containsKey("species")) {
            query.append("where TAXONOMY.SPECIES_COMMON_NAME in (");
            List<String> species = options.getListAs("species", String.class);
            int i = 0;
            for (String s : species) {
                if (i > 0) {
                    query.append(", ");
                }
                query.append("\"").append(s).append("\"");
                i++;
            }
            query.append(")");
        }
        query.append(" group by TAXONOMY.SPECIES_COMMON_NAME order by COUNT desc");
        
        QueryResult qr = null;
        ResultSet rs = null;
        try {
            long start = System.currentTimeMillis();
            rs = EvaproUtils.select(ds, query.toString());
            Map<String, Integer> result = new HashMap<>();
            while (rs.next()) {
                String species = rs.getString(1) != null ? rs.getString(1) : "Others";
                int count = rs.getInt(2);
                result.put(species, count);
            }
            long end = System.currentTimeMillis();
            qr = new QueryResult(null, ((Long) (end - start)).intValue(), result.size(), result.size(), null, null, new ArrayList(result.entrySet()));
        } catch (SQLException ex) {
            Logger.getLogger(VariantSourceEvaproDBAdaptor.class.getName()).log(Level.SEVERE, null, ex);
            qr = new QueryResult();
            qr.setErrorMsg(ex.getMessage());
            return qr;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Logger.getLogger(ArchiveEvaproDBAdaptor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        return qr;
    }
    
    @Override
    public QueryResult countStudiesPerType(QueryOptions options) {
        StringBuilder query = new StringBuilder("select TYPE, count(*) as COUNT from PROJECT left join PROJECT_TAXONOMY on ")
                .append("PROJECT.PROJECT_ACCESSION = PROJECT_TAXONOMY.PROJECT_ACCESSION left join TAXONOMY on PROJECT_TAXONOMY.TAXONOMY_ID = TAXONOMY.TAXONOMY_ID ");
        if (options.containsKey("species")) {
            query.append("where TAXONOMY.SPECIES_COMMON_NAME in (");
            List<String> species = options.getListAs("species", String.class);
            int i = 0;
            for (String s : species) {
                if (i > 0) {
                    query.append(", ");
                }
                query.append("\"").append(s).append("\"");
                i++;
            }
            query.append(")");
        }
        query.append(" group by TYPE order by COUNT desc");
        
        QueryResult qr = null;
        ResultSet rs = null;
        try {
            long start = System.currentTimeMillis();
            rs = EvaproUtils.select(ds, query.toString());
            Map<String, Integer> result = new HashMap<>();
            while (rs.next()) {
                String species = rs.getString(1) != null ? rs.getString(1) : "Others";
                int count = rs.getInt(2);
                result.put(species, count);
            }
            long end = System.currentTimeMillis();
            qr = new QueryResult(null, ((Long) (end - start)).intValue(), result.size(), result.size(), null, null, new ArrayList(result.entrySet()));
        } catch (SQLException ex) {
            Logger.getLogger(VariantSourceEvaproDBAdaptor.class.getName()).log(Level.SEVERE, null, ex);
            qr = new QueryResult();
            qr.setErrorMsg(ex.getMessage());
            return qr;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Logger.getLogger(ArchiveEvaproDBAdaptor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        return qr;
    }

    @Override
    public QueryResult countFiles() {
        try {
            return EvaproUtils.count(ds, "VCF_FILES");
        } catch (SQLException ex) {
            Logger.getLogger(VariantSourceEvaproDBAdaptor.class.getName()).log(Level.SEVERE, null, ex);
            QueryResult qr = new QueryResult();
            qr.setErrorMsg(ex.getMessage());
            return qr;
        }
    }

    @Override
    public QueryResult countSpecies() {
        try {
            return EvaproUtils.count(ds, "TAXONOMY");
        } catch (SQLException ex) {
            Logger.getLogger(VariantSourceEvaproDBAdaptor.class.getName()).log(Level.SEVERE, null, ex);
            QueryResult qr = new QueryResult();
            qr.setErrorMsg(ex.getMessage());
            return qr;
        }
    }

}
