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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;
import org.opencb.opencga.storage.core.adaptors.ArchiveDBAdaptor;

import uk.ac.ebi.eva.lib.datastore.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.datastore.EvaproUtils;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
public class ArchiveDgvaDBAdaptor  implements ArchiveDBAdaptor {

    private DataSource ds;

    public ArchiveDgvaDBAdaptor() throws NamingException, IOException {
        InitialContext cxt = new InitialContext();
        Properties properties = new Properties(); 
        properties.load(DBAdaptorConnector.class.getResourceAsStream("/eva.properties"));
        String dsName = properties.getProperty("eva.evapro.datasource", "evapro");
        ds = (DataSource) cxt.lookup("java:/comp/env/jdbc/" + dsName);
    }

    @Override
    public QueryResult countStudies() {
        try {
            return EvaproUtils.count(ds, "dgva_study_browser");
        } catch (SQLException ex) {
            Logger.getLogger(ArchiveDgvaDBAdaptor.class.getName()).log(Level.SEVERE, null, ex);
            QueryResult qr = new QueryResult();
            qr.setErrorMsg(ex.getMessage());
            return qr;
        }
    }

    @Override
    public QueryResult countStudiesPerSpecies(QueryOptions options) {
        StringBuilder query = new StringBuilder("select common_name, count(*) as COUNT from dgva_study_browser ");
        appendSpeciesAndTypeFilters(query, options);
        query.append(" group by common_name order by COUNT desc");
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        QueryResult qr = null;
        try {
            conn = ds.getConnection();
            pstmt = conn.prepareStatement(query.toString());
            long start = System.currentTimeMillis();
            ResultSet rs = pstmt.executeQuery();
            
            List<Map.Entry<String, Integer>> result = new ArrayList<>();
            while (rs.next()) {
                String species = rs.getString(1) != null ? rs.getString(1) : "Others";
                int count = rs.getInt(2);
                result.add(new AbstractMap.SimpleEntry<>(species, count));
            }
            long end = System.currentTimeMillis();
            qr = new QueryResult(null, ((Long) (end - start)).intValue(), result.size(), result.size(), null, null, result);
        } catch (SQLException ex) {
            Logger.getLogger(ArchiveDgvaDBAdaptor.class.getName()).log(Level.SEVERE, null, ex);
            qr = new QueryResult();
            qr.setErrorMsg(ex.getMessage());
            return qr;
        } finally {
            try {
                EvaproUtils.close(pstmt);
                EvaproUtils.close(conn);
            } catch (SQLException ex) {
                Logger.getLogger(ArchiveDgvaDBAdaptor.class.getName()).log(Level.SEVERE, null, ex);
                qr = new QueryResult();
                qr.setErrorMsg(ex.getMessage());
            }
        }
        
        return qr;
    }

    @Override
    public QueryResult countStudiesPerType(QueryOptions options) {
        StringBuilder query = new StringBuilder("select study_type, count(*) as COUNT from dgva_study_browser ");
        appendSpeciesAndTypeFilters(query, options);
        query.append(" group by study_type order by COUNT desc");

        Connection conn = null;
        PreparedStatement pstmt = null;
        QueryResult qr = null;
        try {
            conn = ds.getConnection();
            pstmt = conn.prepareStatement(query.toString());
            long start = System.currentTimeMillis();
            ResultSet rs = pstmt.executeQuery();
            List<Map.Entry<String, Integer>> result = new ArrayList<>();
            while (rs.next()) {
                String type = rs.getString(1) != null ? rs.getString(1) : "Others";
                int count = rs.getInt(2);
                result.add(new AbstractMap.SimpleEntry<>(type, count));
            }
            long end = System.currentTimeMillis();
            qr = new QueryResult(null, ((Long) (end - start)).intValue(), result.size(), result.size(), null, null, result);
        } catch (SQLException ex) {
            Logger.getLogger(ArchiveDgvaDBAdaptor.class.getName()).log(Level.SEVERE, null, ex);
            qr = new QueryResult();
            qr.setErrorMsg(ex.getMessage());
            return qr;
        } finally {
            try {
                EvaproUtils.close(pstmt);
                EvaproUtils.close(conn);
            } catch (SQLException ex) {
                Logger.getLogger(ArchiveDgvaDBAdaptor.class.getName()).log(Level.SEVERE, null, ex);
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

    @Override
    public QueryResult getSpecies(String version, boolean loaded) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void appendSpeciesAndTypeFilters(StringBuilder query, QueryOptions options) {
        if (options.containsKey("species") || options.containsKey("type")) {
            query.append("where ");
        }
        
        if (options.containsKey("species")) {
            query.append("(");
            query.append(EvaproUtils.getInClause("common_name", options.getAsStringList("species")));
            query.append(" or ");
            query.append(EvaproUtils.getInClause("scientific_name", options.getAsStringList("species")));
            query.append(") ");
        }
        
        if (options.containsKey("species") && options.containsKey("type")) {
            query.append("and ");
        }
        
        if (options.containsKey("type")) {
            query.append("(");
            query.append(EvaproUtils.getInClause("study_type", options.getAsStringList("type")));
            for (String t : options.getAsStringList("type")) {
                query.append(" or study_type like '%").append(t).append("%'");
            }
            query.append(")");
        }
    }
    
}
