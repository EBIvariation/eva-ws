package uk.ac.ebi.eva.lib.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.eva.lib.repositories.TaxonomyRepository;

import java.util.Optional;

@Component
public class TaxonomyUtils {
    @Autowired
    private TaxonomyRepository taxonomyRepository;

    public Optional<String> getAssemblyAccessionForAssemblyCode(String assemblyCode) {
        return taxonomyRepository.getBrowsableSpecies().stream()
                .filter(t -> t.getAssemblyCode().equals(assemblyCode))
                .map(t -> t.getAssemblyAccession())
                .findFirst();
    }

}
