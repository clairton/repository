package br.eti.clairton.repository;

import static org.junit.Assert.assertEquals;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiTestRunner.class)
public class EndsWithTest extends ComparatorTest{

	@Test
	public void test() {
		final Long count = repository.from(Aplicacao.class)
				.where("c", Comparators.ENDS_WITH, Aplicacao_.nome).count();

		assertEquals(Long.valueOf(1l), count);
	}

}
