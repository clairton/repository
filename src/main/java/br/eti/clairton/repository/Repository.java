package br.eti.clairton.repository;

import static br.eti.clairton.repository.Comparators.EQUAL;
import static br.eti.clairton.repository.Order.Direction.ASC;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Arrays.asList;
import static javax.persistence.criteria.JoinType.INNER;
import static org.apache.logging.log4j.LogManager.getLogger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TransactionRequiredException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.FetchParent;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.transaction.Transactional;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.logging.log4j.Logger;

import br.eti.clairton.paginated.collection.Meta;
import br.eti.clairton.paginated.collection.PaginatedCollection;
import br.eti.clairton.paginated.collection.PaginatedList;
import br.eti.clairton.paginated.collection.PaginatedMetaList;
import br.eti.clairton.repository.Order.Direction;

/**
 * Repository para operações com o banco de dados.
 * 
 * @author Clairton Rodrigo Heinzen<clairton.rodrigo@gmail.com>
 */
@Dependent
public class Repository implements Serializable {
	private static final long serialVersionUID = 1L;

	private final Logger logger = getLogger(Repository.class);

	private EntityManager em;

	protected Root<? extends Model> from;
	
	private Selection<?> selection;

	private CriteriaQuery<?> criteriaQuery;

	protected CriteriaBuilder builder;

	protected List<javax.persistence.criteria.Predicate> predicates;

	private final List<javax.persistence.criteria.Order> orders = new ArrayList<>();
	
	private final Map<String, Object> hints = new HashMap<>();

	protected Joinner joinner;

	@Deprecated
	public Repository() {
		this(null);
	}

	@Inject
	public Repository(@NotNull final EntityManager em) {
		super();
		this.em = em;
	}

	@Transactional
	public <T extends Model> T save(@NotNull final T entity) {
		final T e = saveWithoutTransaction(entity);
		flush();
		return e;
	}

	@Transactional
	public <T extends Model> T merge(@NotNull final T entity) {
		final T e = mergeWithoutTransaction(entity);
		flush();
		return e;
	}

	@Transactional
	public <T extends Model> void persist(@NotNull final T entity) {
		persistWithoutTransaction(entity);
		flush();
	}

	public <T extends Model> T mergeWithoutTransaction(@NotNull T entity) {
		entity = em.merge(entity);
		return entity;
	}

	public <T extends Model> void persistWithoutTransaction(@NotNull final T entity) {
		em.persist(entity);
	}

	public <T extends Model> void refresh(@NotNull final T entity) {
		em.refresh(entity);
	}

	public <T extends Model> T saveWithoutTransaction(@NotNull T entity) {
		if (!em.contains(entity) && entity.getId() != null) {
			entity = mergeWithoutTransaction(entity);
		} else {
			persistWithoutTransaction(entity);
		}
		return entity;
	}

	@Transactional
	public <T extends Model> void remove(@NotNull final T entity) {
		removeWithoutTransaction(entity);
		flush();
	}
	
	@Transactional
	public <T extends Model> void remove(@NotNull final Collection<T> entities) {
		removeWithoutTransaction(entities);
		flush();
	}

	public <T extends Model> void removeWithoutTransaction(@NotNull Collection<T> entities) {
		for (final T entity : entities) {
			removeWithoutTransaction(entity);
		}
	}

	public <T extends Model> void removeWithoutTransaction(@NotNull final T entity) {
		em.remove(entity);
	}

