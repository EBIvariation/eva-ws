package uk.ac.ebi.eva.lib.extension;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Created by jorizci on 30/09/16.
 */
public class GenericSpecifications<T> {

    public static <T> Specification<T> isEqual(String attributeName, Object object) {
        return new Specification<T>(){

            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.equal(root.get(attributeName),object);
            }
        };
    }

    public static <T> Specification<T> in(String attributeName, Object ... objects) {
        return new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                return root.get(attributeName).in(objects);
            }
        };
    }

    public static <T> Specification<T> like(String attributeName, String pattern) {
        return new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.like(root.get(attributeName),pattern);
            }
        };
    }
}
