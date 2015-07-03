package br.eti.clairton.repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public class EndsWith implements Comparator {
	private static final long serialVersionUID = 1L;

	@Override
	public Predicate build(final CriteriaBuilder cb, final Expression<?> x, final Object y) {
		@SuppressWarnings("unchecked")
		final Expression<String> s = (Expression<String>) x;
		return cb.like(cb.lower(s), "%" + y.toString().toLowerCase());
	}

	@Override
	public String toString() {
		return "*";
	}
}