	@Transactional
	public <T extends Model> void remove(@NotNull final Class<T> type, @NotNull Long id) {
		final T entity = em.find(type, id);
		em.remove(entity);
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
	public <T extends Model, Y> T byId(@NotNull final Class<T> klass, @NotNull final Y id) throws NoResultException {
		from(klass);
		final EntityType<T> type = em.getMetamodel().entity(klass);
		final Class<?> idType = type.getIdType().getJavaType();
		final Attribute<? super T, ?> attribute = type.getId(idType);
		final T result = where(id, attribute).single();
		return result;
	}

	public <T extends Model> Repository from(@NotNull final Class<T> type) {
		builder = em.getCriteriaBuilder();
		criteriaQuery = builder.createQuery(type);
		from = criteriaQuery.from(type);
		selection = from;
		predicates = new ArrayList<javax.persistence.criteria.Predicate>();
		joinner = new Joinner(builder, from);
		return this;
	}

	public <T extends Model> Repository distinct(@NotNull final Class<T> type) {
		from(type);
		return distinct();
	}

	public Repository distinct() {
		criteriaQuery.distinct(TRUE);
		return this;
	}

	public <T> T single() {
		final TypedQuery<T> query = query(selection, criteriaQuery, predicates);
		return query.getSingleResult();
	}

	public <T> PaginatedList<T, Meta> list(@NotNull @Min(0) final Integer page, @NotNull @Min(0) final Integer perPage) {
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
		return count(TRUE);
	}
	
	public Long count(final Boolean distinct) {
		final Selection<Long> s;
		@SuppressWarnings("unchecked")
		final Set<Fetch<?, ?>> fetches = (Set<Fetch<?, ?>>)((Set<?>) from.getFetches()); 
		final From<?, ?> from = this.from;
		fetchToJoin(from, fetches);
		if (distinct) {
			s = builder.countDistinct(from);
		} else {
			s = builder.count(from);
		}
		final TypedQuery<Long> query = query(s, criteriaQuery, predicates);
		final Long count = (Long) query.getResultList().get(0);
		return count;
	}

	public <T> T first() {
		final List<T> list = list();
		if (list.isEmpty()) {
			throw new NoResultException();
		}
		return list.get(0);
	}

	public <T> T last() {
		final List<T> list = list();
		if (list.isEmpty()) {
			throw new NoResultException();
		}
		return list.get(list.size() - 1);
	}

	public <T> Collection<T> collection() {
		return list();
	}

	public <T extends Model> PaginatedCollection<T, Meta> collection(@NotNull @Min(0) final Integer page, @NotNull @Min(0) final Integer perPage) {
		return list(page, perPage);
	}

	public <T> List<T> list() {
		final TypedQuery<T> query = query(selection, criteriaQuery, predicates);
		return query.getResultList();
	}

	public Repository where(@NotNull final Predicate predicate) {
		return where(asList(predicate));
	}

	public Repository or(@NotNull Predicate predicate) {
		javax.persistence.criteria.Predicate p = builder
				.and(this.predicates
						.toArray(new javax.persistence.criteria.Predicate[this.predicates
								.size()]));
		this.predicates = new ArrayList<javax.persistence.criteria.Predicate>();
		this.predicates.add(builder.or(p, to(predicate)));
		return this;
	}

	public Repository and(final @NotNull Predicate predicate) {
		concat(to(predicate));
		return this;
	}

	public <T> Repository and(@NotNull final T value, @Size(min = 1) @NotNull final Attribute<?, ?>... attributes) {
		return and(value, EQUAL, attributes);
	}

	public <T> Repository and(@NotNull final T value, @NotNull final Comparator comparator, @Size(min = 1) @NotNull final Attribute<?, ?>... attributes) {
		return and(new Predicate(value, comparator, attributes));
	}

	public Repository orderBy(final @NotNull Direction direction, final @Size(min = 1) @NotNull Attribute<?, ?>... attributes) {
		orderBy(direction, Arrays.asList(attributes));
		return this;
	}

	public Repository orderBy(final @NotNull Direction direction, final @Size(min = 1) @NotNull List<Attribute<?, ?>> attributes) {
		Path<?> path = from.get(attributes.get(0).getName());
		Integer i = 1;
		final Integer j = attributes.size() - 1;
		for (; i <= j; i++) {
			path = path.get(attributes.get(i).getName());
		}
		final javax.persistence.criteria.Order order;
		if (ASC.equals(direction)) {
			order = builder.asc(path);
		} else {
			order = builder.desc(path);
		}
		orders.add(order);
		return this;
	}

	public Repository orderBy(final @NotNull Order... orders) {
		orderBy(asList(orders));
		return this;
	}

	public Repository orderBy(final @NotNull List<Order> orders) {
		for (final Order order : orders) {
			orderBy(order.getDirection(), order.getAttributes());
		}
		return this;
	}

	public <T> Repository where(@NotNull final T value, @Size(min = 1) @NotNull final Attribute<?, ?>... attributes) {
		return where(asList(new Predicate(value, attributes)));
	}

	public <T> Repository where(@NotNull final T value, @NotNull final Comparator comparator, @NotNull @Size(min = 1) final Attribute<?, ?>... attributes) {
		return where(asList(new Predicate(value, comparator, attributes)));
	}

	public <T>Repository fetch(final JoinType type, @NotNull @Size(min = 1) final Attribute<?, ?>... attributes) {
		FetchParent<?, ?> fetch = from;
		for (final Attribute<?, ?> attribute : attributes) {
			fetch = fetch.fetch(attribute.getName(), type);
		}		
		return this;
	}

	public <T>Repository fetch(@NotNull @Size(min = 1) final Attribute<?, ?>... attributes) {
		fetch(INNER, attributes);
		return this;
	}

	public <T>Repository select(@NotNull final Attribute<?, ?>... attributes) {
		select(INNER, attributes);
		return this;
	}

	public <T>Repository select(final JoinType joinType, @NotNull final Attribute<?, ?>... attributes) {
		if(attributes.length > 0){
			this.selection = joinner.select(INNER, attributes);
		}
		return this;
	}

	public <T> Repository where(@NotNull final Comparator comparator, @NotNull @Size(min = 1) final Attribute<?, ?>... attributes) {
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
			return FALSE;
		}
	}
	
