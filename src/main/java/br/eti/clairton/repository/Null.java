package br.eti.clairton.repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

/**
 * Cria o {@link Predicate} nulo.
 * 
 * @author Clairton Rodrigo Heinzen<clairton.gmail@gmail.com>
 */
public class Null implements Comparator {
	private static final long serialVersionUID = 1L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Predicate build(CriteriaBuilder cb, Expression<?> x, Object y) {
		return cb.isNull(x);
	}

	@Override
	public String toString() {
		return "IS NULL";
	}
}
