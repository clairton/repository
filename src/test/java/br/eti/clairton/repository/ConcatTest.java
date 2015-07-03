package br.eti.clairton.repository;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.eti.clairton.repository.Concat.Type;

@RunWith(CdiTestRunner.class)
public class ConcatTest extends ComparatorTest{

	@Test
	public void testAfter() {
		final String nome = "ajusfnglafdhjewmyhhalnb," + new Date();
		final String token = ".";
		final Aplicacao aplicacao = new Aplicacao(nome);
		repository.save(aplicacao);

		final Long count = repository.from(Aplicacao.class)
			.where(nome + token, new Concat(Comparators.EQUAL, token), Aplicacao_.nome)
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
			.where(token + nome, new Concat(Comparators.EQUAL, Type.BEFORE, token), Aplicacao_.nome)
			.count();

		assertEquals(Long.valueOf(1l), count);
	}

}
