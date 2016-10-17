package uk.ac.ebi.eva.lib.extension;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

import javax.persistence.Tuple;
import java.io.Serializable;
import java.util.List;

/**
 * Created by jorizci on 03/10/16.
 */
@NoRepositoryBean
public interface ExtendedJpaRepositoryFunctions<T, ID extends Serializable> extends Repository<T, ID> {

    List<Tuple> groupCount(String columnName);

    List<Tuple> groupCount(String columnName, Specification<T> specification);

    List<Tuple> groupCount(String columnName, Specification<T> specification, Boolean orderAscendant);
}
