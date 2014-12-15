package br.eti.clairton.repository;

import java.io.Serializable;
import java.util.Arrays;
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

	private Root<? extends Model> from;

	private CriteriaQuery<?> criteriaQuery;

	private CriteriaBuilder criteriaBuilder;

	private javax.persistence.criteria.Predicate[] predicates;

	@Deprecated
	protected Repository() {
		this(null, null);
	}

	@Inject
	public Repository(@NotNull final EntityManager em, final Cache cache) {
		super();
		this.em = em;
		this.cache = cache;
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
		predicates = new javax.persistence.criteria.Predicate[] {};
		return this;
	}

	public <T extends Model> T single() {
		@SuppressWarnings("unchecked")
		final Selection<T> selection = (Selection<T>) from;
		@SuppressWarnings("unchecked")
		final CriteriaQuery<T> cq = (CriteriaQuery<T>) criteriaQuery;
		final TypedQuery<T> query = em.createQuery(cq.select(selection).where(
				predicates));
		return query.getSingleResult();
	}

	public <T extends Model> T first() {
		@SuppressWarnings("unchecked")
		final T entity = (T) list().get(0);
		return entity;
	}

	public <T extends Model> T last() {
		@SuppressWarnings("unchecked")
		final T entity = (T) list().get(list().size() - 1);
		return entity;
	}

	public <T extends Model> Collection<T> collection() {
		return list(-1, -1);
	}

	public <T extends Model> Collection<T> collection(
			@NotNull @Min(1) final Integer page,
			@NotNull @Min(1) final Integer perPage) {
		return list(page, perPage);
	}

	public <T extends Model> List<T> list() {
		return list(-1, -1);
	}

	public <T extends Model> List<T> list(@NotNull @Min(1) final Integer page,
			@NotNull @Min(1) final Integer perPage) {
		@SuppressWarnings("unchecked")
		final Selection<T> selection = (Selection<T>) from;
		@SuppressWarnings("unchecked")
		final CriteriaQuery<T> cq = (CriteriaQuery<T>) criteriaQuery;
		final TypedQuery<T> query = em.createQuery(cq.select(selection).where(
				predicates));
		if (page > 0 && perPage > 0) {
			query.setMaxResults(perPage);
			query.setFirstResult((page - 1) * perPage);
		}
		return query.getResultList();
	}

	public Long count() {
		@SuppressWarnings("rawtypes")
		final Selection select = criteriaBuilder.count(from);
		@SuppressWarnings("unchecked")
		final CriteriaQuery<?> c = criteriaQuery.select(select).where(
				predicates);
		final TypedQuery<?> query = em.createQuery(c);
		return (Long) query.getSingleResult();
	}

	public Repository where(
			@NotNull @Size(min = 1) final Predicate... predicate) {
		return where(Arrays.asList(predicate));
	}

	public <T> Repository where(@NotNull final T value,
			@Size(min = 1) @NotNull final Attribute<?, ?>... attributes) {
		return where(Arrays.asList(new Predicate(value, attributes)));
	}

	public <T> Repository where(@NotNull final T value,
			@NotNull final Operator operator,
			@NotNull @Size(min = 1) final Attribute<?, ?>... attributes) {
		return where(Arrays.asList(new Predicate(value, operator, attributes)));
	}

	public <T> Repository where(@NotNull final Operator operator,
			@NotNull @Size(min = 1) final Attribute<?, ?>... attributes) {
		return where(Arrays.asList(new Predicate(operator, attributes)));
	}

	public Repository where(
			@NotNull @Size(min = 1) final Collection<Predicate> predicates) {
		Integer index = 0;
		this.predicates = new javax.persistence.criteria.Predicate[predicates
				.size()];
		for (final Predicate predicate : predicates) {
			if (predicate.getAttributes().length == 1) {
				final Path<?> path = get(from, predicate.getAttribute());
				final Operator operacao = predicate.getOperator();
				this.predicates[index] = operacao.build(criteriaBuilder, path,
						predicate.getValue());
			} else {
				int i = 1;
				int j = predicate.getAttributes().length - 1;
				Attribute<?, ?> attribute = predicate.getAttributes()[0];
				Join<?, ?> join = join(from, predicate.getJoinType(), attribute);
				for (; i < j; i++) {
					attribute = predicate.getAttributes()[i];
					join = join(join, predicate.getJoinType(), attribute);
				}
				attribute = predicate.getAttributes()[i];
				final Path<?> path = get(join, attribute);
				final Operator operacao = predicate.getOperator();
				this.predicates[index] = operacao.build(criteriaBuilder, path,
						predicate.getValue());
			}
			index++;
		}
		return this;
	}

	// =============================================================================================================//
	// ========================================metodos
	// privados=====================================================//
	// =============================================================================================================//
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
		return join;
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
