package br.eti.clairton.repository;

import java.sql.Connection;

import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.transaction.TransactionManager;

import org.junit.Before;

public class ComparatorTest {
	protected @Inject Connection connection;
	protected @Inject Repository repository;

	@Before
	public void init() throws Exception {
		final String jndi = "java:/jboss/TransactionManager";
		final InitialContext context = new InitialContext();
		final TransactionManager tm = (TransactionManager) context.lookup(jndi);
		tm.begin();
		final String sql = "DELETE FROM operacoes;DELETE FROM recursos;DELETE FROM aplicacoes;";
		connection.createStatement().execute(sql);
		tm.commit();

		final Aplicacao aplicacao = new Aplicacao("Teste");
		repository.save(aplicacao);
		final Aplicacao aplicacao2 = new Aplicacao("abc");
		repository.save(aplicacao2);

	}
}
