package br.eti.clairton.repository;

import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.validation.constraints.NotNull;

import br.eti.clairton.tenant.Tenant;
import br.eti.clairton.tenant.TenantBuilder;
import br.eti.clairton.tenant.TenantType;

@Dependent
@TenantType(Aplicacao.class)
public class AplicacaoTenant extends Tenant<Aplicacao> {

	@Inject
	public AplicacaoTenant(final TenantBuilder builder) {
		super(builder);
	}

	@Override
	public List<Predicate> add(@NotNull final CriteriaBuilder criteriaBuilder,
			final @NotNull From<?, Aplicacao> from,
			final @NotNull List<Predicate> appendTo) {
		final Path<String> path = from.get(Aplicacao_.nome);
		final String value = "OutroTesteQueNãoDeveAparecerNaConsulta";
		final Predicate predicate = criteriaBuilder.notEqual(path, value);
		appendTo.add(predicate);
		return appendTo;
	}
}
