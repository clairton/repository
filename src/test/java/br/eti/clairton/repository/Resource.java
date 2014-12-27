package br.eti.clairton.repository;

import java.sql.Connection;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.Transactional;

import org.mockito.Mockito;

public class Resource {

	@Produces
	@ApplicationScoped
	public EntityManagerFactory createEntityManagerFactory() {
		return Persistence.createEntityManagerFactory("default");
	}

	@Produces
	@ApplicationScoped
	public EntityManager createEntityManager(
			final @Default EntityManagerFactory emf) {
		return emf.createEntityManager();
	}

	@Produces
	@Singleton
	public Cache createCache(final @Default EntityManagerFactory emf) {
		if (emf.getCache() == null) {
			return Mockito.mock(Cache.class);
		} else {
			return emf.getCache();
		}
	}

	@Produces
	@ApplicationScoped
	@Transactional
	public Connection getConnection(final @Default EntityManager em) {
		// TODO pegar connexão indendepdente de implementação JPA
		try {
			/*
			 * O hibernate não implementa o entityManager de forma a recuperar a
			 * o connection
			 */
			final Class<?> klass = Class
					.forName("org.hibernate.internal.SessionImpl");
			final Object session = em.unwrap(klass);
			return (Connection) klass.getDeclaredMethod("connection").invoke(
					session);
		} catch (final Exception e) {
			return em.unwrap(Connection.class);
		}
	}
}