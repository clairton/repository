package br.eti.clairton.repository;

import javax.enterprise.inject.Vetoed;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;

import br.eti.clairton.tenant.TenantBuilder;
import br.eti.clairton.tenant.TenantNotFound;

@Vetoed
public class JoinnerTenant extends Joinner{
	private final TenantBuilder tenant;
	private Object value;

	public JoinnerTenant(
			final TenantBuilder tenant, 
			final CriteriaBuilder builder, 
			final From<?, ?> from,
			final Object value) {
		super(builder, from);
		this.tenant = tenant;
		this.value = value; 
	}
	
	@Override
	public javax.persistence.criteria.Predicate join(final Predicate predicate) {
		final javax.persistence.criteria.Predicate joinPredicate = super.join(predicate);
		try {
			final javax.persistence.criteria.Predicate tenantPredicate = tenant.run(builder, fromLast, value);
			final javax.persistence.criteria.Predicate completePredicate = builder.and(joinPredicate, tenantPredicate);
			return completePredicate;
		} catch (final TenantNotFound e) {
			return joinPredicate;
		}
	}
}
