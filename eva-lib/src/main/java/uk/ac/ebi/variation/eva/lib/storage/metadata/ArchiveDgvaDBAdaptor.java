package uk.ac.ebi.variation.eva.lib.storage.metadata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
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
public class ArchiveDgvaDBAdaptor  implements ArchiveDBAdaptor {

    private DataSource ds;

    public ArchiveDgvaDBAdaptor() throws NamingException {
        InitialContext cxt = new InitialContext();
        ds = (DataSource) cxt.lookup("java:/comp/env/jdbc/evapro");
    }

    @Override
    public QueryResult countStudies() {
        try {
            return EvaproUtils.count(ds, "dgva_study_mv");
        } catch (SQLException ex) {
            Logger.getLogger(VariantSourceEvaproDBAdaptor.class.getName()).log(Level.SEVERE, null, ex);
            QueryResult qr = new QueryResult();
            qr.setErrorMsg(ex.getMessage());
            return qr;
        }
    }

    @Override
    public QueryResult countStudiesPerSpecies(QueryOptions options) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        QueryResult qr = null;
        try {
            conn = ds.getConnection();
            
            // Get all species entries from the database
            String query = "SELECT * from dgva_organism_mv ORDER BY study_count DESC";
            pstmt = conn.prepareStatement(query);
            long start = System.currentTimeMillis();
            ResultSet rs = pstmt.executeQuery();
            
            pstmt = conn.prepareStatement(query);
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
            try {
                EvaproUtils.close(pstmt);
                EvaproUtils.close(conn);
            } catch (SQLException ex) {
                Logger.getLogger(ArchiveEvaproDBAdaptor.class.getName()).log(Level.SEVERE, null, ex);
                qr = new QueryResult();
                qr.setErrorMsg(ex.getMessage());
            }
        }
        
        return qr;
    }

    @Override
    public QueryResult countStudiesPerType(QueryOptions options) {
        StringBuilder query = new StringBuilder("select study_type, count(*) as COUNT from dgva_study_mv ");
        if (options.containsKey("species")) {
            query.append("where ");
            query.append(EvaproUtils.getInClause("common_name", options.getListAs("species", String.class)));
            query.append(" or ");
            query.append(EvaproUtils.getInClause("scientific_name", options.getListAs("species", String.class)));
        }
        query.append(" group by study_type order by COUNT desc");

        Connection conn = null;
        PreparedStatement pstmt = null;
        QueryResult qr = null;
        try {
            conn = ds.getConnection();
            pstmt = conn.prepareStatement(query.toString());
            long start = System.currentTimeMillis();
            ResultSet rs = pstmt.executeQuery();
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
            try {
                EvaproUtils.close(pstmt);
                EvaproUtils.close(conn);
            } catch (SQLException ex) {
                Logger.getLogger(ArchiveEvaproDBAdaptor.class.getName()).log(Level.SEVERE, null, ex);
                qr = new QueryResult();
                qr.setErrorMsg(ex.getMessage());
            }
        }

        return qr;
    }

    @Override
    public QueryResult countFiles() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public QueryResult countSpecies() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
}
