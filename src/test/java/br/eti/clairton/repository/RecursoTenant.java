package br.eti.clairton.repository;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Join;
import javax.validation.constraints.NotNull;

import br.eti.clairton.tenant.Tenant;
import br.eti.clairton.tenant.TenantBuilder;
import br.eti.clairton.tenant.TenantType;

@Dependent
@TenantType(Recurso.class)
public class RecursoTenant extends Tenant<Recurso> {
	@Inject
	public RecursoTenant(final TenantBuilder builder) {
		super(builder);
	}

	@Override
	public Predicate add(@NotNull final CriteriaBuilder criteriaBuilder,
			final @NotNull From<?, Recurso> from) {
		final Join<Recurso, Aplicacao> join = from.join(Recurso_.aplicacao);
		return builder.run(criteriaBuilder, join);
	}
}
