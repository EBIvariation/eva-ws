package uk.ac.ebi.variation.eva.lib.storage.metadata;

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
import org.opencb.opencga.storage.variant.StudyDBAdaptor;
import uk.ac.ebi.variation.eva.lib.datastore.EvaproUtils;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
public class StudyDgvaDBAdaptor implements StudyDBAdaptor {

    private DataSource ds;

    public StudyDgvaDBAdaptor() throws NamingException {
        InitialContext cxt = new InitialContext();
        ds = (DataSource) cxt.lookup("java:/comp/env/jdbc/evapro");
    }

    @Override
    public QueryResult getAllStudies(QueryOptions options) {
        StringBuilder query = new StringBuilder("select * from dgva_study_browser ");
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
            query.append(EvaproUtils.getInClause("study_type", options.getListAs("type", String.class)));
        }
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        QueryResult qr = null;
        try {
            conn = ds.getConnection();
            pstmt = conn.prepareStatement(query.toString());

            List result = new ArrayList<>();
            long start = System.currentTimeMillis();
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                // Convert the list of tax ids to integer values
                String[] taxIdStrings = rs.getString("tax_id").split(", ");
                int[] taxIds = new int[taxIdStrings.length];
                for (int i = 0; i < taxIdStrings.length; i++) {
                    taxIds[i] = Integer.parseInt(taxIdStrings[i]);
                }
                
                // Build the variant study object
                VariantStudy study = new VariantStudy(rs.getString("display_name"), rs.getString("study_accession"), null, 
                        rs.getString("study_description"), taxIds, rs.getString("common_name"), rs.getString("scientific_name"), 
                        null, null, null, null, EvaproUtils.stringToStudyType(rs.getString("study_type")), rs.getString("analysis_type"), 
                        null, "GRCh38", rs.getString("platform_name"), rs.getInt("variant_count"), -1);
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
            pstmt = conn.prepareStatement("select * from dgva_study_browser where study_accession = ?");
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
                VariantStudy study = new VariantStudy(rs.getString("display_name"), rs.getString("study_accession"), null, 
                        rs.getString("study_description"), taxIds, rs.getString("common_name"), rs.getString("scientific_name"), 
                        null, null, null, null, EvaproUtils.stringToStudyType(rs.getString("study_type")), rs.getString("analysis_type"), 
                        null, "GRCh38", rs.getString("platform_name"), rs.getInt("variant_count"), -1);
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
