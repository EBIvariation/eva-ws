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
 * @author arklad
 */
public class StudyEvaproDBAdaptor implements StudyDBAdaptor {

    private DataSource ds;

    public StudyEvaproDBAdaptor() throws NamingException {
        InitialContext cxt = new InitialContext();
        ds = (DataSource) cxt.lookup("java:/comp/env/jdbc/evapro");
    }

    @Override
    public QueryResult getAllStudies(QueryOptions options) {
        StringBuilder query = new StringBuilder("select TITLE, PROJECT.PROJECT_ACCESSION, DESCRIPTION, SPECIES_LATIN_NAME, SCOPE, MATERIAL, TYPE from PROJECT ")
                .append("left join PROJECT_TAXONOMY on PROJECT.PROJECT_ACCESSION = PROJECT_TAXONOMY.PROJECT_ACCESSION ")
                .append("left join TAXONOMY on PROJECT_TAXONOMY.TAXONOMY_ID = TAXONOMY.TAXONOMY_ID ");
        boolean hasSpecies = options.containsKey("species") && !options.getList("species").isEmpty();
        boolean hasType = options.containsKey("type") && !options.getList("type").isEmpty();
        if (hasSpecies || hasType) {
            query.append("where ");
        }
        if (hasSpecies) {
            query.append("(");
            query.append(EvaproUtils.getInClause("TAXONOMY.SPECIES_COMMON_NAME", options.getListAs("species", String.class)));
            query.append(" or ");
            query.append(EvaproUtils.getInClause("TAXONOMY.SPECIES_LATIN_NAME", options.getListAs("species", String.class)));
            query.append(")");
        }
        if (hasType) {
            if (hasSpecies) {
                query.append(" and ");
            }
            query.append(EvaproUtils.getInClause("PROJECT.TYPE", options.getListAs("type", String.class)));
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
                VariantStudy study = new VariantStudy(rs.getString(1), rs.getString(2));
                study.setDescription(rs.getString(3));
                study.setSpecies(rs.getString(4));
                study.setScope(rs.getString(5));
                study.setMaterial(rs.getString(6));
                study.setType(rs.getString(7));
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
            pstmt = conn.prepareStatement("select TITLE, PROJECT.PROJECT_ACCESSION, DESCRIPTION, SPECIES_LATIN_NAME, SCOPE, MATERIAL, TYPE "
                    + "from PROJECT, PROJECT_TAXONOMY, TAXONOMY "
                    + "where PROJECT.PROJECT_ACCESSION = ? and "
                    + "PROJECT.PROJECT_ACCESSION = PROJECT_TAXONOMY.PROJECT_ACCESSION and PROJECT_TAXONOMY.TAXONOMY_ID = TAXONOMY.TAXONOMY_ID");
            pstmt.setString(1, studyId);
            
            List l = new ArrayList<>();
            long start = System.currentTimeMillis();
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                VariantStudy study = new VariantStudy(rs.getString(1), rs.getString(2));
                study.setDescription(rs.getString(3));
                study.setSpecies(rs.getString(4));
                study.setScope(rs.getString(5));
                study.setMaterial(rs.getString(6));
                study.setType(rs.getString(7));
                l.add(study);
            }
            long end = System.currentTimeMillis();
            qr = new QueryResult(null, ((Long) (end - start)).intValue(), l.size(), l.size(), null, null, l);
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
