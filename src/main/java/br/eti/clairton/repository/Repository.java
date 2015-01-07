package br.eti.clairton.repository;

import static java.util.Arrays.asList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.PluralAttribute.CollectionType;
import javax.persistence.metamodel.SingularAttribute;
import javax.transaction.Transactional;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import br.eti.clairton.tenant.TenantBuilder;

/**
 * Repository para operações com o banco de dados.
 * 
 * @author Clairton Rodrigo Heinzen<clairton.rodrigo@gmail.com>
 */
@ApplicationScoped
public class Repository implements Serializable {
	private static final long serialVersionUID = 1L;

	private final static Map<CollectionType, br.eti.clairton.repository.Join> JOINS = new HashMap<CollectionType, br.eti.clairton.repository.Join>() {
		private static final long serialVersionUID = 1L;
		{
			put(PluralAttribute.CollectionType.LIST,
					br.eti.clairton.repository.Join.LIST);
			put(PluralAttribute.CollectionType.COLLECTION,
					br.eti.clairton.repository.Join.COLLECTION);
			put(PluralAttribute.CollectionType.SET,
					br.eti.clairton.repository.Join.SET);
			put(PluralAttribute.CollectionType.MAP,
					br.eti.clairton.repository.Join.MAP);
		}
	};

	private final EntityManager em;
	private final Cache cache;
	private final TenantBuilder tenant;

	private Root<? extends Model> from;

	private CriteriaQuery<?> criteriaQuery;

	private CriteriaBuilder criteriaBuilder;

	private List<javax.persistence.criteria.Predicate> predicates;

	@Deprecated
	protected Repository() {
		this(null, null, null);
	}

	@Inject
	public Repository(@NotNull final EntityManager em,
			@NotNull final Cache cache, final TenantBuilder tenant) {
		super();
		this.em = em;
		this.cache = cache;
		this.tenant = tenant;
	}

	@Transactional
	public <T extends Model> T save(@NotNull T entity) {
		if (!em.contains(entity) && entity.getId() != null) {
			entity = em.merge(entity);
		} else {
			em.persist(entity);
		}
		em.flush();
		evictCache(entity);
		return entity;
	}

	@Transactional
	public <T extends Model> void remove(@NotNull final T entity) {
		final Class<?> type = entity.getClass();
		final Long id = entity.getId();
		em.remove(entity);
		evictCache(type, id);
		em.flush();
	}

	@Transactional
	public <T extends Model> void remove(@NotNull final Class<T> type,
			@NotNull Long id) {
		final T entity = em.find(type, id);
		em.remove(entity);
		evictCache(type, id);
		em.flush();
	}

	/**
	 * Busca uma entidade pelo id.
	 * 
	 * @param klass
	 *            tipo
	 * @param id
	 *            valor
	 * @return entidade
	 * @throws NoResultException
	 *             caso não seja encontrada a entidade
	 */
	public <T, Y> T byId(@NotNull final Class<T> klass, @NotNull final Y id)
			throws NoResultException {
		final T result = em.find(klass, id);
		if (result == null) {
			throw new NoResultException();
		}
		return result;
	}

	public <T extends Model> Repository from(@NotNull final Class<T> type) {
		criteriaBuilder = em.getCriteriaBuilder();
		criteriaQuery = criteriaBuilder.createQuery(type);
		from = criteriaQuery.from(type);
		predicates = new ArrayList<>();
		predicates.addAll(asList(tenant.run(criteriaBuilder, from)));
		return this;
	}

	public <T extends Model> T single() {
		final TypedQuery<T> query = query(from, criteriaQuery, predicates);
		return query.getSingleResult();
	}

	public <T extends Model> List<T> list(@NotNull @Min(0) final Integer page,
			@NotNull @Min(0) final Integer perPage) {
		final TypedQuery<T> query = query(from, criteriaQuery, predicates);
		if (page != 0 && perPage != 0) {
			query.setMaxResults(perPage);
			query.setFirstResult((page - 1) * perPage);
		}
		return query.getResultList();
	}

	public Long count() {
		final Selection<Long> s = criteriaBuilder.count(from);
		final TypedQuery<Long> query = query(s, criteriaQuery, predicates);
		return (Long) query.getSingleResult();
	}

	private <T> TypedQuery<T> query(final Selection<?> selection,
			final CriteriaQuery<?> criteriaQuery,
			final List<javax.persistence.criteria.Predicate> predicates) {
		@SuppressWarnings("unchecked")
		final CriteriaQuery<T> cq = (CriteriaQuery<T>) criteriaQuery;
		@SuppressWarnings("unchecked")
		final Selection<T> s = (Selection<T>) selection;
		cq.select(s)
				.where(predicates
						.toArray(new javax.persistence.criteria.Predicate[predicates
								.size()]));
		final TypedQuery<T> query = em.createQuery(cq);
		return query;
	}

	public <T extends Model> T first() {
		final List<T> list = list();
		if (list.isEmpty()) {
			throw new NoResultException();
		}
		return list.get(0);
	}

