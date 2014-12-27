package br.eti.clairton.repository;

import java.lang.annotation.Annotation;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.validation.constraints.NotNull;

@Dependent
public class TenantBuilder {

	private final Instance<Tenant<?>> tenants;

	@Inject
	public TenantBuilder(@Any final Instance<Tenant<?>> tenants) {
		super();
		this.tenants = tenants;
	}

	public <T extends Model> Predicate add(final @NotNull From<?, T> from,
			final @NotNull CriteriaBuilder criteriaBuilder) {
		final Class<? extends Model> klazz = (Class<? extends Model>) from
				.getJavaType();
		final TenantTyped type = new TenantTyped() {

			@Override
			public Class<? extends Annotation> annotationType() {
				return TenantTyped.class;
			}

			@Override
			public Class<? extends Model> value() {
				return klazz;
			}
		};
		final Instance<Tenant<?>> instance = tenants.select(type);
		if (instance.isUnsatisfied()) {
			return criteriaBuilder.equal(criteriaBuilder.literal(1), 1);
		}
		@SuppressWarnings("unchecked")
		final Tenant<T> tenant = (Tenant<T>) instance.get();
		return tenant.build(from, criteriaBuilder);
	}
}
