package br.eti.clairton.repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public class NotStartsWith extends StartsWith  implements Comparator {
	private static final long serialVersionUID = 1L;

	@Override
	public Predicate build(final CriteriaBuilder cb, final Expression<?> x, final Object y) {
		final Predicate predicate = super.build(cb, x, y);
		final Predicate negate = cb.not(predicate);
		return negate;
	}

	@Override
	public String toString() {
		return "!^";
	}
}
