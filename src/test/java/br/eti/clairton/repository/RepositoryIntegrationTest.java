package br.eti.clairton.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiJUnit4Runner.class)
public class RepositoryIntegrationTest {
	private @Inject Repository repository;
	private @Inject EntityManager entityManager;

	@Before
	public void init() throws Exception {
		entityManager.getTransaction().begin();
		final Connection connection = entityManager.unwrap(Connection.class);
		final String sql = "DELETE FROM operacoes;DELETE FROM recursos;DELETE FROM aplicacoes;";
		connection.createStatement().execute(sql);
		entityManager.getTransaction().commit();

		final Aplicacao aplicacao = new Aplicacao("Teste");
		final Recurso recurso = new Recurso(aplicacao, "Teste");
		final Operacao operacao = new Operacao(recurso, "Teste");
		repository.save(operacao);
	}

	@Produces
	@ApplicationScoped
	public EntityManager createEntityManager() {
		return Persistence.createEntityManagerFactory("default")
				.createEntityManager();
	}

	@Test
	public void testSingle() {
		final Aplicacao aplicacao = repository.from(Aplicacao.class).single();
		assertNotNull(aplicacao);
	}

	@Test
	public void testCollection() {
		final Collection<Aplicacao> aplicacoes = repository.from(
				Aplicacao.class).collection();
		assertEquals(1, aplicacoes.size());
	}

	@Test
	public void testList() {
		final List<Aplicacao> aplicacoes = repository.from(Aplicacao.class)
				.list();
		assertEquals(1, aplicacoes.size());
	}

	@Test
	public void testPaginatedCollection() {
		final Collection<Operacao> operacoes = repository.from(Operacao.class)
				.collection(1, 1);
		assertEquals(1, operacoes.size());
	}

	@Test
	public void testCount() {
		assertEquals(Long.valueOf(1), repository.from(Aplicacao.class).count());
	}

	@Test
	public void testWhere() {
		final Predicate filtro = new Predicate("Teste", Operacao_.recurso,
				Recurso_.aplicacao, Aplicacao_.nome);
		final Predicate filtro2 = new Predicate(0l,
				Operators.GREATER_THAN_OR_EQUAL, Operacao_.id);
		final Predicate filtro3 = new Predicate(1000000l,
				Operators.LESS_THAN_OR_EQUAL, Operacao_.recurso, Recurso_.id);
		final Predicate filtro4 = new Predicate("e", Operators.LIKE,
				Operacao_.recurso, Recurso_.nome);
		final Predicate filtro5 = new Predicate("OutroTeste",
				Operators.NOT_EQUAL, Operacao_.nome);
		final Collection<Predicate> filtros = Arrays.asList(filtro, filtro2,
				filtro3, filtro4, filtro5);
		assertEquals(Long.valueOf(1),
				repository.from(Operacao.class).where(filtros).count());
	}

	@Test
	public void testWhereAttribute() {
		assertEquals(
				Long.valueOf(1),
				repository
						.from(Operacao.class)
						.where("Teste", Operacao_.recurso, Recurso_.aplicacao,
								Aplicacao_.nome)
						.where(Operators.NOT_NULL, Operacao_.id)
						.where("Testezinho", Operators.NOT_EQUAL,
								Operacao_.nome).count());
	}
}
