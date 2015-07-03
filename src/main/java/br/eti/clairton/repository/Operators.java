package br.eti.clairton.repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.validation.constraints.NotNull;

public enum Operators implements Operator {
	AND(new And()),
	OR(new Or());

	private final Operator operator;

	private Operators(Operator operator) {
		this.operator = operator;
	}

	public Operator getOperator() {
		return operator;
	}

	@Override
	public Predicate build(final @NotNull CriteriaBuilder cb, @NotNull final Expression<Boolean> x, @NotNull final Expression<Boolean> y) {
		return operator.build(cb, x, y);
	}
}
