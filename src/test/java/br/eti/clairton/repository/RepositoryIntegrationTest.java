package br.eti.clairton.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.Cache;
import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiJUnit4Runner.class)
public class RepositoryIntegrationTest {
	private @Inject Repository repository;
	private @Inject EntityManager entityManager;
	private @Inject Cache cache;
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
		entityManager.getTransaction().commit();
	}

	@Test
	public void testLast() {
		final Aplicacao aplicacao = repository.from(Aplicacao.class).last();
		assertNotNull(aplicacao);
	}

	@Test
	public void testFirst() {
		final Aplicacao aplicacao = repository.from(Aplicacao.class).first();
		assertNotNull(aplicacao);
	}

	@Test
	public void testRemove() {
		final Operacao operacao = repository.from(Operacao.class).first();
		repository.remove(operacao);
		assertFalse(cache.contains(Aplicacao.class, operacao.getId()));
	}

	@Test
	public void testSave() {
		final Aplicacao aplicacao = repository.from(Aplicacao.class).first();
		aplicacao.setNome("Outro nome");
		repository.save(aplicacao);
		final Aplicacao aplicacaoSaved = repository.byId(aplicacao.getClass(),
				aplicacao.getId());
		// assertTrue(cache.contains(aplicacao.getClass(), aplicacao.getId()));
		assertEquals(aplicacao.getNome(), aplicacaoSaved.getNome());
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
