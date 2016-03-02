package br.eti.clairton.repository;

import static br.eti.clairton.repository.Comparators.EQUAL;
import static br.eti.clairton.repository.Operators.AND;
import static java.util.Arrays.asList;
import static javax.persistence.criteria.JoinType.INNER;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.JoinType;
import javax.persistence.metamodel.Attribute;

public class PredicateBuilder {
	private Object value;
	private JoinType join;
	private Comparator comparator;
	private Operator operator;
	private List<Attribute<?, ?>> attributes;

	public PredicateBuilder() {
		defaultValues();
	}

	public PredicateBuilder value(final Object value) {
		this.value = value;
		return this;
	}

	public PredicateBuilder join(final JoinType join) {
		this.join = join;
		return this;
	}

	public PredicateBuilder comparator(final Comparator comparator) {
		this.comparator = comparator;
		return this;
	}

	public PredicateBuilder attributes(final List<Attribute<?, ?>> attributes) {
		this.attributes.addAll(attributes);
		return this;
	}

	public PredicateBuilder attribute(final Attribute<?, ?>... attributes) {
		return attributes(asList(attributes));
	}

	public Predicate build() {
		final Predicate predicate = new Predicate(value, join, comparator,
				operator, attributes.toArray(new Attribute<?, ?>[attributes
						.size()]));
		defaultValues();
		return predicate;
	}

	private void defaultValues() {
		value = null;
		join = INNER;
		comparator = EQUAL;
		attributes = new ArrayList<Attribute<?, ?>>();
		operator = AND;
	}

	public PredicateBuilder operator(final Operator operator) {
		this.operator = operator;
		return this;
	}
}
