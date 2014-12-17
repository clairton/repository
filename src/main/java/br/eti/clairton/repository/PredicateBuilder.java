package br.eti.clairton.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.criteria.JoinType;
import javax.persistence.metamodel.Attribute;

public class PredicateBuilder {
	private Object value;
	private JoinType join = JoinType.INNER;
	private Operator operator = Operators.EQUAL;
	private List<Attribute<?, ?>> attributes = new ArrayList<>();

	public PredicateBuilder value(final Object value) {
		this.value = value;
		return this;
	}

	public PredicateBuilder join(final JoinType join) {
		this.join = join;
		return this;
	}

	public PredicateBuilder operator(final Operator operator) {
		this.operator = operator;
		return this;
	}

	public PredicateBuilder attributes(final List<Attribute<?, ?>> attributes) {
		this.attributes.addAll(attributes);
		return this;
	}

	public PredicateBuilder attribute(final Attribute<?, ?>... attributes) {
		return attributes(Arrays.asList(attributes));
	}

	public Predicate build() {
		return new Predicate(value, join, operator,
				attributes.toArray(new Attribute<?, ?>[attributes.size()]));
	}
}
