package br.eti.clairton.repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.validation.constraints.NotNull;
import javax.persistence.criteria.Predicate;

public abstract class Tenant<T> {
	protected final TenantBuilder builder;

	public Tenant(final TenantBuilder builder) {
		super();
		this.builder = builder;
	}

	abstract Predicate build(@NotNull From<?, T> from,
			@NotNull CriteriaBuilder criteriaBuilder);
}
