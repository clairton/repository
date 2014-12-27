package br.eti.clairton.repository;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiJUnit4Runner.class)
public class TenantBuilderTest {
	private @Inject EntityManager entityManager;
	private @Inject Connection connection;
	private @Inject TenantBuilder tenant;

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
		final CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		final CriteriaQuery<Recurso> query = builder.createQuery(Recurso.class);
		final Root<Recurso> from = query.from(Recurso.class);
		final Predicate predicate = tenant.add(from, builder);
		query.select(from).where(predicate);
		final TypedQuery<Recurso> typedQuery = entityManager.createQuery(query);
		final List<Recurso> result = typedQuery.getResultList();
		assertEquals(1, result.size());
	}

	@Test
	public void testWithoutTenant() {
		final CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		final CriteriaQuery<Operacao> query = builder
				.createQuery(Operacao.class);
		final Root<Operacao> from = query.from(Operacao.class);
		final Predicate predicate = tenant.add(from, builder);
		query.select(from).where(predicate);
		final TypedQuery<Operacao> typedQuery = entityManager
				.createQuery(query);
		final List<Operacao> result = typedQuery.getResultList();
		assertEquals(2, result.size());
	}
}
