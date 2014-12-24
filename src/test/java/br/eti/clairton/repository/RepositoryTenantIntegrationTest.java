package br.eti.clairton.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.Connection;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

//@RunWith(CdiJUnit4Runner.class)
public class RepositoryTenantIntegrationTest {
	private @Inject EntityManager em;
	private @Inject Repository repository;
	private @Inject Connection connection;

//	@Before
	public void setUp() throws Exception {
		em.getTransaction().begin();
		final String sql = "DELETE FROM operacoes;DELETE FROM recursos;DELETE FROM aplicacoes;";
		connection.createStatement().execute(sql);

		final Aplicacao aplicacao = new Aplicacao("Teste");
		final Recurso recurso = new Recurso(aplicacao, "Teste");
		final Operacao operacao = new Operacao(recurso, "Teste");
		repository.save(operacao);
		em.getTransaction().commit();
	}

//	@Test
	public void test() {
		final CriteriaBuilder cb = em.getCriteriaBuilder();
		final CriteriaQuery<Aplicacao> query = cb.createQuery(Aplicacao.class);
		final TypedQuery<Aplicacao> typedQuery = em.createQuery(query);
		assertEquals(1, typedQuery.getResultList().size());
		assertNotNull(em);
	}
}
