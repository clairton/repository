package br.eti.clairton.repository;

import static java.util.Arrays.asList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TransactionRequiredException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.transaction.Transactional;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import br.eti.clairton.paginated.collection.Meta;
import br.eti.clairton.paginated.collection.PaginatedCollection;
import br.eti.clairton.paginated.collection.PaginatedList;
import br.eti.clairton.paginated.collection.PaginatedMetaList;
import br.eti.clairton.tenant.TenantBuilder;
import br.eti.clairton.tenant.TenantNotFound;

/**
 * Repository para operações com o banco de dados.
 * 
 * @author Clairton Rodrigo Heinzen<clairton.rodrigo@gmail.com>
 */
@Dependent
public class Repository implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private final Logger logger = LogManager.getLogger(Repository.class);
	
	private EntityManager em;
	
	private final Cache cache;
	
	private final TenantBuilder tenant;

	private Root<? extends Model> from;

	private CriteriaQuery<?> criteriaQuery;

	private CriteriaBuilder criteriaBuilder;

	private List<javax.persistence.criteria.Predicate> predicates;

	private Object tenantValue;

	private final Joinner joinner;

	private Boolean withTenant = Boolean.FALSE;

	@Deprecated
	public Repository() {
		this(null, null, null, null);
	}

	@Inject
	public Repository(@NotNull final EntityManager em,
			@NotNull final Cache cache, @NotNull final TenantBuilder tenant,
			@NotNull final Joinner joinner) {
		super();
		this.em = em;
		this.cache = cache;
		this.tenant = tenant;
		this.joinner = joinner;
	}

	@Transactional
	public <T extends Model> T save(@NotNull T entity) {
		final T e = saveWithoutTransaction(entity);
		flush();
		return e;
	}

	public <T extends Model> T saveWithoutTransaction(@NotNull T entity) {
		if (!em.contains(entity) && entity.getId() != null) {
			entity = em.merge(entity);
		} else {
			em.persist(entity);
		}
		evictCache(entity);
		return entity;
	}

	@Transactional
	public <T extends Model> void remove(@NotNull final T entity) {
		removeWithoutTransaction(entity);
		flush();
	}

	public <T extends Model> void removeWithoutTransaction(
			@NotNull final T entity) {
		final Class<?> type = entity.getClass();
		final Long id = entity.getId();
		em.remove(entity);
		evictCache(type, id);
	}

	@Transactional
	public <T extends Model> void remove(@NotNull final Class<T> type,
			@NotNull Long id) {
		final T entity = em.find(type, id);
		em.remove(entity);
		evictCache(type, id);
		flush();
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
	public <T extends Model, Y> T byId(@NotNull final Class<T> klass,
			@NotNull final Y id) throws NoResultException {
		from(klass);
		final EntityType<T> type = em.getMetamodel().entity(klass);
		final Class<?> idType = type.getIdType().getJavaType();
		final Attribute<? super T, ?> attribute = type.getId(idType);
		final T result = where(id, attribute).single();
		return result;
	}

	public <T extends Model> Repository from(@NotNull final Class<T> type) {
		criteriaBuilder = em.getCriteriaBuilder();
		criteriaQuery = criteriaBuilder.createQuery(type);
		from = criteriaQuery.from(type);
		predicates = new ArrayList<javax.persistence.criteria.Predicate>();
		try {
			if (withTenant) {
				predicates.add(tenant.run(criteriaBuilder, from, tenantValue));
			}
		} catch (final TenantNotFound e) {
		}
		return this;
	}

	public <T extends Model> Repository distinct(@NotNull final Class<T> type) {
		from(type);
		return distinct();
	}

	public Repository distinct() {
		criteriaQuery.distinct(Boolean.TRUE);
		return this;
	}

	public <T extends Model> T single() {
		final TypedQuery<T> query = query(from, criteriaQuery, predicates);
		return query.getSingleResult();
	}

	public <T extends Model> PaginatedList<T, Meta> list(@NotNull @Min(0) final Integer page,
			@NotNull @Min(0) final Integer perPage) {
		final TypedQuery<T> query = query(from, criteriaQuery, predicates);
		if (page != 0 && perPage != 0) {
			query.setMaxResults(perPage);
			query.setFirstResult((page - 1) * perPage);
		}
		final Long total = count();
		final Meta meta = new Meta(total, Long.valueOf(page));
		return new PaginatedMetaList<T>(query.getResultList(), meta);
	}

	public Long count() {
		return count(Boolean.TRUE);
	}

	public Long count(Boolean distinct) {
		final Selection<Long> s;
		if (distinct) {
			s = criteriaBuilder.countDistinct(from);
		} else {
			s = criteriaBuilder.count(from);
		}
		final TypedQuery<Long> query = query(s, criteriaQuery, predicates);
		return (Long) query.getSingleResult();
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
		return list();
	}

	public <T extends Model> PaginatedCollection<T, Meta> collection(
			@NotNull @Min(0) final Integer page,
			@NotNull @Min(0) final Integer perPage) {
		return list(page, perPage);
	}

	public <T extends Model> List<T> list() {
		final TypedQuery<T> query = query(from, criteriaQuery, predicates);
		return query.getResultList();
	}

	public Repository where(@NotNull final Predicate predicate) {
		return where(asList(predicate));
	}

	public Repository or(@NotNull Predicate predicate) {
		javax.persistence.criteria.Predicate p = criteriaBuilder
				.and(this.predicates
						.toArray(new javax.persistence.criteria.Predicate[this.predicates
								.size()]));
		this.predicates = new ArrayList<javax.persistence.criteria.Predicate>();
		this.predicates.add(criteriaBuilder.or(p, to(predicate)));
		return this;
	}

	public Repository and(final @NotNull Predicate predicate) {
		concat(to(predicate));
		return this;
	}

	public <T> Repository and(@NotNull final T value,
			@Size(min = 1) @NotNull final Attribute<?, ?>... attributes) {
		return and(value, Comparators.EQUAL, attributes);
	}

	public <T> Repository and(@NotNull final T value,
			@NotNull final Comparator comparator,
			@Size(min = 1) @NotNull final Attribute<?, ?>... attributes) {
		return and(new Predicate(value, comparator, attributes));
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

	public Repository tenant(final Boolean withTenant) {
		this.withTenant = withTenant;
		return this;
	}

	public Repository tenantValue(final Object tenantValue) {
		this.withTenant = Boolean.TRUE;
		this.tenantValue = tenantValue;
		return this;
	}

	public void change(final @NotNull EntityManager em) {
		em.getEntityManagerFactory().getProperties().get("name");
		this.em = em;
	}

	@Transactional
	public <T extends Model> void remove() {
		removeWithoutTransaction();
		flush();
	}

	public <T extends Model> void removeWithoutTransaction() {
		final Collection<T> entities = collection();
		for (final T entity : entities) {
			removeWithoutTransaction(entity);
		}
	}

	@Transactional
	public <T extends Model> void save(final @NotNull Collection<T> entities) {
		saveWithoutTransaction(entities);
		flush();
	}

	public <T extends Model> void saveWithoutTransaction(
			final @NotNull Collection<T> entities) {
		for (final T entity : entities) {
			saveWithoutTransaction(entity);
		}
	}

	// =======================================================================//
	// ========================================metodos privados===============//
	// =======================================================================//

	private void flush() {
		logger.info("Executando Flush no Banco de dados");
		try {
			em.joinTransaction();
		} catch (final TransactionRequiredException e) {
		}
		try {
			em.flush();
		} catch (final TransactionRequiredException e) {
			logger.warn("Não há transação em andamento para rodar o EntityManager#flush");
			throw e;
		}
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

	private void to(
			@NotNull @Size(min = 1) final Collection<Predicate> predicates) {
		int i = 1;
		int j = predicates.size() - 1;
		final List<Predicate> ps = new ArrayList<Predicate>(predicates);
		javax.persistence.criteria.Predicate p = to(ps.get(0));
		for (; i <= j; i++) {
			final javax.persistence.criteria.Predicate other = to(ps.get(i));
			final Operator operator = ps.get(i).getOperator();
			p = operator.build(criteriaBuilder, p, other);
		}
		concat(p);
	}

	private javax.persistence.criteria.Predicate to(
			@NotNull final Predicate predicate) {
		return joinner.join(criteriaBuilder, from, predicate, tenantValue,
				withTenant);
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
		try{
			cache.evict(type, id);
		}catch(Exception e){
			logger.warn("Erro ao invalidar cache", e.getMessage());
			logger.debug("Erro ao invalidar cache", e);
		}
	}
}
