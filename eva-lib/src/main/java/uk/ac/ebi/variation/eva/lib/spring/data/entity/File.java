package uk.ac.ebi.variation.eva.lib.spring.data.entity;

import uk.ac.ebi.variation.eva.lib.spring.data.ResultClasses.FileFtpReference;

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
    private Long file_id;

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

    public long getFile_id() {
        return file_id;
    }

    public void setFile_id(long file_id) {
        this.file_id = file_id;
    }

    public String getEnaSubmissionFileId() {
        return enaSubmissionFileId;
    }

    public void setEnaSubmissionFileId(String enaSubmissionFileId) {
        this.enaSubmissionFileId = enaSubmissionFileId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileClass() {
        return fileClass;
    }

    public void setFileClass(String fileClass) {
        this.fileClass = fileClass;
    }

    public int getFileVersion() {
        return fileVersion;
    }

    public void setFileVersion(int fileVersion) {
        this.fileVersion = fileVersion;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setCurrent(boolean current) {
        isCurrent = current;
    }

    public String getFtpFile() {
        return ftpFile;
    }

    public void setFtpFile(String ftpFile) {
        this.ftpFile = ftpFile;
    }

    public boolean isMongoLoadStatus() {
        return mongoLoadStatus;
    }

    public void setMongoLoadStatus(boolean mongoLoadStatus) {
        this.mongoLoadStatus = mongoLoadStatus;
    }

    public String getEvaSubmissionFileId() {
        return evaSubmissionFileId;
    }

    public void setEvaSubmissionFileId(String evaSubmissionFileId) {
        this.evaSubmissionFileId = evaSubmissionFileId;
    }
}
