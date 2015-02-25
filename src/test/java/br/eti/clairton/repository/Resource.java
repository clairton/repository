package br.eti.clairton.repository;

import java.sql.Connection;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Singleton;
import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mockito.Mockito;

@Singleton
public class Resource {

	private final EntityManagerFactory emf;

	private final EntityManager em;

	private final Connection connection;

	public Resource() {
		emf = createEntityManagerFactory();
		em = createEntityManager(emf);
		connection = createConnection(em);
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

	public Connection createConnection(final @Default EntityManager em) {
		Connection connection;
		em.getTransaction().begin();
		// TODO pegar connexão indendepdente de implementação JPA
		try {
			/*
			 * O hibernate não implementa o entityManager de forma a recuperar a
			 * o connection
			 */
			final Class<?> klass = Class
					.forName("org.hibernate.internal.SessionImpl");
			final Object session = em.unwrap(klass);
			connection = (Connection) klass.getDeclaredMethod("connection")
					.invoke(session);
		} catch (final Exception e) {
			connection = em.unwrap(Connection.class);
		}
		em.getTransaction().commit();
		return connection;
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
