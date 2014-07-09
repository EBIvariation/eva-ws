package uk.ac.ebi.variation.eva.lib.datastore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.opencb.datastore.core.QueryResult;
import uk.ac.ebi.variation.eva.lib.storage.metadata.ArchiveEvaproDBAdaptor;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
public class EvaproUtils {
    
    public static QueryResult count(DataSource ds, String table) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        QueryResult qr = null;
        try {
            conn = ds.getConnection();
            pstmt = conn.prepareStatement(String.format("select count(*) from %s", table));
            long start = System.currentTimeMillis();
            rs = pstmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            long end = System.currentTimeMillis();
            qr = new QueryResult(null, ((Long) (end - start)).intValue(), 1, 1, null, null, Arrays.asList(count));
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Logger.getLogger(ArchiveEvaproDBAdaptor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException ex) {
                    Logger.getLogger(ArchiveEvaproDBAdaptor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(ArchiveEvaproDBAdaptor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        return qr;
    }
}
