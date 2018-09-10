package br.eti.clairton.repository;

import static br.eti.clairton.repository.Comparators.EQUAL;
import static br.eti.clairton.repository.Order.Direction.ASC;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Arrays.asList;
import static javax.persistence.criteria.JoinType.INNER;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TransactionRequiredException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.FetchParent;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.transaction.Transactional;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import br.eti.clairton.paginated.collection.Meta;
import br.eti.clairton.paginated.collection.PaginatedCollection;
import br.eti.clairton.paginated.collection.PaginatedList;
import br.eti.clairton.paginated.collection.PaginatedMetaList;
import br.eti.clairton.repository.Order.Direction;

/**
 * Repository para operações com o banco de dados.
 * 
 * @author Clairton Rodrigo Heinzen clairton.rodrigo@gmail.com
 */
@Dependent
public class Repository implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(Repository.class.getName());

	private final Map<String, Object> hints = new HashMap<>();
	
	private EntityManager em;

	protected Root<?> from;
	
	private List<Expression<?>> selections = new LinkedList<>();

	private CriteriaQuery<?> criteriaQuery;
	
	protected final List<javax.persistence.criteria.Order> orders = new ArrayList<>();
	
	protected final List<javax.persistence.criteria.Predicate> predicates = new ArrayList<>();

	protected CriteriaBuilder builder;

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
	public <T> T save(@NotNull final T entity) {
		final T e = saveWithoutTransaction(entity);
		flush();
		return e;
	}

	@Transactional
	public <T> T merge(@NotNull final T entity) {
		final T e = mergeWithoutTransaction(entity);
		flush();
		return e;
	}

	@Transactional
	public <T> void persist(@NotNull final T entity) {
		persistWithoutTransaction(entity);
		flush();
	}

	public <T> T mergeWithoutTransaction(@NotNull T entity) {
		entity = em.merge(entity);
		return entity;
	}

	public <T> void persistWithoutTransaction(@NotNull final T entity) {
		em.persist(entity);
	}

	public <T> void refresh(@NotNull final T entity) {
		em.refresh(entity);
	}

	public <T> T saveWithoutTransaction(@NotNull T entity) {
		if (!em.contains(entity) && isManaged(entity)) {
			entity = mergeWithoutTransaction(entity);
		} else {
			persistWithoutTransaction(entity);
		}
		return entity;
	}

	@Transactional
	public <T> void remove(@NotNull final T entity) {
		removeWithoutTransaction(entity);
		flush();
	}

	public <T, Y> void remove(@NotNull final Class<T> klass, @NotNull final Y id) {
		final T entity = byId(klass, id);
		remove(entity);
	}
	
	@Transactional
	public <T> void remove(@NotNull final Collection<T> entities) {
		removeWithoutTransaction(entities);
		flush();
	}

	public <T> void removeWithoutTransaction(@NotNull final Collection<T> entities) {
		for (final T entity : entities) {
			removeWithoutTransaction(entity);
		}
	}

	public <T> void removeWithoutTransaction(@NotNull final T entity) {
		em.remove(entity);
	}

	@Transactional
	public <T> void remove(@NotNull final Class<T> type, @NotNull Long id) {
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
	 * @param <T> type of entity  
	 * @param <Y> type of id  
	 * @return entidade
	 * @throws NoResultException
	 *             caso não seja encontrada a entidade
	 */
	public <T, Y> T byId(@NotNull final Class<T> klass, @NotNull final Y id) throws NoResultException {
		from(klass);
		final Attribute<? super T, ?> attribute= idAttribute(klass);
		final T result = where(id, attribute).single();
		return result;
	}

	public <T> Repository from(@NotNull final Class<T> type) {
		return from(type, type);
	}

	public <T, Y> Repository from(@NotNull final Class<T> modelType, @NotNull final Class<Y> transferObjectType) {
		builder = em.getCriteriaBuilder();
		criteriaQuery = builder.createQuery(transferObjectType);
		from = root(modelType);
		joinner = new Joinner(builder, from);
		return this;
	}

	public <T> Root<T> root(@NotNull final Class<T> type) {
		return criteriaQuery.from(type);
	}

	public <T> Repository distinct(@NotNull final Class<T> type) {
		from(type);
		return distinct();
	}

	public Repository distinct() {
		criteriaQuery.distinct(TRUE);
		return this;
	}

	public <T> T single() {
		final TypedQuery<T> query = query(selections, criteriaQuery, predicates, orders);		
		this.filtersClears();
		return query.getSingleResult();
	}

	public <T> PaginatedList<T, Meta> list(@NotNull @Min(0) final Integer page, @NotNull @Min(0) final Integer perPage) {
		final TypedQuery<T> query = query(selections, criteriaQuery, predicates, orders);
		if (page != 0 && perPage != 0) {
			query.setMaxResults(perPage);
			query.setFirstResult((page - 1) * perPage);
		} else if (perPage != 0) {
			query.setMaxResults(perPage);
		}
		final Long total = count();
		final Meta meta = new Meta(total, Long.valueOf(page));
		return new PaginatedMetaList<T>(query.getResultList(), meta);
	}

	public Long count() {
		return count(TRUE);
	}

	public Long count(final Boolean distinct) {
		final Expression<?> from;
		if (selections.isEmpty()) {			
			from = this.from;
		} else if(selections.size() ==  1) {			
			from = (Expression<?>) selections.get(0);
		} else {
			throw new SelectionHasManyButMustOneException();
		}
		@SuppressWarnings("unchecked")
		final Set<Fetch<?, ?>> fetches = (Set<Fetch<?, ?>>)((Set<?>) this.from.getFetches()); 
		fetchToJoin(this.from, fetches);
		final Expression<?> s;
		if (distinct) {
			s = builder.countDistinct(from);
		} else {
			s = builder.count(from);
		}
		final List<Expression<?>> selections = new ArrayList<>(); 
		selections.add(s);
		final TypedQuery<Long> query = query(selections, criteriaQuery, predicates, new ArrayList<javax.persistence.criteria.Order>());
		final Long count = (Long) query.getResultList().get(0);		
		filtersClears();
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

	public <T> PaginatedCollection<T, Meta> collection(@NotNull @Min(0) final Integer page, @NotNull @Min(0) final Integer perPage) {
		return list(page, perPage);
	}

	public <T> List<T> list() {
		final TypedQuery<T> query = query(selections, criteriaQuery, predicates, orders);
		this.filtersClears();
		return query.getResultList();
	}

	public Repository where(@NotNull final Predicate predicate) {
		return where(asList(predicate));
	}

	public Repository or(@NotNull Predicate predicate) {
		final javax.persistence.criteria.Predicate[] array = new javax.persistence.criteria.Predicate[this.predicates.size()];
		final javax.persistence.criteria.Predicate p = builder.and(predicates.toArray(array));
		predicatesClear();
		predicates.add(builder.or(p, to(predicate)));
		return this;
	}
	
	public <T> Repository or(@NotNull final T value, @Size(min = 1) @NotNull final Attribute<?, ?>... attributes) {
		return or(value, EQUAL, attributes);
	}

	public <T> Repository or(@NotNull final T value, @NotNull final Comparator comparator, @Size(min = 1) @NotNull final Attribute<?, ?>... attributes) {
		return or(new Predicate(value, comparator, attributes));
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

	public Repository orderBy(@NotNull final Direction direction, @Size(min = 1) @NotNull final Attribute<?, ?>... attributes) {
		final Expression<?> path = joinner.join(INNER, attributes);
		final javax.persistence.criteria.Order order;
		if (ASC.equals(direction)) {
			order = builder.asc(path);
		} else {
			order = builder.desc(path);
		}
		orders.add(order);
		return this;
	}

	public Repository orderBy(@NotNull final Direction direction, @Size(min = 1) @NotNull final List<Attribute<?, ?>> attributes) {
		orderBy(direction, attributes.toArray(new Attribute<?,?>[attributes.size()]));
		return this;
	}

	public Repository orderBy(@NotNull final Order... orders) {
		orderBy(asList(orders));
		return this;
	}

	public Repository orderBy(@NotNull final List<Order> orders) {
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
			this.selections.add(joinner.select(INNER, attributes));
		}
		return this;
	}
	
  	public <T>Repository multiselect(@NotNull final Attribute<?, ?>... attributes) {
  		return select(attributes);
  	}
  
  	public <T>Repository multiselect(final JoinType joinType, @NotNull final Attribute<?, ?>... attributes) {
  		return select(joinType, attributes);
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
	
	public Boolean notExist() {
		return !exist();
	}
	
	public Repository hint(final String key, final Object value) {
		this.hints.put(key, value);
		return this;
	}

	public void change(@NotNull final EntityManager em) {
		em.getEntityManagerFactory().getProperties().get("name");
		this.em = em;
	}

	@Transactional
	public <T> void remove() {
		removeWithoutTransaction();
		flush();
	}

	public <T> void removeWithoutTransaction() {
		final Collection<T> entities = collection();
		removeWithoutTransaction(entities);
	}

	@Transactional
	public <T> void save(final @NotNull Collection<T> entities) {
		saveWithoutTransaction(entities);
		flush();
	}

	public <T> void saveWithoutTransaction(final @NotNull Collection<T> entities) {
		for (final T entity : entities) {
			saveWithoutTransaction(entity);
		}
	}
	
	public void close(){
		this.em.close();
	}

	/**
	 * Execute {@link EntityManager#clear()}
	 * 
	 * @return this
	 */
	public Repository clear() {
		this.flush();
		this.em.clear();
		return this;
	}

	public Repository readonly() {
		hint("org.hibernate.readOnly", "true");
		hint("org.hibernate.cacheable", "false");
		hint("eclipselink.read-only", "true");
		hint("eclipselink.query-results-cache", "true");
		return this;
	}	
	
	public void flush() {
		logger.info("Executando Flush no Banco de dados");
		try {
			em.joinTransaction();
		} catch (final TransactionRequiredException e) {
		}
		try {
			em.flush();
		} catch (final TransactionRequiredException e) {
			logger.warning("Não há transação em andamento para rodar o EntityManager#flush");
			throw e;
		}
	}

	// =======================================================================//
	// ========================================metodos privados===============//
	// =======================================================================//
	protected void hintsClear(){
		hints.clear();
	}
	
	protected void ordersClear(){
		orders.clear();
	}
	
	protected void predicatesClear(){
		predicates.clear();
	}
	
	protected void selectionsClear(){
		selections.clear();
	}
	
	protected void filtersClears(){
		hintsClear();
		ordersClear();
		predicatesClear();
		selectionsClear();
	}
	
	protected <T>Boolean isManaged(final T record){
		return idValue(record) != null;
	}
	
	protected <X>Attribute<? super X, ?> idAttribute(final Class<X> klazz) {
		final EntityType<X> type = em.getMetamodel().entity(klazz);
		final Class<?> idType = type.getIdType().getJavaType();
		final Attribute<? super X, ?> attribute = type.getId(idType);
		return attribute;
	}
	
	protected <T>Object idValue(final T record) {
		final Class<?> type = record.getClass();
		final String name = idName(type);
		try{
			final Field field = getField(type, name);
			field.setAccessible(TRUE);
			final Object value = field.get(record);
			return value;			
		} catch (final IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected Field getField(final Class<?> type, final String name){
		try{
			final Field field = type.getDeclaredField(name);
			field.setAccessible(TRUE);
			return field;			
		} catch (final NoSuchFieldException e) {
			if(type.equals(Object.class)){
				throw new RuntimeException(e);
			} else {				
				return getField(type.getSuperclass(), name);
			}
		}
	}
	
	protected String idName(final Class<?> klazz) {
		final Attribute<?, ?> id = idAttribute(klazz);
		final String field = id.getName();
		return field;
	}

	protected <T> TypedQuery<T> query(
			final List<Expression<?>> selections, 
			final CriteriaQuery<?> criteriaQuery, 
			final List<javax.persistence.criteria.Predicate> predicates,
			final List<javax.persistence.criteria.Order> orders) {
		@SuppressWarnings("unchecked")
		final CriteriaQuery<T> cq = (CriteriaQuery<T>) criteriaQuery;
		if (selections.isEmpty()) {			
			@SuppressWarnings("unchecked")
			final Selection<T> s = (Selection<T>) from;
			cq.select(s);
		} else if(selections.size() ==  1) {			
			@SuppressWarnings("unchecked")
			final Selection<T> s = (Selection<T>) selections.get(0);
			cq.select(s);
		} else {
  			final List<Selection<?>> list = new ArrayList<>();
  			for (final Selection<?> selection : selections) {
				list.add(selection);
			}
			cq.multiselect(list);
  		}
		cq.orderBy(orders.toArray(new javax.persistence.criteria.Order[]{}));
		final javax.persistence.criteria.Predicate[] array = new javax.persistence.criteria.Predicate[predicates.size()];
		cq.where(predicates.toArray(array));
		final TypedQuery<T> query = em.createQuery(cq);		
		for (final Entry<String, Object> entry : hints.entrySet()) {
			final String key = entry.getKey();
			final Object value = entry.getValue();
			query.setHint(key, value);
		}
		return query;
	}

	protected void to(@NotNull @Size(min = 1) final Collection<Predicate> predicates) {
		int i = 1;
		int j = predicates.size() - 1;
		final List<Predicate> ps = new ArrayList<Predicate>(predicates);
		javax.persistence.criteria.Predicate p = to(ps.get(0));
		for (; i <= j; i++  ) {
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
	protected void fetchToJoin(final From<?, ?> from, final Set<Fetch<?, ?>> fetches) {
		if (fetches != null && !fetches.isEmpty()) {
			for (final Fetch<?, ?> fetch : fetches) {
				@SuppressWarnings("rawtypes")
				final Join join = (Join) fetch;
				final Set<Fetch<?, ?>> fs = (Set<Fetch<?, ?>>) ((Set<?>) fetch.getFetches());
				if (fs.isEmpty()) {
					try {
						from.getJoins().add(join);
					} catch (UnsupportedOperationException e) {
					}
				} else {
					fetchToJoin(join, fs);
				}
			}
			from.getFetches().clear();
		}
	}
}
