package br.eti.clairton.repository;

import static javax.persistence.Persistence.createEntityManagerFactory;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.junit.Before;
import org.junit.Test;

public class RepositoryIntegrationTest {
	private Repository repository;

	@Before
	public void init() {
		EntityManagerFactory emf = createEntityManagerFactory("default");
		final EntityManager em = emf.createEntityManager();
		final Aplicacao aplicacao = new Aplicacao("Teste");
		final Recurso recurso = new Recurso(aplicacao, "Teste");
		final Operacao operacao = new Operacao(recurso, "Teste");
		repository = new Repository(em);
		em.getTransaction().begin();
		em.persist(aplicacao);
		em.persist(recurso);
		em.persist(operacao);
		em.flush();
		em.getTransaction().commit();
		em.clear();
		em.getTransaction().begin();
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
		final Predicate filtro5 = new Predicate("OutroTeste", Operators.NOT_EQUAL,
				Operacao_.nome);
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
						.where("Testezinho", Operators.NOT_EQUAL, Operacao_.nome)
						.count());
	}
}
