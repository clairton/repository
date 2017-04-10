package br.eti.clairton.repository;

import static br.eti.clairton.repository.Join.COLLECTION;
import static br.eti.clairton.repository.Join.LIST;
import static br.eti.clairton.repository.Join.MAP;
import static br.eti.clairton.repository.Join.SET;
import static javax.persistence.criteria.JoinType.INNER;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Vetoed;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.PluralAttribute.CollectionType;
import javax.persistence.metamodel.SingularAttribute;

/**
 * Joinner by {@link br.eti.clairton.repository.Predicate}.
 * 
 * @author Clairton Rodrigo Heinzen<clairton.rodrigo@gmail.com>
 */
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

	protected final Map<From<?, ?>, Map<Attribute<?, ?>, Join<?, ?>>> index = new HashMap<>();
	protected final CriteriaBuilder builder;
	protected final From<?, ?> from;
	protected From<?, ?> fromLast;

	/**
	 * Constructor default.
	 * 
	 * @param builder
	 *            instance of {@link CriteriaBuilder}
	 * @param from
	 *            instance of {@link From}
	 */
	public Joinner(final CriteriaBuilder builder, final From<?, ?> from) {
		super();
		this.builder = builder;
		this.from = from;
	}

	/**
	 * Select the attribute.
	 * 
	 * @param joinType
	 *            type of Join
	 * @param attributes
	 *            atrributes paths
	 * @return instance of {@link Selection}
	 */
	public <Y> Expression<Y> select(final JoinType joinType, final Attribute<?, ?>... attributes) {
		final Expression<Y> path = join(joinType, attributes);
		return path;
	}
	/**
	 * Select the attribute.
	 * 
	 * @param attributes
	 *            atrributes paths
	 * @return instance of {@link Selection}
	 */
	public <Y> Expression<Y> select(final Attribute<?, ?>... attributes) {
		return join(INNER, attributes);
	}

	/**
	 * Transform {@link br.eti.clairton.repository.Predicate} in
	 * {@link Predicate}.
	 * 
	 * @param predicate
	 *            instance of {@link br.eti.clairton.repository.Predicate}
	 * @return instance of {@link Predicate}
	 */
	public Predicate join(final br.eti.clairton.repository.Predicate predicate) {
		final Comparator comparator = predicate.getComparator();
		final Attribute<?, ?>[] attributes = predicate.getAttributes();
		final JoinType joinType = predicate.getJoinType();
		return join(joinType, comparator, predicate.getValue(), attributes);
	}
	
	public Predicate join(final JoinType joinType, final Comparator comparator, final Object value, final Attribute<?, ?>... attributes) {
		final Attribute<?, ?> attribute;
		fromLast = this.from;
		if (attributes.length == 0) {
			final String message = "Must be have a attribute in predicate";
			throw new IllegalStateException(message);
		} else if (attributes.length == 1) {
			attribute = attributes[0];
		} else {
			Integer i = 1;
			final Integer j = attributes.length - 1;
			Attribute<?, ?> a = attributes[0];
			Join<?, ?> join = join(fromLast, joinType, a);
			for (; i < j; i++) {
				a = attributes[i];
				join = join(join, joinType, a);
			}
			attribute = attributes[i];
			fromLast = join;
		}
		final Expression<?> path = get(fromLast, attribute);
		final Predicate joinPredicate = comparator.build(builder, path, value);
		return joinPredicate;
	}
	
	public Join<?, ?> join(final From<?, ?> from, final Attribute<?, ?> attribute) {
		final Join<?, ?> join = join(from, INNER, attribute);
		return join;
	}

	public <T, Y> Join<T, Y> join(final From<T, Y> from, final JoinType joinType, final Attribute<?, ?> attribute) {
		final Join<T, Y> join;
		if (isReady(from, attribute)) {
			@SuppressWarnings("unchecked")
			final Join<T, Y> j = (Join<T, Y>) searchIndex(from, attribute);
			join = j;
		} else {
			if (attribute.isCollection()) {
				final PluralAttribute<?, ?, ?> pAttribute = (PluralAttribute<?, ?, ?>) attribute;
				join = JOINS.get(pAttribute.getCollectionType()).join(from, joinType, pAttribute);
			} else {
				@SuppressWarnings("unchecked")
				final SingularAttribute<? super Y, Y> sAttribute = (SingularAttribute<? super Y, Y>) attribute;
				@SuppressWarnings("unchecked")
				final Join<T, Y> j = (Join<T, Y>) from.join(sAttribute, joinType);
				join = j;
			}
			addIndex(from, attribute, join);
		}
		return join;
	}

	protected <Y> Expression<Y> join(final Attribute<?, ?>... attributes) {
		return join(INNER, attributes);
	}

	protected <Y> Expression<Y> join(final JoinType joinType, final Attribute<?, ?>... attributes) {
		final Attribute<?, ?> attribute;
		From<?, ?> from = this.from;
		if (attributes.length == 0) {
			throw new AttributeNotBeEmptyException();
		} else if (attributes.length == 1) {
			attribute = attributes[0];
			if(attribute.isAssociation() && !isReady(from, attribute)){
				addIndex(from, attribute, join(from, joinType, attribute));
			}
		} else {
			Integer i = 1;
			final Integer j = attributes.length - 1;
			Attribute<?, ?> a = attributes[0];
			Join<?, ?> join = join(from, joinType, a);
			for (; i < j; i++) {
				a = attributes[i];
				join = join(join, joinType, a);
			}
			attribute = attributes[i];
			from = join;
		}
		final Expression<Y> path = get(from, attribute);
		return path;
	}

	public Boolean isReady(final From<?, ?> origin, final Attribute<?, ?> destiny) {
		return index.containsKey(origin) && index.get(origin).containsKey(destiny);
	}

	public Join<?, ?> searchIndex(final From<?, ?> origin, final Attribute<?, ?> destiny) {
		return index.get(origin).get(destiny);
	}

	public void addIndex(final From<?, ?> origin, final Attribute<?, ?> destiny, final Join<?, ?> value) {
		if (!index.containsKey(origin)) {
			index.put(origin, new HashMap<Attribute<?, ?>, Join<?, ?>>());
		}
		index.get(origin).put(destiny, value);
	}

	protected <T, Y> Expression<Y> get(final From<?, ?> from, final Attribute<?, ?> attribute) {
		final Expression<Y> path;
		if (attribute.isCollection()) {
			path = from.join(attribute.getName());
		} else {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			final Expression<Y> p = from.get((SingularAttribute) attribute);
			path = p;
		}
		return path;
	}
	
	protected Map<From<?, ?>, Map<Attribute<?, ?>, Join<?, ?>>> getIndex() {
		return index;
	}
}
