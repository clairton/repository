package br.eti.clairton.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Builder para facilidar o agrupamento de {@link Attribute}.
 * 
 * @author Clairton Rodrigo Heinzen<clairton.rodrigo@gmail.com>
 */
public class AttributeBuilder {
	private final List<Attribute<?, ?>> attributes = new ArrayList<>();

	private final EntityManager entityManager;

	public AttributeBuilder(final EntityManager entityManager) {
		super();
		this.entityManager = entityManager;
	}

	/**
	 * Construto com parametros.
	 * 
	 * @param attibute
	 *            {@link Attribute}
	 */
	public AttributeBuilder(final Attribute<?, ?> attibute) {
		this.entityManager = null;
		attributes.add(attibute);
	}

	/**
	 * Adiciona um {@link Attribute}
	 * 
	 * @param attibute
	 *            {@link Attribute}
	 * @return this
	 */
	public AttributeBuilder add(final Attribute<?, ?> attibute) {
		attributes.add(attibute);
		return this;
	}

	/**
	 * Devolve um array.
	 * 
	 * @return array
	 */
	public Attribute<?, ?>[] toArray() {
		final Attribute<?, ?>[] array = new Attribute<?, ?>[attributes.size()];
		return attributes.toArray(array);
	}

	/**
	 * Devolve uma coleção.
	 * 
	 * @return {@link Collection}
	 */
	public Collection<Attribute<?, ?>> toCollection() {
		return attributes;
	}

	public <T extends Model> Attribute<?, ?>[] with(
			@NotNull final Class<T> base,
			@NotNull @Size(min = 1) final String path) {
		final Metamodel metamodel = entityManager.getMetamodel();
		final EntityType<?> entityType = metamodel.entity(base);
		final String[] fields = path.split("\\.");
		final Attribute<?, ?> attribute = entityType.getAttribute(fields[0]);
		attributes.add(attribute);
		if (fields.length > 1
				&& (attribute.isAssociation() || attribute.isCollection())) {
			@SuppressWarnings("unchecked")
			final Class<T> nextType = (Class<T>) attribute.getJavaType();
			return with(nextType, path.replace(fields[0] + ".", ""));
		}
		final Attribute<?, ?>[] a = toArray();
		attributes.clear();
		return a;
	}
}
