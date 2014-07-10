package uk.ac.ebi.variation.eva.lib.storage.metadata;

import java.net.MalformedURLException;
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

/**
 *
 * @author arklad
 */
public class StudyEvaproDBAdaptor implements StudyDBAdaptor {

    private DataSource ds;

    public StudyEvaproDBAdaptor() throws NamingException {
        InitialContext cxt = new InitialContext();
        ds = (DataSource) cxt.lookup( "java:/comp/env/jdbc/evapro" );
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
        ResultSet rs = null;
        QueryResult qr = null;
        try {
            conn = ds.getConnection();
            pstmt = conn.prepareStatement(
                    "select PROJECT.PROJECT_ACCESSION, DESCRIPTION, SPECIES_LATIN_NAME, SCOPE, MATERIAL, TYPE " + 
                    "from PROJECT, PROJECT_TAXONOMY, TAXONOMY " + 
                    "where PROJECT.PROJECT_ACCESSION = ? and " + 
                    "PROJECT.PROJECT_ACCESSION = PROJECT_TAXONOMY.PROJECT_ACCESSION and PROJECT_TAXONOMY.TAXONOMY_ID = TAXONOMY.TAXONOMY_ID");
            pstmt.setString(1, studyId);
            long start = System.currentTimeMillis();
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                VariantStudy study = new VariantStudy(null, rs.getString(1));
                study.setDescription(rs.getString(2));
                study.setSpecies(rs.getString(3));
                study.setScope(rs.getString(4));
                study.setMaterial(rs.getString(5));
                study.setType(rs.getString(6));
                long end = System.currentTimeMillis();
                List l = new ArrayList<>(); l.add(study);
                qr = new QueryResult(null, ((Long) (end - start)).intValue(), 0, 0, null, null, l);
            } else {
                long end = System.currentTimeMillis();
                qr = new QueryResult(null, ((Long) (end - start)).intValue(), 0, 0, null, null, new ArrayList<>());
            }
        } catch (SQLException ex) {
            Logger.getLogger(VariantSourceEvaproDBAdaptor.class.getName()).log(Level.SEVERE, null, ex);
            qr = new QueryResult();
            qr.setErrorMsg(ex.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Logger.getLogger(ArchiveEvaproDBAdaptor.class.getName()).log(Level.SEVERE, null, ex);
                    qr = new QueryResult();
                    qr.setErrorMsg(ex.getMessage());
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException ex) {
                    Logger.getLogger(ArchiveEvaproDBAdaptor.class.getName()).log(Level.SEVERE, null, ex);
                    qr = new QueryResult();
                    qr.setErrorMsg(ex.getMessage());
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(ArchiveEvaproDBAdaptor.class.getName()).log(Level.SEVERE, null, ex);
                    qr = new QueryResult();
                    qr.setErrorMsg(ex.getMessage());
                }
            }
        }
        
        return qr;
    }

    @Override
    public boolean close() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
