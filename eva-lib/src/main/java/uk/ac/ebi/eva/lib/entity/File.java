package uk.ac.ebi.eva.lib.entity;

import uk.ac.ebi.eva.lib.result.FileFtpReference;

import javax.persistence.*;

/**
 * Created by jorizci on 03/10/16.
 */
@Entity
@SqlResultSetMappings({
    @SqlResultSetMapping(
            name = "fileFtpReference",
            classes = @ConstructorResult(
                    targetClass = FileFtpReference.class,
                    columns = {
                        @ColumnResult(name = "filename", type = String.class),
                        @ColumnResult(name= "ftp_file", type = String.class)
                    }
            )
    )
})
@NamedNativeQueries({
    @NamedNativeQuery(
            name = "File.getFileFtpReferenceByFilename",
            query = "select distinct bf.filename, f.ftp_file " +
                    "from browsable_file bf " +
                    "left join file f on bf.file_id = f.file_id " +
                    "where bf.filename = :filename",
            resultSetMapping = "fileFtpReference"
    ),
    @NamedNativeQuery(
            name = "File.getFileFtpReferenceByNames",
            query = "select distinct bf.filename, f.ftp_file " +
                    "from browsable_file bf "+
                    "left join file f on bf.file_id = f.file_id "+
                    "where bf.dilename in :filenames;",
            resultSetMapping = "fileFtpReference"
    )
})
@Table(name = "file")
public class File {

    @Id
    @Column(name = "file_id")
    private Long fileId;

    @Column(length = 45, name="ena_submission_file_id")
    private String enaSubmissionFileId;

    @Column(length = 250)
    private String filename;

    @Column(length = 250, name = "file_md5")
    private String fileMd5;

    @Column(length = 250, name="file_location")
    private String fileLocation;

    @Column(length = 250, name="file_type", nullable = false)
    private String fileType;

    @Column(length = 250, name="file_class", nullable = false)
    private String fileClass;

    @Column(name = "file_version")
    private int fileVersion;

    @Column(name = "is_current")
    private boolean isCurrent;

    @Column(length = 250, name="ftp_file")
    private String ftpFile;

    @Column(name = "mongo_load_status")
    private boolean mongoLoadStatus;

    @Column(length = 15, name = "eva_submission_file_id")
    private String evaSubmissionFileId;

}
