package br.eti.clairton.repository;

import static org.junit.Assert.assertEquals;
import static br.eti.clairton.repository.Comparators.GREATER_THAN_OR_EQUAL;

import java.sql.Connection;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiJUnit4Runner.class)
public class RepositoryTenantIntegrationTest {
	private @Inject EntityManager entityManager;
	private @Inject Repository repository;
	private @Inject Connection connection;

	@Before
	public void init() throws Exception {
		entityManager.getTransaction().begin();
		final String sql = "DELETE FROM operacoes;DELETE FROM recursos;DELETE FROM aplicacoes;";
		connection.createStatement().execute(sql);
		entityManager.getTransaction().commit();

		entityManager.getTransaction().begin();
		final Aplicacao aplicacao = new Aplicacao("Teste");
		final Recurso recurso = new Recurso(aplicacao, "Teste");
		final Operacao operacao = new Operacao(recurso, "Teste");
		entityManager.persist(operacao);
		final Aplicacao aplicacao2 = new Aplicacao(
				"OutroTesteQueNãoDeveAparecerNaConsulta");
		final Recurso recurso2 = new Recurso(aplicacao2, "OutroTeste");
		final Operacao operacao2 = new Operacao(recurso2, "OutroTeste");
		entityManager.persist(operacao2);
		entityManager.getTransaction().commit();
	}

	@Test
	public void testWithTenant() {
		final List<Recurso> result = repository.from(Recurso.class).list();
		assertEquals(1, result.size());
	}

	@Test
	public void testWithTenantInJoin() {
		final List<Operacao> result = repository
				.from(Operacao.class)
				.where(0l, GREATER_THAN_OR_EQUAL, Operacao_.recurso,
						Recurso_.id).list();
		assertEquals(1, result.size());
	}

	@Test
	public void testWithoutTenant() {
		final List<Operacao> result = repository.from(Operacao.class).list();
		assertEquals(2, result.size());
	}
}
