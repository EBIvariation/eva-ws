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

package uk.ac.ebi.eva.lib.datastore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.opencb.datastore.core.QueryResult;

import uk.ac.ebi.eva.lib.models.VariantStudy;
import uk.ac.ebi.eva.lib.storage.metadata.ArchiveEvaproDBAdaptor;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
public class EvaproUtils {
    
    public static QueryResult count(DataSource ds, String table) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        QueryResult qr = null;
        try {
            conn = ds.getConnection();
            pstmt = conn.prepareStatement(String.format("select count(*) from %s", table));
            long start = System.currentTimeMillis();
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            long end = System.currentTimeMillis();
            qr = new QueryResult(null, ((Long) (end - start)).intValue(), 1, 1, null, null, Arrays.asList(count));
        } finally {
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
    
    public static String getInClause(String field, List<String> values) {
        StringBuilder query = new StringBuilder(field).append(" in (");
        int i = 0;
        for (String s : values) {
            if (i > 0) {
                query.append(", ");
            }
            query.append("'").append(s).append("'");
            i++;
        }
        query.append(") ");
        return query.toString();
    }
    
    public static void close(PreparedStatement pstmt) throws SQLException {
        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (SQLException ex) {
                Logger.getLogger(ArchiveEvaproDBAdaptor.class.getName()).log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }
    
    public static void close(Connection conn) throws SQLException {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(ArchiveEvaproDBAdaptor.class.getName()).log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
    }
    
    
    public static VariantStudy.StudyType stringToStudyType(String studyType) {
        switch (studyType) {
            case "Collection":
            case "Curated Collection":
                return VariantStudy.StudyType.COLLECTION;
            case "Control Set":
            case "Control-Set":
                return VariantStudy.StudyType.CONTROL;
            case "Case Control":
            case "Case-Control":
                return VariantStudy.StudyType.CASE_CONTROL;
            case "Case Set":
            case "Case-Set":
                return VariantStudy.StudyType.CASE;
            case "Tumor vs. Matched-Normal":
                return VariantStudy.StudyType.PAIRED_TUMOR;
            case "Aggregate":
                return VariantStudy.StudyType.AGGREGATE;
            default:
                throw new IllegalArgumentException("Study type " + studyType + " is not valid");
        }
    }
    
    
    public static String studyTypeToString(VariantStudy.StudyType studyType) {
        switch (studyType) {
            case COLLECTION:
                return "Collection";
            case CONTROL:
                return "Control Set";
            case CASE_CONTROL:
                return "Case-Control";
            case CASE:
                return "Case-Set";
            case PAIRED:
                return "Tumor vs. Matched-Normal";
            default:
                StringBuilder lower = new StringBuilder(studyType.name().toLowerCase());
                lower.replace(0, 1, studyType.name().substring(0, 1)); // First letter uppercase
                return lower.toString();
        }
    }
}
