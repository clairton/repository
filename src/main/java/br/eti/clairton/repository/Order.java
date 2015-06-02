package br.eti.clairton.repository;

import java.util.Arrays;
import java.util.List;

import javax.persistence.metamodel.Attribute;

public class Order {
	public enum Type {
		ASC, DESC;

		public static Type byString(final String order) {
			return valueOf(order.toLowerCase());
		}
	}

	private final Type type;

	private final List<Attribute<?, ?>> attributes;

	public Order(final Attribute<?, ?>... attributes) {
		this(Type.ASC, Arrays.asList(attributes));
	}

	public Order(final List<Attribute<?, ?>> attributes) {
		this(Type.ASC, attributes);
	}

	public Order(final Type type, final Attribute<?, ?>... attributes) {
		this(type, Arrays.asList(attributes));
	}

	public Order(final Type type, final List<Attribute<?, ?>> attributes) {
		super();
		this.attributes = attributes;
		this.type = type;
	}

	public List<Attribute<?, ?>> getAttributes() {
		return attributes;
	}

	public Type getType() {
		return type;
	}
}
