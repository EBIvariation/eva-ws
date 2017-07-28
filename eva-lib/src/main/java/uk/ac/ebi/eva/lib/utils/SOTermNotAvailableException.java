package uk.ac.ebi.eva.lib.utils;

public class SOTermNotAvailableException extends RuntimeException {
    public SOTermNotAvailableException(String soName) {
        super("SO term " + soName + " not available in class ConsequenceTypeMappings");
    }
}