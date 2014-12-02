package br.eti.clairton.repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public class Equal implements Operator {
    @Override
    public Predicate build(CriteriaBuilder cb, Expression<?> x, Object y) {
        return cb.equal(x, y);
    }
    
    @Override
    public String toString() {
    	return "==";
    }    
}
