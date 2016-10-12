package uk.ac.ebi.variation.eva.lib.spring.data.ResultClasses;

/**
 * Created by jorizci on 04/10/16.
 */
public class FileFtpReference {

    private final String filename;
    private final String file_ftp;

    public FileFtpReference(String filename, String file_ftp) {
        this.filename = filename;
        this.file_ftp = file_ftp;
    }

    public String getFilename() {
        return filename;
    }

    public String getFile_ftp() {
        return file_ftp;
    }
}
