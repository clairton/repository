package br.eti.clairton.repository;

import static br.eti.clairton.repository.Recurso_.aplicacao;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.validation.constraints.NotNull;

import br.eti.clairton.tenant.TenantBuilder;
import br.eti.clairton.tenant.TenantType;
import br.eti.clairton.tenant.Tenantable;

@Dependent
@TenantType(Recurso.class)
public class RecursoTenant extends Tenantable<Recurso> {
	private final TenantBuilder builder;

	@Inject
	public RecursoTenant(final TenantBuilder builder) {
		this.builder = builder;
	}

	@Override
	public Predicate add(
			final @NotNull CriteriaBuilder builder,
			final @NotNull From<?, Recurso> from,
			final @NotNull Object value) {
		final Join<Recurso, Aplicacao> join = from.join(aplicacao);
		return this.builder.run(builder, join, value);
	}
}
