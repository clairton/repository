package br.eti.clairton.repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.validation.constraints.NotNull;

public interface Operator {

	Predicate build(final @NotNull CriteriaBuilder cb, @NotNull final Expression<Boolean> x, @NotNull final Expression<Boolean> y);
}
