package br.eti.clairton.repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public class EqualIgnoreCase implements Comparator {
	private static final long serialVersionUID = 1L;

	@Override
	public Predicate build(final CriteriaBuilder cb, final Expression<?> x,
			final Object y) {
		try {
			@SuppressWarnings("unchecked")
			final Expression<String> es = (Expression<String>) x;
			final String s = (String) y;
			return cb.equal(cb.lower(es), s.toLowerCase());
		} catch (final ClassCastException e) {
			final String message = "Erro ao comparar ignorando caixa alta para"
					+ x + "valor " + y;
			throw new RepositoryQueryException(message, e);
		}
	}

	@Override
	public String toString() {
		return "=~";
	}
}
