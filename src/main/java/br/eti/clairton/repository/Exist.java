package br.eti.clairton.repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Subquery;

public class Exist implements Operator {
	@Override
	public Predicate build(CriteriaBuilder cb, Expression<?> x, Object y) {
		return cb.exists((Subquery<?>) y);
	}

	@Override
	public String toString() {
		return "EXISTS";
	}
}
