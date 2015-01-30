package uk.ac.ebi.variation.eva.lib.storage.metadata;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.opencb.biodata.models.variant.VariantStudy;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;
import org.opencb.opencga.storage.core.adaptors.StudyDBAdaptor;
import uk.ac.ebi.variation.eva.lib.datastore.EvaproUtils;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
public class StudyEvaproDBAdaptor implements StudyDBAdaptor {

    private DataSource ds;

    public StudyEvaproDBAdaptor() throws NamingException {
        InitialContext cxt = new InitialContext();
        ds = (DataSource) cxt.lookup("java:/comp/env/jdbc/evapro");
    }

    @Override
    public QueryResult getAllStudies(QueryOptions options) {
        StringBuilder query = new StringBuilder("select * from study_browser ");
        boolean hasSpecies = options.containsKey("species") && !options.getList("species").isEmpty();
        boolean hasType = options.containsKey("type") && !options.getList("type").isEmpty();
        if (hasSpecies || hasType) {
            query.append("where ");
        }
        if (hasSpecies) {
            query.append("(");
            query.append(EvaproUtils.getInClause("common_name", options.getListAs("species", String.class)));
            query.append(" or ");
            query.append(EvaproUtils.getInClause("scientific_name", options.getListAs("species", String.class)));
            query.append(")");
        }
        if (hasType) {
            if (hasSpecies) {
                query.append(" and ");
            }
            query.append(EvaproUtils.getInClause("experiment_type", options.getListAs("type", String.class)));
            for (String t : options.getListAs("type", String.class)) {
                query.append(" or experiment_type like '%").append(t).append("%'");
            }
        }
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        QueryResult qr = null;
        try {
            conn = ds.getConnection();
            pstmt = conn.prepareStatement(query.toString());

            long start = System.currentTimeMillis();
            ResultSet rs = pstmt.executeQuery();
            List result = new ArrayList<>();
            while (rs.next()) {
                // Convert the list of tax ids to integer values
                String[] taxIdStrings = rs.getString("tax_id").split(", ");
                int[] taxIds = new int[taxIdStrings.length];
                for (int i = 0; i < taxIdStrings.length; i++) {
                    taxIds[i] = Integer.parseInt(taxIdStrings[i]);
                }
                
                // Build the variant study object
                URI uri = null;
                try {
                    uri = new URI(rs.getString("resource"));
                } catch(URISyntaxException | SQLException | NullPointerException ex) {
                }
                VariantStudy study = new VariantStudy(rs.getString("project_title"), rs.getString("project_accession"), null, 
                        rs.getString("description"), taxIds, rs.getString("common_name"), rs.getString("scientific_name"), 
                        rs.getString("source_type"), rs.getString("center"), rs.getString("material"), rs.getString("scope"), 
                        null, rs.getString("experiment_type"), rs.getString("experiment_type_abbreviation"), 
                        rs.getString("assembly_name"), rs.getString("platform"), uri,
                        rs.getInt("variant_count"), rs.getInt("samples"));
                result.add(study);
            }
            long end = System.currentTimeMillis();
            qr = new QueryResult(null, ((Long) (end - start)).intValue(), result.size(), result.size(), null, null, result);
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
    public QueryResult listStudies() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public QueryResult findStudyNameOrStudyId(String studyId, QueryOptions options) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public QueryResult getStudyById(String studyId, QueryOptions options) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        QueryResult qr = null;
        try {
            conn = ds.getConnection();
            pstmt = conn.prepareStatement("select * from study_browser where project_accession = ?");
            pstmt.setString(1, studyId);
            
            List result = new ArrayList<>();
            long start = System.currentTimeMillis();
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Convert the list of tax ids to integer values
                String[] taxIdStrings = rs.getString("tax_id").split(", ");
                int[] taxIds = new int[taxIdStrings.length];
                for (int i = 0; i < taxIdStrings.length; i++) {
                    taxIds[i] = Integer.parseInt(taxIdStrings[i]);
                }
                
                // Build the variant study object
                URI uri = null;
                try {
                    uri = new URI(rs.getString("resource"));
                } catch(URISyntaxException | SQLException | NullPointerException ex) {
                }
                VariantStudy study = new VariantStudy(rs.getString("project_title"), rs.getString("project_accession"), null, 
                        rs.getString("description"), taxIds, rs.getString("common_name"), rs.getString("scientific_name"), 
                        rs.getString("source_type"), rs.getString("center"), rs.getString("material"), rs.getString("scope"), 
                        null, rs.getString("experiment_type"), rs.getString("experiment_type_abbreviation"), 
                        rs.getString("assembly_name"), rs.getString("platform"), uri,
                        rs.getInt("variant_count"), rs.getInt("samples"));
                result.add(study);
            }
            long end = System.currentTimeMillis();
            qr = new QueryResult(null, ((Long) (end - start)).intValue(), result.size(), result.size(), null, null, result);
        } catch (SQLException ex) {
            Logger.getLogger(VariantSourceEvaproDBAdaptor.class.getName()).log(Level.SEVERE, null, ex);
            qr = new QueryResult();
            qr.setErrorMsg(ex.getMessage());
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
    public boolean close() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
