package br.eti.clairton.repository;

import java.sql.Connection;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.naming.InitialContext;
import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;

import org.mockito.Mockito;

@ApplicationScoped
public class Resource {

	private EntityManagerFactory emf;

	private EntityManager em;

	private Connection connection;

	@PostConstruct
	public void init() {
		emf = createEntityManagerFactory();
		em = createEntityManager(emf);
		try {
			connection = createConnection();
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public EntityManagerFactory createEntityManagerFactory() {
		return Persistence.createEntityManagerFactory("default");
	}

	public EntityManager createEntityManager(final @Default EntityManagerFactory emf) {
		return emf.createEntityManager();
	}

	@Produces
	@RequestScoped
	public Cache createCache(final @Default EntityManagerFactory emf) {
		if (emf.getCache() == null) {
			return Mockito.mock(Cache.class);
		} else {
			return emf.getCache();
		}
	}

	private Connection createConnection() throws Exception {
		final String name = "java:/jdbc/datasources/MyDS";
		final InitialContext context = new InitialContext();
		final DataSource dataSource = (DataSource) context.lookup(name);
		return dataSource.getConnection();
	}

	@Produces
	@RequestScoped
	public Connection getConnection() {
		return connection;
	}

	@Produces
	@RequestScoped
	public EntityManager getEm() {
		return em;
	}

	@Produces
	@RequestScoped
	public EntityManagerFactory getEmf() {
		return emf;
	}
}
