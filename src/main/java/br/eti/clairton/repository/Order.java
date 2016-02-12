package br.eti.clairton.repository;

import static br.eti.clairton.repository.Order.Direction.ASC;
import static java.util.Arrays.asList;

import java.util.List;

import javax.persistence.metamodel.Attribute;

public class Order {
	public enum Direction {
		ASC,
		DESC;

		public static Direction byString(final String order) {
			return valueOf(order.toUpperCase());
		}
	}

	private final Direction direction;

	private final List<Attribute<?, ?>> attributes;

	public Order(final Attribute<?, ?>... attributes) {
		this(ASC, asList(attributes));
	}

	public Order(final List<Attribute<?, ?>> attributes) {
		this(ASC, attributes);
	}

	public Order(final Direction direction, final Attribute<?, ?>... attributes) {
		this(direction, asList(attributes));
	}

	public Order(final Direction direction, final List<Attribute<?, ?>> attributes) {
		super();
		this.attributes = attributes;
		this.direction = direction;
	}

	public List<Attribute<?, ?>> getAttributes() {
		return attributes;
	}

	public Direction getDirection() {
		return direction;
	}
}
