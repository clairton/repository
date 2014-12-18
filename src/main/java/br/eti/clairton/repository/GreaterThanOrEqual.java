package br.eti.clairton.repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public class GreaterThanOrEqual implements Operator {
	private static final long serialVersionUID = 1L;
    @Override
    public Predicate build(CriteriaBuilder cb, Expression<?> x, Object y) {
        @SuppressWarnings("rawtypes")
        final Expression w = x;
        @SuppressWarnings("rawtypes")
        final Comparable k = ( Comparable ) y;
        @SuppressWarnings("unchecked")
        final Predicate p = cb.greaterThanOrEqualTo(w, k);
        return p;
    }
    
    @Override
    public String toString() {
        return ">=";
    }
}
