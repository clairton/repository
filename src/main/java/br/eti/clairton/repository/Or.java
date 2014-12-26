package br.eti.clairton.repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.validation.constraints.NotNull;

public class Or implements Operator {

	@Override
	public Predicate build(final @NotNull CriteriaBuilder cb,
			@NotNull final Expression<Boolean> x,
			@NotNull final Expression<Boolean> y) {
		return cb.or(x, y);
	}
}
