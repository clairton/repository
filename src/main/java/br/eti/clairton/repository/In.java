package br.eti.clairton.repository;

import java.util.Collection;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

/**
 * Cria o {@link Expression#in(java.util.Collection)}.
 * 
 * @author Clairton Rodrigo Heinzen<clairton.rodrigo@gmail.com>
 */
public class In implements Operator {
    /**
     * {@inheritDoc}
     */
    @Override
    public Predicate build(final CriteriaBuilder cb, final Expression<?> x, final Object y) {
        return x.in(( Collection<?> ) y);
    }
    
    @Override
    public String toString() {
        return "IN";
    }
}
