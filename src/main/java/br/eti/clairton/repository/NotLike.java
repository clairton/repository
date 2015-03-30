package br.eti.clairton.repository;

import java.util.Collection;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

/**
 * Cria o {@link CriteriaBuilder#notLike}.
 * 
 * @author Clairton Rodrigo Heinzen<clairton.rodrigo@gmail.com>
 */
public class NotLike implements Comparator {
	private static final long serialVersionUID = 1L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Predicate build(final CriteriaBuilder cb, final Expression<?> x,
			final Object y) {
		if (y instanceof Collection) {
			return cb.not(x.in(( Collection<?> ) y));
		} else {
			@SuppressWarnings("unchecked")
			final Expression<String> s = (Expression<String>) x;
			return cb.notLike(cb.lower(s), "%" + y.toString() + "%");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "!*";
	}
}
