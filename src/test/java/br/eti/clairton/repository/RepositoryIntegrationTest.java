package br.eti.clairton.repository;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.Cache;
import javax.persistence.EntityManager;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiTestRunner.class)
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
		final Operacao operacao2 = new Operacao(recurso, "OutraOperacao");
		entityManager.persist(operacao2);
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
	public void testExist() {
		assertTrue(repository.from(Aplicacao.class).exist());
	}

	@Test
	public void testNotExist() {
		assertFalse(repository.from(Aplicacao.class)
				.where(new Predicate("nomequenãoexiste", Aplicacao_.nome))
				.exist());
	}

	@Test
	public void testRemoveOne() {
		entityManager.getTransaction().begin();
		final Operacao operacao = repository.from(Operacao.class).first();
		repository.remove(operacao);
		assertFalse(cache.contains(Aplicacao.class, operacao.getId()));
		entityManager.getTransaction().commit();

	}

	@Test
	public void testRemove() {
		entityManager.getTransaction().begin();
		assertTrue(repository.from(Operacao.class).exist());
		repository.from(Operacao.class).remove();
		assertFalse(repository.from(Operacao.class).exist());
		entityManager.getTransaction().commit();

	}

	@Test
	public void testSaveOne() {
		entityManager.getTransaction().begin();
		final Aplicacao aplicacao = repository.from(Aplicacao.class).first();
		aplicacao.setNome("Outro nome");
		repository.save(aplicacao);
		final Aplicacao aplicacaoSaved = repository.byId(aplicacao.getClass(),
				aplicacao.getId());
		assertEquals(aplicacao.getNome(), aplicacaoSaved.getNome());
		entityManager.getTransaction().commit();

	}

	@Test
	public void testSave() {
		entityManager.getTransaction().begin();
		final Aplicacao aplicacao = repository.from(Aplicacao.class).first();
		aplicacao.setNome("Outro nome");
		repository.save(Arrays.asList(aplicacao));
		final Aplicacao aplicacaoSaved = repository.byId(aplicacao.getClass(),
				aplicacao.getId());
		assertEquals(aplicacao.getNome(), aplicacaoSaved.getNome());
		entityManager.getTransaction().commit();

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
				Comparators.GREATER_THAN_OR_EQUAL, Operacao_.id);
		final Predicate filtro3 = new Predicate(1000000l,
				Comparators.LESS_THAN_OR_EQUAL, Operacao_.recurso, Recurso_.id);
		final Predicate filtro4 = new Predicate("e", Comparators.LIKE,
				Operacao_.recurso, Recurso_.nome);
		final Predicate filtro5 = new Predicate("OutraOperacao",
				Comparators.NOT_EQUAL, Operacao_.nome);
		final List<Predicate> filtros = Arrays.asList(filtro, filtro2, filtro3,
				filtro4, filtro5);
		assertEquals(Long.valueOf(1),
				repository.from(Operacao.class).where(filtros).count());
	}

	@Test
	public void testOrInPredicate() {
		final Predicate filtro = new Predicate("Teste", Operacao_.nome);
		final Predicate filtro2 = new Predicate(Operators.OR, "OutraOperacao",
				Operacao_.nome);
		final Collection<Predicate> filtros = Arrays.asList(filtro, filtro2);
		assertEquals(Long.valueOf(2),
				repository.from(Operacao.class).where(filtros).count());
	}

	@Test
	public void testOr() {
		final Predicate filtro = new Predicate("Teste", Operacao_.nome);
		final Predicate filtro2 = new Predicate("OutraOperacao", Operacao_.nome);
		assertEquals(
				Long.valueOf(2),
				repository.from(Operacao.class).where(asList(filtro))
						.or(filtro2).count());
	}

	@Test
	public void testAnd() {
		final Predicate filtro = new Predicate(0l, Comparators.NOT_EQUAL,
				Operacao_.id);
		final Predicate filtro2 = new Predicate("OutraOperacao",
				Comparators.NOT_EQUAL, Operacao_.nome);
		assertEquals(
				Long.valueOf(1),
				repository.from(Operacao.class).where(filtro)
						.and("Teste", Operacao_.nome).and(filtro2).count());
	}

	@Test
	public void testWhereAttribute() {
		assertEquals(
				Long.valueOf(1),
				repository
						.from(Operacao.class)
						.where("Teste", Operacao_.recurso, Recurso_.aplicacao,
								Aplicacao_.nome)
						.where(Comparators.NOT_NULL, Operacao_.id)
						.where("OutraOperacao", Comparators.NOT_EQUAL,
								Operacao_.nome).count());
	}
}
