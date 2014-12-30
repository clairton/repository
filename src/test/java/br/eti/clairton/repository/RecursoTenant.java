package br.eti.clairton.repository;

import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
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
	public List<Predicate> add(@NotNull final CriteriaBuilder criteriaBuilder,
			final @NotNull From<?, Recurso> from,
			final @NotNull List<Predicate> appendTo) {
		final Join<Recurso, Aplicacao> join = from.join(Recurso_.aplicacao);
		return builder.run(criteriaBuilder, join, appendTo);
	}
}
