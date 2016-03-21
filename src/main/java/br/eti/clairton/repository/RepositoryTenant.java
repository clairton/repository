package br.eti.clairton.repository;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.validation.constraints.NotNull;

import br.eti.clairton.tenant.TenantBuilder;
import br.eti.clairton.tenant.TenantNotFound;

/**
 * Devolve um Repository com o valor de tenant setado.
 * 
 * @author Clairton Rodrigo Heinzen<clairton.rodrigo@gmail.com>
 */
@Tenant
@RequestScoped
public class RepositoryTenant extends Repository {
	private static final long serialVersionUID = 1L;

	private Object tenantValue;

	private final TenantBuilder tenant;

	@Deprecated
	protected RepositoryTenant() {
		this(null, null, null);
	}

	@Inject
	public RepositoryTenant(
			@NotNull final EntityManager em,
			@NotNull final TenantBuilder tenant,
			@NotNull final TenantValue<?> tenantValue) {
		super(em);
		this.tenant = tenant;
		if (tenantValue != null) {
			withTenant(tenantValue.get());
		}
	}

	public Repository withTenant(final Object value) {
		this.tenantValue = value;
		return this;
	}
	

	public <T extends Model> Repository from(@NotNull final Class<T> type) {
		super.from(type);
		this.joinner = new JoinnerTenant(tenant, builder, from, tenantValue);
		try {
			predicates.add(tenant.run(builder, from, tenantValue));
		} catch (final TenantNotFound e) {}
		return this;
	}
}
