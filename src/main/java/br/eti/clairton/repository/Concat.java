package br.eti.clairton.repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public class Concat implements Comparator {
	private static final long serialVersionUID = 1L;

	private final Position position;

	private final Comparator delegate;

	private final String string;

	public Concat(final Comparator delegate, final Position position, final String string) {
		this.delegate = delegate;
		this.position = position;
		this.string = string;
	}

	public Concat(final Comparator delegate, final String string) {
		this(delegate, Position.AFTER, string);
	}

	@Override
	public Predicate build(final CriteriaBuilder cb, final Expression<?> x, final Object y) {
		@SuppressWarnings("unchecked")
		final Expression<String> expression = (Expression<String>) x;
		final Expression<String> concatened = position.build(cb, expression, string);
		return delegate.build(cb, concatened, y);
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	public enum Position{
		BEFORE,
		AFTER(){
			@Override
			public Expression<String> build(final CriteriaBuilder cb, final Expression<String> expression, final String string){
				return cb.concat(expression, string);
			}
		};

		public Expression<String> build(final CriteriaBuilder cb, final Expression<String> expression, final String string){
			return cb.concat(string, expression);
		}
	}
}
