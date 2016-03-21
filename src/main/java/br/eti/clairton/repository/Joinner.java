package br.eti.clairton.repository;

import static br.eti.clairton.repository.Join.COLLECTION;
import static br.eti.clairton.repository.Join.LIST;
import static br.eti.clairton.repository.Join.MAP;
import static br.eti.clairton.repository.Join.SET;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Vetoed;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.PluralAttribute.CollectionType;
import javax.persistence.metamodel.SingularAttribute;
import javax.validation.constraints.NotNull;


@Vetoed
public class Joinner {

	public final static Map<CollectionType, br.eti.clairton.repository.Join> JOINS = new HashMap<CollectionType, br.eti.clairton.repository.Join>() {
		private static final long serialVersionUID = 1L;
		{
			put(CollectionType.LIST, LIST);
			put(CollectionType.COLLECTION, COLLECTION);
			put(CollectionType.SET, SET);
			put(CollectionType.MAP, MAP);
		}
	};
	protected final CriteriaBuilder builder;
	protected final From<?, ?> from;
	protected From<?, ?> fromLast;
	
	public Joinner(CriteriaBuilder builder, From<?, ?> from) {
		super();
		this.builder = builder;
		this.from = from;
	}

	public <Y>Expression<Y> join(final JoinType joinType, final Attribute<?, ?>... attributes) {
		final Attribute<?, ?> attribute;
		From<?, ?> from = this.from; 
		if (attributes.length == 0) {
			throw new AttributeNotBeEmptyException();
		} else if (attributes.length == 1) {
			attribute = attributes[0];
		} else {
			Integer i = 1;
			final Integer j = attributes.length - 1;
			Attribute<?, ?> a = attributes[0];
			Join<?, ?> join = join(builder, from, joinType, a);
			for (; i < j; i++) {
				a = attributes[i];
				join = join(builder, join, joinType, a);
			}
			attribute = attributes[i];
			from = join;
		}
		final Expression<Y> path = get(from, attribute);
		return path;
	}

	public <Y>Selection<Y> select(final JoinType joinType, final Attribute<?, ?>... attributes) {
		final Expression<Y> path = join(joinType, attributes);		
		final Attribute<?, ?> attribute = attributes[attributes.length - 1];
		final Class<?> type;
		if (PluralAttribute.class.isInstance(attribute)) {
			final PluralAttribute<?, ?, ?> pAttribute = (PluralAttribute<?, ?, ?>) attribute;
			type = pAttribute.getElementType().getJavaType();
		} else {
			final SingularAttribute<?, ?> sAttribute = (SingularAttribute<?, ?>) attribute;
			type = sAttribute.getJavaType();
		}
		@SuppressWarnings("unchecked")
		final Selection<Y> selection = (Selection<Y>) path.as(type);
		return selection;
	}	
	
	public javax.persistence.criteria.Predicate join( @NotNull final Predicate predicate) {
		final Comparator comparator = predicate.getComparator();
		final Attribute<?, ?> attribute;
		final Attribute<?, ?>[] attributes = predicate.getAttributes();
		final JoinType joinType = predicate.getJoinType();
		fromLast = this.from;
		if (attributes.length == 0) {
			final String message = "Must be have a attribute in predicate";
			throw new IllegalStateException(message);
		} else if (predicate.getAttributes().length == 1) {
			attribute = attributes[0];
		} else {
			Integer i = 1;
			final Integer j = attributes.length - 1;
			Attribute<?, ?> a = attributes[0];
			Join<?, ?> join = join(builder, fromLast, joinType, a);
			for (; i < j; i++) {
				a = attributes[i];
				join = join(builder, join, joinType, a);
			}
			attribute = attributes[i];
			fromLast = join;
		}
		final Expression<?> path = get(fromLast, attribute);
		final javax.persistence.criteria.Predicate joinPredicate = comparator.build(builder, path, predicate.getValue());
		return joinPredicate;
	}
	
	protected <T, Y> Join<T, Y> join(
			@NotNull final CriteriaBuilder criteriaBuilder,
			@NotNull final From<T, Y> from,
			@NotNull final JoinType joinType,
			@NotNull final Attribute<?, ?> attribute) {
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

	protected <T, Y> Expression<Y> get(@NotNull final From<?, ?> from, @NotNull final Attribute<?, ?> attribute) {
		final Expression<Y> path;
		if (attribute.isCollection()) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			final Expression<Y> p = (Expression<Y>) from.get((PluralAttribute) attribute);
			path = p;
		} else {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			final Expression<Y> p = from.get((SingularAttribute) attribute);
			path = p;
		}
		return path;
	}
}
