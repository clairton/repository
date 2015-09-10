package br.eti.clairton.repository;

import static br.eti.clairton.repository.Join.COLLECTION;
import static br.eti.clairton.repository.Join.LIST;
import static br.eti.clairton.repository.Join.MAP;
import static br.eti.clairton.repository.Join.SET;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.PluralAttribute.CollectionType;
import javax.persistence.metamodel.SingularAttribute;
import javax.validation.constraints.NotNull;

import br.eti.clairton.tenant.TenantBuilder;
import br.eti.clairton.tenant.TenantNotFound;

@Dependent
public class Joinner {

	private final static Map<CollectionType, br.eti.clairton.repository.Join> JOINS = new HashMap<CollectionType, br.eti.clairton.repository.Join>() {
		private static final long serialVersionUID = 1L;
		{
			put(CollectionType.LIST, LIST);
			put(CollectionType.COLLECTION, COLLECTION);
			put(CollectionType.SET, SET);
			put(CollectionType.MAP, MAP);
		}
	};
	private final TenantBuilder tenant;

	@Inject
	public Joinner(final TenantBuilder tenant) {
		super();
		this.tenant = tenant;
	}

	public javax.persistence.criteria.Predicate join(
			@NotNull final CriteriaBuilder criteriaBuilder,
			@NotNull From<?, ?> from, @NotNull final Predicate predicate,
			final Object tenantValue) {
		return join(criteriaBuilder, from, predicate, tenantValue, Boolean.TRUE);
	}

	public javax.persistence.criteria.Predicate join(
			@NotNull final CriteriaBuilder criteriaBuilder,
			@NotNull From<?, ?> from, @NotNull final Predicate predicate,
			final Object tenantValue, final @NotNull Boolean withTenant) {
		final Comparator comparator = predicate.getComparator();
		final Attribute<?, ?> attribute;
		final Attribute<?, ?>[] attributes = predicate.getAttributes();
		final JoinType joinType = predicate.getJoinType();
		if (attributes.length == 0) {
			final String message = "Must be have a attribute in predicate";
			throw new IllegalStateException(message);
		} else if (predicate.getAttributes().length == 1) {
			attribute = attributes[0];
		} else {
			Integer i = 1;
			final Integer j = attributes.length - 1;
			Attribute<?, ?> a = attributes[0];
			Join<?, ?> join = join(criteriaBuilder, from, joinType, a, tenantValue);
			for (; i < j; i++) {
				a = attributes[i];
				join = join(criteriaBuilder, join, joinType, a,
						tenantValue);
			}
			attribute = attributes[i];
			from = join;
		}
		final Expression<?> path = get(from, attribute);
		final javax.persistence.criteria.Predicate joinPredicate = comparator.build(criteriaBuilder, path, predicate.getValue());
		try {
			if (!withTenant) {
				throw new TenantNotFound();
			}
			final javax.persistence.criteria.Predicate tenantPredicate = tenant(criteriaBuilder, from, tenantValue);
			final javax.persistence.criteria.Predicate completePredicate = criteriaBuilder.and(joinPredicate, tenantPredicate);
			return completePredicate;
		} catch (final TenantNotFound e) {
			return joinPredicate;
		}
	}
	
	public <T>Path<T> join(
			Metamodel metaModel,
			@NotNull final CriteriaBuilder criteriaBuilder, 
			@NotNull From<?, ?> from,
			final JoinType joinType,
			final Object tenantValue, 
			final @NotNull Boolean withTenant,
			final Class<?> type,
			final Attribute<?, ?>... attributes) {
		final Attribute<?, ?> attribute;
		if (attributes.length == 0) {
			final String message = "Must be have a attribute in predicate";
			throw new IllegalStateException(message);
		} else if (attributes.length == 1) {
			attribute = attributes[0];
		} else {
			Integer i = 1;
			final Integer j = attributes.length - 1;
			Attribute<?, ?> a = attributes[0];
			Join<?, ?> join = join(criteriaBuilder, from, joinType, a, tenantValue);
			for (; i < j; i++) {
				a = attributes[i];
				join = join(criteriaBuilder, join, joinType, a, tenantValue);
			}
			attribute = attributes[i];
			from = join;
		}
		Path<T> path;
		try{
			metaModel.entity(type);
			final Path <T> p;
			if (attribute.isCollection()) {
				final PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) attribute;
				final br.eti.clairton.repository.Join join = JOINS.get(pluralAttribute.getCollectionType());
				@SuppressWarnings("unchecked")
				final Join<?, T> join2 = (Join<?, T>) join.join(from, joinType, pluralAttribute);
				p = (Path<T>) join2;
			} else {
				@SuppressWarnings("rawtypes")
				final SingularAttribute singularAttribute = (SingularAttribute) attribute;
				@SuppressWarnings("unchecked")
				Join<?, T> join = from.join(singularAttribute, joinType);
				p = join;
			}
			path = p;
		}catch(final IllegalArgumentException e){
			path = get(from, attribute);			
		}
		return path;
	}

	// =======================================================================//
	// ========================================metodos privados===============//
	// =======================================================================//
	private <T, Y> Join<T, Y> join(
			@NotNull final CriteriaBuilder criteriaBuilder,
			@NotNull final From<T, Y> from, @NotNull final JoinType joinType,
			@NotNull final Attribute<?, ?> attribute,
			final @NotNull Object tenantValue) {
		final Join<T, Y> join;
		if (attribute.isCollection()) {
			final PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) attribute;
			join = JOINS.get(pluralAttribute.getCollectionType()).join(from, joinType, pluralAttribute);
		} else {
			@SuppressWarnings("unchecked")
			final SingularAttribute<? super Y, Y> singularAttribute = (SingularAttribute<? super Y, Y>) attribute;
			@SuppressWarnings("unchecked")
			final Join<T, Y> j = (Join<T, Y>) from.join(singularAttribute, joinType);
			join = j;
		}
		return join;
	}

	private javax.persistence.criteria.Predicate tenant(
			@NotNull final CriteriaBuilder criteriaBuilder,
			@NotNull From<?, ?> from, final @NotNull Object tenantValue)
			throws TenantNotFound {
		return tenant.run(criteriaBuilder, from, tenantValue);
	}

	private <T, Y> Path<Y> get(@NotNull final From<?, ?> from, @NotNull final Attribute<?, ?> attribute) {
		final Path<Y> path;
		if (attribute.isCollection()) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			final Path<Y> p = (Path<Y>) from.get((PluralAttribute) attribute);
			path = p;
		} else {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			final Path<Y> p = from.get((SingularAttribute) attribute);
			path = p;
		}
		return path;
	}
}
