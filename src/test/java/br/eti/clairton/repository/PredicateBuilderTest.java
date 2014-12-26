package br.eti.clairton.repository;

import static org.junit.Assert.assertEquals;

import javax.persistence.criteria.JoinType;

import org.junit.Test;

public class PredicateBuilderTest {

	@Test
	public void test() {
		final PredicateBuilder builder = new PredicateBuilder();
		final Predicate predicate = builder.value("abc")
				.comparator(Comparators.NOT_EQUAL).join(JoinType.LEFT)
				.attribute(Aplicacao_.id).build();
		assertEquals(Comparators.NOT_EQUAL, predicate.getComparator());
		assertEquals(JoinType.LEFT, predicate.getJoinType());
		assertEquals("abc", predicate.getValue());
		assertEquals(1, predicate.getAttributes().length);
		assertEquals(Aplicacao_.id, predicate.getAttributes()[0]);
	}

}
