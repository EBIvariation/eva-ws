/*
 *
 * Copyright 2024 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package uk.ac.ebi.eva.server.ws.contigalias;

import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.eva.commons.core.models.FeatureCoordinates;
import uk.ac.ebi.eva.commons.core.models.contigalias.ContigAliasResponse;
import uk.ac.ebi.eva.commons.core.models.contigalias.ContigAliasTranslator;
import uk.ac.ebi.eva.commons.core.models.contigalias.ContigNamingConvention;
import uk.ac.ebi.eva.commons.core.models.ws.VariantWithSamplesAndAnnotation;

import java.util.ArrayList;
import java.util.List;


public class ContigAliasService {

    public static final String CONTIG_ALIAS_CHROMOSOMES_GENBANK_ENDPOINT = "/v1/chromosomes/genbank/";

    public static final String CONTIG_ALIAS_CHROMOSOMES_REFSEQ_ENDPOINT = "/v1/chromosomes/refseq/";

    public static final String CONTIG_ALIAS_CHROMOSOMES_NAME_ENDPOINT = "/v1/chromosomes/name/";

    private final RestTemplate restTemplate;

    private final String contigAliasUrl;

    public ContigAliasService(RestTemplate restTemplate, String contigAliasUrl) {
        this.restTemplate = restTemplate;
        this.contigAliasUrl = contigAliasUrl;
    }

    public List<VariantWithSamplesAndAnnotation> getVariantsWithTranslatedContig(
            List<VariantWithSamplesAndAnnotation> variantsList, ContigNamingConvention contigNamingConvention) {

        if (skipContigTranslation(contigNamingConvention)) {
            return variantsList;
        }

        List<VariantWithSamplesAndAnnotation> variantsListAfterTranslatedContig = new ArrayList<>();
        for (VariantWithSamplesAndAnnotation variant : variantsList) {
            String translatedContig = translateContigFromInsdc(variant.getChromosome(), contigNamingConvention);
            if (translatedContig.equals("")) {
                variantsListAfterTranslatedContig.add(variant);
            } else {
                variantsListAfterTranslatedContig.add(createVariantsWithNewContig(variant, translatedContig));
            }
        }
        return variantsListAfterTranslatedContig;
    }

    public VariantWithSamplesAndAnnotation createVariantsWithNewContig(VariantWithSamplesAndAnnotation variant,
                                                                       String newContig) {
        VariantWithSamplesAndAnnotation variantWithNewContig = new VariantWithSamplesAndAnnotation(newContig,
                variant.getStart(), variant.getEnd(), variant.getReference(), variant.getAlternate(),
                variant.getMainId());
        variantWithNewContig.setAnnotation(variant.getAnnotation());
        variantWithNewContig.addSourceEntries(variant.getSourceEntries());

        return variantWithNewContig;
    }

    public List<FeatureCoordinates> getFeatureCoordinatesWithTranslatedContig(List<FeatureCoordinates> featuresList,
                                                          ContigNamingConvention contigNamingConvention) {
        if (skipContigTranslation(contigNamingConvention)) {
            return featuresList;
        }

        List<FeatureCoordinates> featureListTranslatedContig = new ArrayList<>();
        for (FeatureCoordinates feature : featuresList) {
            String translatedContig = translateContigFromInsdc(feature.getChromosome(), contigNamingConvention);
            if (translatedContig.equals("")) {
                featureListTranslatedContig.add(feature);
            } else {
                featureListTranslatedContig.add(new FeatureCoordinates(feature.getId(), feature.getName(),
                        feature.getFeature(), translatedContig, feature.getStart(), feature.getEnd()));
            }
        }

        return featureListTranslatedContig;
    }


    public String translateContigFromInsdc(String genbankContig, ContigNamingConvention contigNamingConvention) {
        if (contigNamingConvention == null) {
            return "";
        }
        String url = contigAliasUrl + CONTIG_ALIAS_CHROMOSOMES_GENBANK_ENDPOINT + genbankContig;
        ContigAliasResponse contigAliasResponse = restTemplate.getForObject(url, ContigAliasResponse.class);
        if (contigAliasResponse == null || contigAliasResponse.getEmbedded() == null) {
            return "";
        } else {
            return ContigAliasTranslator.getTranslatedContig(contigAliasResponse, contigNamingConvention);
        }
    }

    public boolean skipContigTranslation(ContigNamingConvention contigNamingConvention) {
        return contigNamingConvention == null ||
                contigNamingConvention.equals(ContigNamingConvention.INSDC) ||
                contigNamingConvention.equals(ContigNamingConvention.NO_REPLACEMENT);
    }

    public String translateContigToInsdc(String contig, String assembly, ContigNamingConvention contigNamingConvention) {
        if (skipContigTranslation(contigNamingConvention)) {
            return contig;
        }
        if (contigNamingConvention.equals(ContigNamingConvention.REFSEQ)) {
            return translateContigRefseqToInsdc(contig);
        } else {
            return translateContigNameToInsdc(contig, assembly, contigNamingConvention);
        }
    }

    private String translateContigRefseqToInsdc(String refseq) {
        String url = contigAliasUrl + CONTIG_ALIAS_CHROMOSOMES_REFSEQ_ENDPOINT + refseq;
        ContigAliasResponse contigAliasResponse = restTemplate.getForObject(url, ContigAliasResponse.class);
        if (contigAliasResponse == null || contigAliasResponse.getEmbedded() == null) {
            return "";
        }
        return ContigAliasTranslator.getTranslatedContig(contigAliasResponse, ContigNamingConvention.INSDC);
    }

    private String translateContigNameToInsdc(String contigName, String assembly, ContigNamingConvention contigNamingConvention) {
        String url = contigAliasUrl + CONTIG_ALIAS_CHROMOSOMES_NAME_ENDPOINT + contigName
                + "?accession=" + assembly + "&name=" + getNameParam(contigNamingConvention);
        ContigAliasResponse contigAliasResponse = restTemplate.getForObject(url, ContigAliasResponse.class);
        if (contigAliasResponse == null || contigAliasResponse.getEmbedded() == null) {
            return "";
        }
        return ContigAliasTranslator.getTranslatedContig(contigAliasResponse, ContigNamingConvention.INSDC);
    }

    private String getNameParam(ContigNamingConvention contigNamingConvention) {
        switch (contigNamingConvention) {
            case UCSC:
                return "ucsc";
            case ENA_SEQUENCE_NAME:
                return "ena";
            default:
                return "genbank";
        }
    }

}
