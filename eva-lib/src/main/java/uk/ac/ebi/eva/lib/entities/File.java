/*
 * Copyright 2014-2017 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.eva.lib.entities;

import uk.ac.ebi.eva.lib.models.FileFtpReference;

import javax.persistence.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                    "where bf.filename in :filenames",
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

    @OneToMany(mappedBy = "fileSamplePK.fileId")
    private List<FileSample> fileSamples;

    public File() {
    }

    public File(Long fileId, String enaSubmissionFileId, String filename, String fileMd5, String fileLocation,
                String fileType, String fileClass, int fileVersion, boolean isCurrent, String ftpFile,
                boolean mongoLoadStatus, String evaSubmissionFileId) {
        this.fileId = fileId;
        this.enaSubmissionFileId = enaSubmissionFileId;
        this.filename = filename;
        this.fileMd5 = fileMd5;
        this.fileLocation = fileLocation;
        this.fileType = fileType;
        this.fileClass = fileClass;
        this.fileVersion = fileVersion;
        this.isCurrent = isCurrent;
        this.ftpFile = ftpFile;
        this.mongoLoadStatus = mongoLoadStatus;
        this.evaSubmissionFileId = evaSubmissionFileId;
    }

    public String getFilename() {
        return filename;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public String getFileType() {
        return fileType;
    }

    public List<FileSample> getFileSamples() {
        return fileSamples;
    }

    public void setFileSamples(List<FileSample> fileSamples) {
        this.fileSamples = fileSamples;
    }

    public Map<String, Sample> getNameInFileToSampleMap() {
        // TODO is nameInFile unique per file?
        Map<String, Sample> nameInFileToSampleMap = new HashMap<>();
        for (FileSample fileSample : fileSamples) {
            nameInFileToSampleMap.put(fileSample.getNameInFile(), fileSample.getSample());
        }
        return nameInFileToSampleMap;
    }

}
