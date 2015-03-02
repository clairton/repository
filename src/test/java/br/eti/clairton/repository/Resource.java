package br.eti.clairton.repository;

import java.sql.Connection;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Singleton;
import javax.naming.InitialContext;
import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mockito.Mockito;

@Singleton
public class Resource {

	private EntityManagerFactory emf;

	private EntityManager em;

	private Connection connection;

	@PostConstruct
	public void init() throws Exception {
		emf = createEntityManagerFactory();
		em = createEntityManager(emf);
		connection = createConnection();
	}

	public EntityManagerFactory createEntityManagerFactory() {
		return Persistence.createEntityManagerFactory("default");
	}

	public EntityManager createEntityManager(
			final @Default EntityManagerFactory emf) {
		return emf.createEntityManager();
	}

	@Produces
	public Cache createCache(final @Default EntityManagerFactory emf) {
		if (emf.getCache() == null) {
			return Mockito.mock(Cache.class);
		} else {
			return emf.getCache();
		}
	}

	@Produces
	public Logger produceLogger(final InjectionPoint injectionPoint) {
		final Class<?> type = injectionPoint.getMember().getDeclaringClass();
		final String klass = type.getName();
		return LogManager.getLogger(klass);
	}

	public Connection createConnection() throws Exception {
		final String name = "java:/jdbc/datasources/MyDS";
		final InitialContext context = new InitialContext();
		final DataSource dataSource = (DataSource) context.lookup(name);
		return dataSource.getConnection();
	}

	@Produces
	public Connection getConnection() {
		return connection;
	}

	@Produces
	public EntityManager getEm() {
		return em;
	}

	@Produces
	public EntityManagerFactory getEmf() {
		return emf;
	}
}
