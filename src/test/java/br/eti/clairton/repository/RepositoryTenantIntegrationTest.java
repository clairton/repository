package br.eti.clairton.repository;

import static br.eti.clairton.repository.Comparators.GREATER_THAN_OR_EQUAL;
import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.util.List;

import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.transaction.TransactionManager;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiTestRunner.class)
public class RepositoryTenantIntegrationTest {
	private @Inject EntityManager entityManager;
	private @Inject @Tenant Repository repository;
	private @Inject Connection connection;
	private static final String tenantValue = "OutroTesteQueNãoDeveAparecerNaConsulta";

	@Before
	public void init() throws Exception {
		final InitialContext context = new InitialContext();
		final TransactionManager tm = (TransactionManager) context.lookup("java:/jboss/TransactionManager");
		tm.begin();
		final String sql = "DELETE FROM operacoes;DELETE FROM recursos;DELETE FROM aplicacoes;";
		connection.createStatement().execute(sql);

		final Aplicacao aplicacao = new Aplicacao("Teste");
		final Recurso recurso = new Recurso(aplicacao, "Teste");
		final Operacao operacao = new Operacao(recurso, "Teste");
		entityManager.persist(operacao);
		final Aplicacao aplicacao2 = new Aplicacao(tenantValue);
		final Recurso recurso2 = new Recurso(aplicacao2, "OutroTeste");
		final Operacao operacao2 = new Operacao(recurso2, "OutroTeste");
		entityManager.persist(operacao2);
		entityManager.joinTransaction();
		entityManager.flush();
		entityManager.clear();
		tm.commit();
	}

	@Test
	public void testWithTenantInFirst() {
		final List<Recurso> result = repository.tenantValue(tenantValue)
				.from(Aplicacao.class).list();
		assertEquals(1, result.size());
	}

	@Test
	public void testWithTenantInSecond() {
		final List<Recurso> result = repository.tenantValue(tenantValue)
				.from(Recurso.class).list();
		assertEquals(1, result.size());
	}

	@Test
	public void testWithTenantInJoin() {
		final List<Operacao> result = repository
				.tenantValue(tenantValue)
				.from(Operacao.class)
				.where(0l, GREATER_THAN_OR_EQUAL, Operacao_.recurso,
						Recurso_.id).list();
		assertEquals(1, result.size());
	}

	@Test
	public void testWithoutTenant() {
		final List<Operacao> result = repository.tenantValue(tenantValue)
				.from(Operacao.class).list();
		assertEquals(2, result.size());
	}
}