	public <T extends Model> T last() {
		final List<T> list = list();
		if (list.isEmpty()) {
			throw new NoResultException();
		}
		return list.get(list.size() - 1);
	}

	public <T extends Model> Collection<T> collection() {
		return list(0, 0);
	}

	public <T extends Model> Collection<T> collection(
			@NotNull @Min(0) final Integer page,
			@NotNull @Min(0) final Integer perPage) {
		return list(page, perPage);
	}

	public <T extends Model> List<T> list() {
		return list(0, 0);
	}

	public Repository where(@NotNull final Predicate predicate) {
		return where(asList(predicate));
	}

	public Repository or(@NotNull Predicate predicate) {
		javax.persistence.criteria.Predicate p = criteriaBuilder
				.and(this.predicates
						.toArray(new javax.persistence.criteria.Predicate[this.predicates
								.size()]));
		this.predicates = new ArrayList<>();
		this.predicates.add(criteriaBuilder.or(p, to(predicate)));
		return this;
	}

	public Repository and(final @NotNull Predicate predicate) {
		concat(to(predicate));
		return this;
	}

	public <T> Repository where(@NotNull final T value,
			@Size(min = 1) @NotNull final Attribute<?, ?>... attributes) {
		return where(asList(new Predicate(value, attributes)));
	}

	public <T> Repository where(@NotNull final T value,
			@NotNull final Comparator comparator,
			@NotNull @Size(min = 1) final Attribute<?, ?>... attributes) {
		return where(asList(new Predicate(value, comparator, attributes)));
	}

	public <T> Repository where(@NotNull final Comparator comparator,
			@NotNull @Size(min = 1) final Attribute<?, ?>... attributes) {
		return where(asList(new Predicate(comparator, attributes)));
	}

	public Repository where(@NotNull final Collection<Predicate> predicates) {
		if (!predicates.isEmpty()) {
			to(predicates);
		}
		return this;
	}

	public Boolean exist() {
		try {
			return count() > 0;
		} catch (final NoResultException e) {
			return Boolean.FALSE;
		}
	}

	// =======================================================================//
	// ========================================metodos privados===============//
	// =======================================================================//
	private void to(
			@NotNull @Size(min = 1) final Collection<Predicate> predicates) {
		int i = 1;
		int j = predicates.size() - 1;
		final List<Predicate> ps = new ArrayList<>(predicates);
		javax.persistence.criteria.Predicate p = to(ps.get(0));
		for (; i <= j; i++) {
			final javax.persistence.criteria.Predicate other = to(ps.get(i));
			final Operator operator = ps.get(i).getOperator();
			p = operator.build(criteriaBuilder, p, other);
		}
		concat(p);
	}

	private javax.persistence.criteria.Predicate to(final Predicate predicate) {
		final Comparator comparator = predicate.getComparator();
		final From<?, ?> from;
		final Attribute<?, ?> attribute;
		if (predicate.getAttributes().length == 0) {
			final String message = "Must be have a attribute in predicate";
			throw new IllegalStateException(message);
		} else if (predicate.getAttributes().length == 1) {
			from = this.from;
			attribute = predicate.getAttribute();
		} else {
			Integer i = 1;
			final Integer j = predicate.getAttributes().length - 1;
			Attribute<?, ?> a = predicate.getAttributes()[0];
			Join<?, ?> join = join(this.from, predicate.getJoinType(), a);
			for (; i < j; i++) {
				a = predicate.getAttributes()[i];
				join = join(join, predicate.getJoinType(), a);
			}
			attribute = predicate.getAttributes()[i];
			from = join;
		}
		final Path<?> path = get(from, attribute);
		return comparator.build(criteriaBuilder, path, predicate.getValue());
	}

	private <T, Y> Path<Y> get(@NotNull final From<?, ?> from,
			@NotNull final Attribute<?, ?> attribute) {
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

	private <T, Y> Join<T, Y> join(@NotNull final From<T, Y> from,
			@NotNull final JoinType joinType,
			@NotNull final Attribute<?, ?> attribute) {
		final Join<T, Y> join;
		if (attribute.isCollection()) {
			final PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) attribute;
			join = JOINS.get(pluralAttribute.getCollectionType()).join(from,
					joinType, pluralAttribute);
		} else {
			@SuppressWarnings("unchecked")
			final SingularAttribute<? super Y, Y> singularAttribute = (SingularAttribute<? super Y, Y>) attribute;
			@SuppressWarnings("unchecked")
			final Join<T, Y> j = (Join<T, Y>) from.join(singularAttribute,
					joinType);
			join = j;
		}
		concat(tenant.run(criteriaBuilder, join));
		return join;
	}

	private void concat(
			final javax.persistence.criteria.Predicate... predicates) {
		final javax.persistence.criteria.Predicate and = criteriaBuilder
				.and(predicates);
		this.predicates.add(and);
	}

	private <T extends Model> void evictCache(T entity) {
		final Class<?> type = entity.getClass();
		final Long id = entity.getId();
		evictCache(type, id);
	}

	private <T extends Model> void evictCache(final Class<?> type, final Long id) {
		cache.evict(type, id);
	}
}
