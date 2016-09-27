/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2014, 2015 EMBL - European Bioinformatics Institute
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

package uk.ac.ebi.eva.lib.storage.metadata;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.opencb.biodata.models.variant.stats.VariantSourceStats;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;
import org.opencb.opencga.storage.core.variant.adaptors.VariantSourceDBAdaptor;

import uk.ac.ebi.eva.lib.datastore.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.datastore.EvaproUtils;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
public class VariantSourceEvaproDBAdaptor implements VariantSourceDBAdaptor {

    private DataSource ds;
    private String evaVersion;

    public VariantSourceEvaproDBAdaptor() throws NamingException, IOException {
        InitialContext cxt = new InitialContext();
        Properties properties = new Properties(); 
        properties.load(DBAdaptorConnector.class.getResourceAsStream("/eva.properties"));
        String dsName = properties.getProperty("eva.evapro.datasource", "evapro");
        this.ds = (DataSource) cxt.lookup("java:/comp/env/jdbc/" + dsName);
        this.evaVersion = properties.getProperty("eva.version");
    }
    
    @Override
    public QueryResult countSources() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        QueryResult qr = null;
        try {
            conn = ds.getConnection();
            pstmt = conn.prepareStatement("select count(*) from file where file_type in (\"vcf\", \"vcf_aggregate\")");
            
            long start = System.currentTimeMillis();
            ResultSet rs = pstmt.executeQuery();
            int count = rs.next() ? rs.getInt(1) : 0;
            long end = System.currentTimeMillis();
            qr = new QueryResult(null, ((Long) (end - start)).intValue(), 1, 1, null, null, Arrays.asList(count));
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
    public QueryResult getAllSources(QueryOptions options) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public QueryResult getAllSourcesByStudyId(String studyId, QueryOptions options) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public QueryResult getAllSourcesByStudyIds(List<String> studyIds, QueryOptions options) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public QueryResult getSamplesBySource(String fileId, QueryOptions options) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public QueryResult getSamplesBySources(List<String> fileIds, QueryOptions options) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public QueryResult getSourceDownloadUrlById(String fileId, String studyId) {
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
            pstmt = conn.prepareStatement(
                    "select distinct bf.filename, f.ftp_file " +
                    "from browsable_file bf " + 
                    "left join file f on bf.file_id = f.file_id " +
                    "where bf.filename = ?"
            );
            pstmt.setString(1, filename);
            System.out.println(pstmt);
            long start = System.currentTimeMillis();
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                URL url = new URL("ftp:/" + rs.getString(2));
                long end = System.currentTimeMillis();
                qr = new QueryResult(rs.getString(1), ((Long) (end - start)).intValue(), 1, 1, null, null, Arrays.asList(url));
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
    public List<QueryResult> getSourceDownloadUrlByName(List<String> filenames) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<QueryResult> results = new ArrayList<>();
        String query = 
                "select distinct bf.filename, f.ftp_file " +
                "from browsable_file bf " +
                "left join file f on bf.file_id = f.file_id " +
                "where " + EvaproUtils.getInClause("bf.filename", filenames);
        
        try {
            conn = ds.getConnection();
            pstmt = conn.prepareStatement(query);
            System.out.println(pstmt);
            long start = System.currentTimeMillis();
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                results.add(new QueryResult(rs.getString(1), ((Long) (System.currentTimeMillis() - start)).intValue(), 
                        1, 1, null, null, Arrays.asList(new URL("ftp:/" + rs.getString(2)))));
            }
            
        } catch (SQLException | MalformedURLException ex) {
            Logger.getLogger(VariantSourceEvaproDBAdaptor.class.getName()).log(Level.SEVERE, null, ex);
            QueryResult qr = new QueryResult();
            qr.setErrorMsg(ex.getMessage());
            results.add(qr);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Logger.getLogger(ArchiveEvaproDBAdaptor.class.getName()).log(Level.SEVERE, null, ex);
                    QueryResult qr = new QueryResult();
                    qr.setErrorMsg(ex.getMessage());
                    results.add(qr);
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException ex) {
                    Logger.getLogger(ArchiveEvaproDBAdaptor.class.getName()).log(Level.SEVERE, null, ex);
                    QueryResult qr = new QueryResult();
                    qr.setErrorMsg(ex.getMessage());
                    results.add(qr);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(ArchiveEvaproDBAdaptor.class.getName()).log(Level.SEVERE, null, ex);
                    QueryResult qr = new QueryResult();
                    qr.setErrorMsg(ex.getMessage());
                    results.add(qr);
                }
            }
        }
        
        return results;
    }
    
    @Override
    public QueryResult updateSourceStats(VariantSourceStats variantSourceStats, QueryOptions queryOptions) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean close() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
