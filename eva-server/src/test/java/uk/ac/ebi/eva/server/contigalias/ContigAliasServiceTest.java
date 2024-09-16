package uk.ac.ebi.eva.server.contigalias;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.eva.commons.core.models.Annotation;
import uk.ac.ebi.eva.commons.core.models.FeatureCoordinates;
import uk.ac.ebi.eva.commons.core.models.contigalias.ContigAliasChromosome;
import uk.ac.ebi.eva.commons.core.models.contigalias.ContigAliasEmbedded;
import uk.ac.ebi.eva.commons.core.models.contigalias.ContigAliasResponse;
import uk.ac.ebi.eva.commons.core.models.contigalias.ContigNamingConvention;
import uk.ac.ebi.eva.commons.core.models.ws.VariantWithSamplesAndAnnotation;
import uk.ac.ebi.eva.server.ws.contigalias.ContigAliasService;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

public class ContigAliasServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ContigAliasService contigAliasService;

    private final String contigAliasUrl = "http://example.com";

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        contigAliasService = new ContigAliasService(restTemplate, contigAliasUrl);
    }

    @Test
    public void testSkipContigTranslation() {
        assertTrue(contigAliasService.skipContigTranslation(null));
        assertTrue(contigAliasService.skipContigTranslation(ContigNamingConvention.INSDC));
        assertTrue(contigAliasService.skipContigTranslation(ContigNamingConvention.NO_REPLACEMENT));
        assertFalse(contigAliasService.skipContigTranslation(ContigNamingConvention.UCSC));
        assertFalse(contigAliasService.skipContigTranslation(ContigNamingConvention.ENA_SEQUENCE_NAME));
        assertFalse(contigAliasService.skipContigTranslation(ContigNamingConvention.REFSEQ));
    }


    @Test
    public void testGetVariantsWithTranslatedContig() {
        List<VariantWithSamplesAndAnnotation> variantsList = Arrays.asList(new VariantWithSamplesAndAnnotation("1", 1000, 1005,
                "A", "T", "rs1"));

        // skip translation
        List<VariantWithSamplesAndAnnotation> result = contigAliasService
                .getVariantsWithTranslatedContig(variantsList, ContigNamingConvention.INSDC);
        assertEquals("1", result.get(0).getChromosome());

        // no translation
        when(restTemplate.getForObject(anyString(), eq(ContigAliasResponse.class)))
                .thenReturn(new ContigAliasResponse());
        result = contigAliasService
                .getVariantsWithTranslatedContig(variantsList, ContigNamingConvention.ENA_SEQUENCE_NAME);
        assertEquals("1", result.get(0).getChromosome());

        // with translation
        when(restTemplate.getForObject(anyString(), eq(ContigAliasResponse.class)))
                .thenReturn(getContigAliasResponse(ContigNamingConvention.ENA_SEQUENCE_NAME, "chr1"));
        result = contigAliasService
                .getVariantsWithTranslatedContig(variantsList, ContigNamingConvention.ENA_SEQUENCE_NAME);
        assertEquals("chr1", result.get(0).getChromosome());
    }

    @Test
    public void testGetFeatureCoordinatesWithTranslatedContig() {
        List<FeatureCoordinates> featureCoordinatesList = Arrays.asList(new FeatureCoordinates("id", "fbx02", "feature", "1", 0, 1));

        // skip translation
        List<FeatureCoordinates> result = contigAliasService
                .getFeatureCoordinatesWithTranslatedContig(featureCoordinatesList, ContigNamingConvention.INSDC);
        assertEquals("1", result.get(0).getChromosome());

        // no translation
        when(restTemplate.getForObject(anyString(), eq(ContigAliasResponse.class))).thenReturn(new ContigAliasResponse());
        result = contigAliasService.getFeatureCoordinatesWithTranslatedContig(featureCoordinatesList, ContigNamingConvention.ENA_SEQUENCE_NAME);
        assertEquals("1", result.get(0).getChromosome());

        // with translation
        when(restTemplate.getForObject(anyString(), eq(ContigAliasResponse.class)))
                .thenReturn(getContigAliasResponse(ContigNamingConvention.ENA_SEQUENCE_NAME, "chr1"));
        result = contigAliasService.getFeatureCoordinatesWithTranslatedContig(featureCoordinatesList, ContigNamingConvention.ENA_SEQUENCE_NAME);
        assertEquals("chr1", result.get(0).getChromosome());
    }

    @Test
    public void testGetAnnotationWithTranslatedContig() {
        Annotation annotation = new Annotation("1", 0, 0, null, null, null, null);

        // skip translation
        Annotation result = contigAliasService
                .getAnnotationWithTranslatedContig(annotation, ContigNamingConvention.INSDC);
        assertEquals("1", result.getChromosome());

        // no translation
        when(restTemplate.getForObject(anyString(), eq(ContigAliasResponse.class))).thenReturn(new ContigAliasResponse());
        result = contigAliasService.getAnnotationWithTranslatedContig(annotation, ContigNamingConvention.ENA_SEQUENCE_NAME);
        assertEquals("1", result.getChromosome());

        // with translation
        when(restTemplate.getForObject(anyString(), eq(ContigAliasResponse.class)))
                .thenReturn(getContigAliasResponse(ContigNamingConvention.ENA_SEQUENCE_NAME, "chr1"));
        result = contigAliasService.getAnnotationWithTranslatedContig(annotation, ContigNamingConvention.ENA_SEQUENCE_NAME);
        assertEquals("chr1", result.getChromosome());
    }

    @Test
    public void testTranslateContigFromInsdc() {
        // no translation
        when(restTemplate.getForObject(anyString(), eq(ContigAliasResponse.class))).thenReturn(new ContigAliasResponse());
        String translatedContig = contigAliasService.translateContigFromInsdc("1", ContigNamingConvention.ENA_SEQUENCE_NAME);
        assertEquals("", translatedContig);

        // with translation
        when(restTemplate.getForObject(anyString(), eq(ContigAliasResponse.class)))
                .thenReturn(getContigAliasResponse(ContigNamingConvention.ENA_SEQUENCE_NAME, "chr1"));
        translatedContig = contigAliasService.translateContigFromInsdc("1", ContigNamingConvention.ENA_SEQUENCE_NAME);
        assertEquals("chr1", translatedContig);

    }

    @Test
    public void testTranslateContigToInsdc() {
        // no translation
        when(restTemplate.getForObject(anyString(), eq(ContigAliasResponse.class))).thenReturn(new ContigAliasResponse());
        String translatedContig = contigAliasService.translateContigToInsdc("1", "asm1", null);
        assertEquals("1", translatedContig);

        // with refseq translation
        when(restTemplate.getForObject(anyString(), eq(ContigAliasResponse.class)))
                .thenReturn(getContigAliasResponse(ContigNamingConvention.INSDC, "chr1"));
        translatedContig = contigAliasService.translateContigToInsdc("1", "asm1", ContigNamingConvention.REFSEQ);
        assertEquals("chr1", translatedContig);

        // with ena_seq_name translation
        when(restTemplate.getForObject(anyString(), eq(ContigAliasResponse.class)))
                .thenReturn(getContigAliasResponse(ContigNamingConvention.INSDC, "chr1"));
        translatedContig = contigAliasService.translateContigToInsdc("1", "asm1", ContigNamingConvention.ENA_SEQUENCE_NAME);
        assertEquals("chr1", translatedContig);

    }


    private ContigAliasResponse getContigAliasResponse(ContigNamingConvention contigNamingConvention, String chromosome) {
        ContigAliasChromosome contigAliasChromosome = new ContigAliasChromosome();
        switch (contigNamingConvention) {
            case GENBANK_SEQUENCE_NAME:
                contigAliasChromosome.setGenbankSequenceName(chromosome);
                break;
            case REFSEQ:
                contigAliasChromosome.setRefseq(chromosome);
                break;
            case UCSC:
                contigAliasChromosome.setUcscName(chromosome);
                break;
            case ENA_SEQUENCE_NAME:
                contigAliasChromosome.setEnaSequenceName(chromosome);
                break;
            case INSDC:
                contigAliasChromosome.setInsdcAccession(chromosome);
                break;
        }

        ContigAliasEmbedded contigAliasEmbedded = new ContigAliasEmbedded();
        contigAliasEmbedded.setContigAliasChromosomes(Arrays.asList(contigAliasChromosome));

        ContigAliasResponse contigAliasResponse = new ContigAliasResponse();
        contigAliasResponse.setEmbedded(contigAliasEmbedded);

        return contigAliasResponse;
    }


}
