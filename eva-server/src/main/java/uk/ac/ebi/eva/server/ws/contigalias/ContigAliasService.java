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
import uk.ac.ebi.eva.commons.core.models.Annotation;
import uk.ac.ebi.eva.commons.core.models.FeatureCoordinates;
import uk.ac.ebi.eva.commons.core.models.Region;
import uk.ac.ebi.eva.commons.core.models.contigalias.ContigAliasChromosome;
import uk.ac.ebi.eva.commons.core.models.contigalias.ContigAliasResponse;
import uk.ac.ebi.eva.commons.core.models.contigalias.ContigAliasTranslator;
import uk.ac.ebi.eva.commons.core.models.contigalias.ContigNamingConvention;
import uk.ac.ebi.eva.commons.core.models.ws.VariantWithSamplesAndAnnotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class ContigAliasService {

    public static final String CONTIG_ALIAS_CHROMOSOMES_GENBANK_ENDPOINT = "/v1/chromosomes/genbank/";

    public static final String CONTIG_ALIAS_CHROMOSOMES_REFSEQ_ENDPOINT = "/v1/chromosomes/refseq/";

    public static final String CONTIG_ALIAS_CHROMOSOMES_NAME_ENDPOINT = "/v1/chromosomes/name/";

    public static final String CONTIG_ALIAS_CHROMOSOMES_SEARCH_ENDPOINT = "/v1/search/chromosome/";

    private final RestTemplate restTemplate;

    private final String contigAliasUrl;

    public ContigAliasService(RestTemplate restTemplate, String contigAliasUrl) {
        this.restTemplate = restTemplate;
        this.contigAliasUrl = contigAliasUrl;
    }

    public List<VariantWithSamplesAndAnnotation> getVariantsWithTranslatedContig(
            List<VariantWithSamplesAndAnnotation> variantsList, ContigAliasChromosome contigAliasChromosome,
            ContigNamingConvention contigNamingConvention) {

        if (skipContigTranslation(contigNamingConvention)) {
            return variantsList;
        }

        String translatedContig = ContigAliasTranslator.getTranslatedContig(contigAliasChromosome, contigNamingConvention);

        List<VariantWithSamplesAndAnnotation> variantsListAfterTranslatedContig = new ArrayList<>();
        variantsList.forEach(variant -> variantsListAfterTranslatedContig.add(createVariantsWithNewContig(variant, translatedContig)));

        return variantsListAfterTranslatedContig;
    }

    public List<VariantWithSamplesAndAnnotation> getVariantsWithTranslatedContig(
            List<VariantWithSamplesAndAnnotation> variantsList, Map<Region, String> insdcRegionAndNameInOriginalNamingConventionMap) {
        List<VariantWithSamplesAndAnnotation> variantsListAfterTranslatedContig = new ArrayList<>();
        for (VariantWithSamplesAndAnnotation variant : variantsList) {
            String translatedContig = insdcRegionAndNameInOriginalNamingConventionMap.entrySet().stream()
                    .filter(entry -> {
                        Region region = entry.getKey();
                        if (variant.getChromosome().equals(region.getChromosome())
                                && variant.getStart() >= region.getStart()
                                && variant.getEnd() <= region.getEnd()) {
                            return true;
                        } else {
                            return false;
                        }
                    }).findFirst().map(Map.Entry::getValue).orElse(null);
            if (translatedContig == null || translatedContig.equals("")) {
                variantsListAfterTranslatedContig.add(variant);
            } else {
                variantsListAfterTranslatedContig.add(createVariantsWithNewContig(variant, translatedContig));
            }
        }
        return variantsListAfterTranslatedContig;
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

    public Annotation getAnnotationWithTranslatedContig(Annotation annotation,
                                                        ContigNamingConvention contigNamingConvention) {
        String translatedContig = translateContigFromInsdc(annotation.getChromosome(), contigNamingConvention);
        if (translatedContig.isEmpty()) {
            return annotation;
        } else {
            return new Annotation(translatedContig, annotation.getStart(), annotation.getEnd(),
                    annotation.getVepVersion(), annotation.getVepCacheVersion(), annotation.getXrefs(),
                    annotation.getConsequenceTypes());
        }
    }

    public String translateContigFromInsdc(String insdcContig, ContigNamingConvention contigNamingConvention) {
        if (contigNamingConvention == null) {
            return "";
        }
        String url = contigAliasUrl + CONTIG_ALIAS_CHROMOSOMES_GENBANK_ENDPOINT + insdcContig;
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

    public List<ContigAliasChromosome> searchChromosomeByName(String contigName, String assembly, ContigNamingConvention contigNamingConvention) {
        String url = contigAliasUrl + CONTIG_ALIAS_CHROMOSOMES_SEARCH_ENDPOINT + contigName;
        if ((assembly != null && !assembly.isEmpty()) && contigNamingConvention != null) {
            url += "?assemblyAccession=" + assembly + "&namingConvention=" + getNameParam(contigNamingConvention);
        } else if (assembly != null && !assembly.isEmpty()) {
            url += "?assemblyAccession=" + assembly;
        } else if (contigNamingConvention != null) {
            url += "?namingConvention=" + getNameParam(contigNamingConvention);
        }

        ContigAliasResponse contigAliasResponse = restTemplate.getForObject(url, ContigAliasResponse.class);
        if (contigAliasResponse == null || contigAliasResponse.getEmbedded() == null ||
                contigAliasResponse.getEmbedded().getContigAliasChromosomes() == null ||
                contigAliasResponse.getEmbedded().getContigAliasChromosomes().isEmpty()) {
            return null;
        }

        return contigAliasResponse.getEmbedded().getContigAliasChromosomes();
    }

    public ContigAliasChromosome getUniqueInsdcChromosomeByName(String contigName, String assembly, ContigNamingConvention contigNamingConvention) {
        List<ContigAliasChromosome> chromosomeList = searchChromosomeByName(contigName, assembly, contigNamingConvention);
        if (chromosomeList == null || chromosomeList.isEmpty()) {
            return null;
        }
        if (chromosomeList.size() == 1) {
            return chromosomeList.get(0);
        } else {
            Set<String> insdcAccessionsSet = chromosomeList.stream().map(cac -> cac.getInsdcAccession())
                    .collect(Collectors.toSet());
            if (insdcAccessionsSet.size() == 1) {
                return chromosomeList.get(0);
            } else {
                Set<ContigNamingConvention> contigNamingConventionSet = chromosomeList.stream()
                        .map(contigAliasChromosome -> getMatchingContigNamingConvention(contigAliasChromosome, contigName))
                        .collect(Collectors.toSet());
                throw new RuntimeException("Multiple Chromosomes found for " + contigName + " in assembly " + assembly
                        + " with contig naming conventions " + contigNamingConventionSet);
            }
        }
    }

    public ContigNamingConvention getMatchingContigNamingConvention(ContigAliasChromosome contigAliasChromosome, String chromosomeName) {
        if (chromosomeName == contigAliasChromosome.getInsdcAccession()) {
            return ContigNamingConvention.INSDC;
        } else if (chromosomeName == contigAliasChromosome.getGenbankSequenceName()) {
            return ContigNamingConvention.GENBANK_SEQUENCE_NAME;
        } else if (chromosomeName == contigAliasChromosome.getEnaSequenceName()) {
            return ContigNamingConvention.ENA_SEQUENCE_NAME;
        } else if (chromosomeName == contigAliasChromosome.getRefseq()) {
            return ContigNamingConvention.REFSEQ;
        } else if (chromosomeName == contigAliasChromosome.getUcscName()) {
            return ContigNamingConvention.UCSC;
        } else {
            return null;
        }
    }

    private String getNameParam(ContigNamingConvention contigNamingConvention) {
        switch (contigNamingConvention) {
            case INSDC:
                return "insdc";
            case REFSEQ:
                return "refseq";
            case UCSC:
                return "ucsc";
            case ENA_SEQUENCE_NAME:
                return "ena";
            case GENBANK_SEQUENCE_NAME:
                return "genbank";
            default:
                return "genbank";
        }
    }

}
