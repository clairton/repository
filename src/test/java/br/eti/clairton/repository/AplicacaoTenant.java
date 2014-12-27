package br.eti.clairton.repository;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.validation.constraints.NotNull;

@Dependent
@TenantTyped(Aplicacao.class)
public class AplicacaoTenant extends Tenant<Aplicacao> {

	@Inject
	public AplicacaoTenant(final TenantBuilder builder) {
		super(builder);
	}

	@Override
	public Predicate build(final @NotNull From<?, Aplicacao> from,
			final @NotNull CriteriaBuilder criteriaBuilder) {
		final Path<String> path = from.get(Aplicacao_.nome);
		return criteriaBuilder.notEqual(path,
				"OutroTesteQueNãoDeveAparecerNaConsulta");
	}
}
