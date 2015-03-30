package br.eti.clairton.repository;

import java.util.Collection;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public class Like implements Comparator {
	private static final long serialVersionUID = 1L;

	@Override
	public Predicate build(CriteriaBuilder cb, Expression<?> x, Object y) {
		if (y instanceof Collection) {
			return x.in((Collection<?>) y);
		} else {
			@SuppressWarnings("unchecked")
			Expression<String> s = (Expression<String>) x;
			return cb.like(cb.lower(s), "%" + y.toString().toLowerCase() + "%");
		}
	}

	@Override
	public String toString() {
		return "*";
	}
}