	public Repository hint(final String key, final Object value) {
		this.hints.put(key, value);
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
		removeWithoutTransaction(entities);
	}

	@Transactional
	public <T extends Model> void save(final @NotNull Collection<T> entities) {
		saveWithoutTransaction(entities);
		flush();
	}

	public <T extends Model> void saveWithoutTransaction(final @NotNull Collection<T> entities) {
		for (final T entity : entities) {
			saveWithoutTransaction(entity);
		}
	}

	// =======================================================================//
	// ========================================metodos privados===============//
	// =======================================================================//

	protected void flush() {
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

	protected <T> TypedQuery<T> query(final Selection<?> selection, final CriteriaQuery<?> criteriaQuery, final List<javax.persistence.criteria.Predicate> predicates) {
		@SuppressWarnings("unchecked")
		final CriteriaQuery<T> cq = (CriteriaQuery<T>) criteriaQuery;
		@SuppressWarnings("unchecked")
		final Selection<T> s = (Selection<T>) selection;
		cq.select(s);
		criteriaQuery.orderBy(orders.toArray(new javax.persistence.criteria.Order[]{}));
		cq.where(predicates
				.toArray(new javax.persistence.criteria.Predicate[predicates
						.size()]));
		final TypedQuery<T> query = em.createQuery(cq);
		
		for (final Entry<String, Object> entry : hints.entrySet()) {
			final String key = entry.getKey();
			final Object value = entry.getValue();
			query.setHint(key, value);
		}
		
		hints.clear();
		orders.clear();
		return query;
	}

	protected void to(@NotNull @Size(min = 1) final Collection<Predicate> predicates) {
		int i = 1;
		int j = predicates.size() - 1;
		final List<Predicate> ps = new ArrayList<Predicate>(predicates);
		javax.persistence.criteria.Predicate p = to(ps.get(0));
		for (; i <= j; i++) {
			final javax.persistence.criteria.Predicate other = to(ps.get(i));
			final Operator operator = ps.get(i).getOperator();
			p = operator.build(builder, p, other);
		}
		concat(p);
	}

	protected javax.persistence.criteria.Predicate to(@NotNull final Predicate predicate) {
		return joinner.join(predicate);
	}

	protected void concat(final javax.persistence.criteria.Predicate... predicates) {
		final javax.persistence.criteria.Predicate and = builder.and(predicates);
		this.predicates.add(and);
	}

	@SuppressWarnings("unchecked")
	protected void fetchToJoin(final From<?, ?> from, Set<Fetch<?, ?>> fetches) {
		if (fetches != null && !fetches.isEmpty()) {
			for (final Fetch<?, ?> fetch : fetches) {
				@SuppressWarnings("rawtypes")
				Join join = (Join) fetch;
				final Set<Fetch<?, ?>> fs = (Set<Fetch<?, ?>>)((Set<?>) fetch.getFetches()); 
				if(fs.isEmpty()){
					try{
						from.getJoins().add(join);
					}catch(UnsupportedOperationException e){}
				}else{
					fetchToJoin(join, fs);					
				}				
			}
			from.getFetches().clear();	
		}
	}
}
