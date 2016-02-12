package br.eti.clairton.repository;

import static br.eti.clairton.repository.Comparators.EQUAL;
import static br.eti.clairton.repository.Concat.Position.BEFORE;
import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiTestRunner.class)
public class ConcatTest extends ComparatorTest{

	@Test
	public void testAfter() {
		final String nome = "ajusfnglafdhjewmyhhalnb," + new Date();
		final String token = ".";
		final Aplicacao aplicacao = new Aplicacao(nome);
		repository.save(aplicacao);

		final Long count = repository.from(Aplicacao.class)
			.where(nome + token, new Concat(EQUAL, token), Aplicacao_.nome)
			.count();

		assertEquals(Long.valueOf(1l), count);
	}

	@Test
	public void testBefore() {
		final String nome = "ajusfnglafdhjewmyhhalnb," + new Date();
		final String token = ".";
		final Aplicacao aplicacao = new Aplicacao(nome);
		repository.save(aplicacao);

		final Long count = repository.from(Aplicacao.class)
			.where(token + nome, new Concat(EQUAL, BEFORE, token), Aplicacao_.nome)
			.count();

		assertEquals(Long.valueOf(1l), count);
	}

}
