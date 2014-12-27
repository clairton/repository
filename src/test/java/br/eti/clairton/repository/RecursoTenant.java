package br.eti.clairton.repository;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.validation.constraints.NotNull;
import javax.persistence.criteria.Predicate;

@Dependent
@TenantTyped(Recurso.class)
public class RecursoTenant extends Tenant<Recurso> {
	@Inject
	public RecursoTenant(final TenantBuilder builder) {
		super(builder);
	}

	@Override
	public Predicate build(final @NotNull From<?, Recurso> from,
			final @NotNull CriteriaBuilder criteriaBuilder) {
		final Join<Recurso, Aplicacao> join = from.join(Recurso_.aplicacao);
		return builder.add(join, criteriaBuilder);
	}
}
