package br.eti.clairton.repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.validation.constraints.NotNull;

public interface Operator {
	Predicate build(@NotNull final CriteriaBuilder cb,
			@NotNull final Expression<?> x, @NotNull final Object y);
}
