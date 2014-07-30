package uk.ac.ebi.variation.eva.lib.storage.metadata;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;
import org.opencb.opencga.storage.variant.VariantSourceDBAdaptor;
import uk.ac.ebi.variation.eva.lib.datastore.EvaproUtils;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
public class VariantSourceEvaproDBAdaptor implements VariantSourceDBAdaptor {

    private DataSource ds;

    public VariantSourceEvaproDBAdaptor() throws NamingException {
        InitialContext cxt = new InitialContext();
        ds = (DataSource) cxt.lookup( "java:/comp/env/jdbc/evapro" );
    }
    
    @Override
    public QueryResult countSources() {
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
    public QueryResult getAllSources(QueryOptions options) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public QueryResult getAllSourcesByStudyId(String studyId, QueryOptions options) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public QueryResult getSamplesBySource(String fileId, String studyId, QueryOptions options) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public QueryResult getSourceDownloadUrlByName(String filename) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        QueryResult qr = null;
        try {
            conn = ds.getConnection();
            pstmt = conn.prepareStatement("select distinct(FTP_FILE) from FILE where FILENAME = ?");
            pstmt.setString(1, filename);
            System.out.println(pstmt);
            long start = System.currentTimeMillis();
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                URL url = new URL("ftp:/" + rs.getString(1));
                long end = System.currentTimeMillis();
                qr = new QueryResult(null, ((Long) (end - start)).intValue(), 1, 1, null, null, Arrays.asList(url));
            } else {
                long end = System.currentTimeMillis();
                qr = new QueryResult(null, ((Long) (end - start)).intValue(), 0, 0, null, null, new ArrayList<>());
            }
        } catch (SQLException | MalformedURLException ex) {
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
    public QueryResult getSourceDownloadUrlById(String fileId, String studyId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean close() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
