package uk.ac.ebi.eva.server.ws;

import uk.ac.ebi.eva.commons.core.models.VariantType;

import java.util.Map;
import java.util.Set;

public class VariantCoreInfo {
    private String chromosome;

    private long start;

    private long end;

    private String reference;

    private String alternate;

    private Set<String> ids;

    private VariantType type;

    private int length;

    private Map<String, Set<String>> hgvs;

    public VariantCoreInfo(String chromosome, long start, long end, String reference, String alternate, Set<String> ids,
                           VariantType type, int length, Map<String, Set<String>> hgvs) {
        this.chromosome = chromosome;
        this.start = start;
        this.end = end;
        this.reference = reference;
        this.alternate = alternate;
        this.ids = ids;
        this.type = type;
        this.length = length;
        this.hgvs = hgvs;
    }
}
