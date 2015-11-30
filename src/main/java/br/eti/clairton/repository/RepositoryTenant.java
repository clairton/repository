package br.eti.clairton.repository;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.validation.constraints.NotNull;

import br.eti.clairton.tenant.TenantBuilder;

/**
 * Devolve um Repository com o valor de tenant setado.
 * 
 * @author Clairton Rodrigo Heinzen<clairton.rodrigo@gmail.com>
 */
@Tenant
@RequestScoped
public class RepositoryTenant extends Repository {
	private static final long serialVersionUID = 1L;

	@Deprecated
	protected RepositoryTenant() {
		this(null, null, null, null);
	}

	@Inject
	public RepositoryTenant(@NotNull final EntityManager em,
			@NotNull final TenantBuilder tenant,
			@NotNull final Joinner joinner,
			@NotNull final TenantValue<?> tenantValue) {
		super(em, tenant, joinner);
		if (tenantValue != null) {
			tenantValue(tenantValue.get());
		}
	}
}
