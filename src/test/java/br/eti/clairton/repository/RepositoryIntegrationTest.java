package br.eti.clairton.repository;

import static br.eti.clairton.repository.Comparators.GREATER_THAN;
import static br.eti.clairton.repository.Comparators.GREATER_THAN_OR_EQUAL;
import static br.eti.clairton.repository.Comparators.LESS_THAN_OR_EQUAL;
import static br.eti.clairton.repository.Comparators.LIKE;
import static br.eti.clairton.repository.Comparators.NOT_EQUAL;
import static br.eti.clairton.repository.Operacao_.recurso;
import static br.eti.clairton.repository.Recurso_.aplicacao;
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
import javax.naming.InitialContext;
import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.transaction.TransactionManager;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.eti.clairton.paginated.collection.Meta;
import br.eti.clairton.paginated.collection.PaginatedCollection;
import net.vidageek.mirror.dsl.Mirror;

@RunWith(CdiTestRunner.class)
public class RepositoryIntegrationTest {
	private @Inject Repository repository;
	private @Inject EntityManager manager;
	private @Inject EntityManager entityManager;
	private @Inject Cache cache;
	private @Inject Connection connection;
	private final Aplicacao aplicacaoo = new Aplicacao("Teste");

	@Before
	public void init() throws Exception {
		final InitialContext context = new InitialContext();
		final TransactionManager tm = (TransactionManager) context
				.lookup("java:/jboss/TransactionManager");
		tm.begin();
		final String sql = "DELETE FROM operacoes;DELETE FROM recursos;DELETE FROM aplicacoes;";
		connection.createStatement().execute(sql);

		final Recurso recurso = new Recurso(aplicacaoo, "Teste");
		final Operacao operacao = new Operacao(recurso, "Teste");
		entityManager.persist(operacao);
		final Operacao operacao2 = new Operacao(recurso, "OutraOperacao");
		entityManager.persist(operacao2);
		entityManager.joinTransaction();
		entityManager.flush();
		entityManager.clear();
		tm.commit();
	}

	@Test
	public void testRemove() throws Exception {
		assertTrue(repository.from(Operacao.class).exist());
		repository.from(Operacao.class).remove();
		assertFalse(repository.from(Operacao.class).exist());
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
	public void testUpdate() {
		int antes = repository.from(Aplicacao.class).count().intValue();
		final Aplicacao aplicacao = new Aplicacao("Ainda outro Outro nome");
		new Mirror().on(aplicacao).set().field("id").withValue(aplicacaoo.getId()); 
		repository.save(aplicacao);
		int depois = repository.from(Aplicacao.class).count().intValue();
		assertEquals(antes, depois);
		final Aplicacao aplicacaoSaved = manager.find(aplicacao.getClass(), aplicacao.getId());
		assertEquals(aplicacao.getNome(), aplicacaoSaved.getNome());
	}

	@Test
	public void testFetch() {
		final List<Aplicacao> aplicacoes = repository
				.from(Aplicacao.class)
				.fetch(Aplicacao_.recursos, Recurso_.operacoes)
				.list(1,10);
		assertFalse(aplicacoes.isEmpty());
	}

	@Test
	public void testExist() {
		assertTrue(repository.from(Aplicacao.class).exist());
	}

	@Test
	public void testNotExist() {
		assertTrue(repository.from(Aplicacao.class)
				.where(new Predicate("nomequenãoexiste", Aplicacao_.nome))
				.notExist());
	}

	@Test
	public void testRemoveOne() {
		final Operacao operacao = repository.from(Operacao.class).first();
		repository.remove(operacao);
		assertFalse(cache.contains(Operacao.class, operacao.getId()));
		assertFalse(repository.from(Operacao.class)
				.where(operacao.getId(), Operacao_.id).exist());
	}

	@Test
	public void testSaveOne() {
		final Aplicacao aplicacao = repository.from(Aplicacao.class).first();
		aplicacao.setNome("Outro nome");
		repository.save(aplicacao);
		final Aplicacao aplicacaoSaved = repository.byId(aplicacao.getClass(),
				aplicacao.getId());
		assertEquals(aplicacao.getNome(), aplicacaoSaved.getNome());

	}

	@Test
	public void testPersist() {
		final Aplicacao aplicacao = repository.from(Aplicacao.class).first();
		aplicacao.setNome("Outro nome");
		repository.persist(aplicacao);
		final Aplicacao aplicacaoSaved = repository.byId(aplicacao.getClass(),
				aplicacao.getId());
		assertEquals(aplicacao.getNome(), aplicacaoSaved.getNome());
	}

	@Test
	public void testMerge() {
		final Aplicacao aplicacao = repository.from(Aplicacao.class).first();
		aplicacao.setNome("Outro nome");
		repository.merge(aplicacao);
		final Aplicacao aplicacaoSaved = repository.byId(aplicacao.getClass(),
				aplicacao.getId());
		assertEquals(aplicacao.getNome(), aplicacaoSaved.getNome());
	}

	@Test
	public void testSave() {
		final Aplicacao aplicacao = repository.from(Aplicacao.class).first();
		aplicacao.setNome("Outro nome");
		repository.save(Arrays.asList(aplicacao));
		final Aplicacao aplicacaoSaved = repository.byId(aplicacao.getClass(),
				aplicacao.getId());
		assertEquals(aplicacao.getNome(), aplicacaoSaved.getNome());
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
	public void testListAplicacapAtravesDeRecurso() {
		final List<Aplicacao> aplicacoes = repository
				.from(Recurso.class)
				.where(1, Comparators.GREATER_THAN_OR_EQUAL, Recurso_.id)
				.select(Recurso_.aplicacao) 
				.list();
		assertEquals(1, aplicacoes.size());
		assertEquals("Teste", aplicacoes.get(0).getNome());
	}
	
	@Test
	public void testListOperacoesAtravesDeAplicacao() {
		final List<Recurso> recursos = repository
				.from(Aplicacao.class)
				.where(1, Comparators.GREATER_THAN_OR_EQUAL, Aplicacao_.id)
				.select(Aplicacao_.recursos) 
				.list();
		assertEquals(1, recursos.size());
		assertEquals("Teste", recursos.get(0).getNome());
	}
	
	@Test
	public void testListNomeOperacoesAtravesDeOperacaoEAplicacao() {
		final List<String> recursos = repository
				.from(Aplicacao.class)
				.select(Aplicacao_.recursos, Recurso_.operacoes, Operacao_.nome)
				.where(1, Comparators.GREATER_THAN_OR_EQUAL, Aplicacao_.id)
				.list();
		assertEquals(2, recursos.size());
	}

	@Test
	public void testListNomeRecursosAtravesDeAplicacao() {
		final List<String> recursos = repository
				.from(Aplicacao.class)
				.select(Aplicacao_.recursos, Recurso_.nome)
				.where(1, Comparators.GREATER_THAN_OR_EQUAL, Aplicacao_.id)
				.list();
		assertEquals(1, recursos.size());
		assertEquals("Teste", recursos.get(0));
	}

	@Test
	public void testList() {
		final List<Aplicacao> aplicacoes = repository.from(Aplicacao.class).list();
		assertEquals(1, aplicacoes.size());
	}

	@Test
	public void testSelect() {
		final List<Recurso> recursos = repository
									.from(Aplicacao.class)
									.where(1, GREATER_THAN, Aplicacao_.recursos, Recurso_.id)
									.and(1, GREATER_THAN, Aplicacao_.id)
									.select(Aplicacao_.recursos)
									.list();
		assertEquals(1, recursos.size());
		assertNotNull(recursos.iterator().next().getNome());
	}
	
	@Test
	public void testOneCallOrderBy() {
		final List<Operacao> operacoes = repository.from(Operacao.class)
				.orderBy(Order.Direction.ASC, Operacao_.recurso, Recurso_.nome)
				.list();
		assertEquals(2, operacoes.size());
	}
	
	@Test
	public void testOrderByCollection() {
		final List<Operacao> operacoes = repository.from(Recurso.class)
				.orderBy(Order.Direction.ASC, Recurso_.operacoes, Operacao_.nome)
				.list();
		assertEquals(2, operacoes.size());
	}

	@Test
	public void testCallListOrderBy() {
		final List<Operacao> operacoes = repository.from(Operacao.class)
				.orderBy(Arrays.asList(new Order(Order.Direction.ASC, Operacao_.nome), 
						new Order(Order.Direction.DESC, Operacao_.id)))
				.list();
		assertEquals(2, operacoes.size());
		assertEquals("OutraOperacao", operacoes.get(0).getNome());
		assertEquals("Teste", operacoes.get(1).getNome());
	}
	
	@Test
	public void testCallArrayOrderBy() {
		final List<Operacao> operacoes = repository.from(Operacao.class)
				.orderBy(new Order(Order.Direction.ASC, Operacao_.nome), 
						new Order(Order.Direction.DESC, Operacao_.id))
				.list();
		assertEquals(2, operacoes.size());
		assertEquals("OutraOperacao", operacoes.get(0).getNome());
		assertEquals("Teste", operacoes.get(1).getNome());
	}
	
	@Test
	public void testMoreOneOrderBy() {
		final List<Operacao> operacoes = repository.from(Operacao.class)
				.orderBy(Order.Direction.ASC, Operacao_.nome)
				.orderBy(Order.Direction.DESC, Operacao_.id)
				.list();
		assertEquals(2, operacoes.size());
		assertEquals("OutraOperacao", operacoes.get(0).getNome());
		assertEquals("Teste", operacoes.get(1).getNome());
	}

	@Test
	public void testPaginatedCollection() {
		final PaginatedCollection<Operacao, Meta> operacoes = repository.from(Operacao.class).collection(1, 1);
		assertEquals(1, operacoes.size());
		assertEquals(Long.valueOf(2), operacoes.unwrap(Meta.class).getTotal());
		assertEquals(Long.valueOf(1), operacoes.unwrap(Meta.class).getPage());
	}

	@Test
	public void testPaginatedList() {
		final PaginatedCollection<Operacao, Meta> operacoes = repository.from(Operacao.class).list(2, 1);
		assertEquals(1, operacoes.size());
		assertEquals(Long.valueOf(2), operacoes.unwrap(Meta.class).getTotal());
		assertEquals(Long.valueOf(2), operacoes.unwrap(Meta.class).getPage());
	}

	@Test
	public void testCount() {
		assertEquals(Long.valueOf(1), repository.from(Aplicacao.class).count());
	}

	@Test
	public void testWhere() {
		assertEquals(Long.valueOf(1),
				repository.from(Operacao.class)
							.where("Teste", recurso, aplicacao, Aplicacao_.nome)
							.and(0l, GREATER_THAN_OR_EQUAL, Operacao_.id)
							.and(1000000l, LESS_THAN_OR_EQUAL, recurso, Recurso_.id)
							.and("e", LIKE, recurso, Recurso_.nome)
							.and("OutraOperacao", NOT_EQUAL, Operacao_.nome)
							.count());
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
						.where("Teste", Operacao_.recurso, Recurso_.aplicacao, Aplicacao_.nome)
						.where(Comparators.NOT_NULL, Operacao_.id)
						.where("OutraOperacao", Comparators.NOT_EQUAL, Operacao_.nome).count());
	}
	
  	@Test
  	public void testListNomeRecursosENomeAplicacaoAtravesDeAplicacao() {
  		final List<NomeRecursoENomeAplicacao> objects = repository
  				.from(Aplicacao.class, NomeRecursoENomeAplicacao.class)
  				.select(Aplicacao_.descricao)
  				.select(Aplicacao_.recursos, Recurso_.nome)
  				.where(1, Comparators.GREATER_THAN_OR_EQUAL, Aplicacao_.id)
  				.list();
  		assertEquals(1, objects.size());
  		assertEquals("Teste", objects.get(0).nome);
  		assertEquals(null, objects.get(0).descricao);
  	}
	
  	@Test
  	public void testListNomeRecursosENomeAplicacaoAtravesDeAplicacaoComTuple() {
  		final List<Tuple> objects = repository
  				.from(Aplicacao.class, Tuple.class)
  				.select(Aplicacao_.descricao)
  				.select(Aplicacao_.recursos, Recurso_.nome)
  				.where(1, Comparators.GREATER_THAN_OR_EQUAL, Aplicacao_.id)
  				.list();
  		assertEquals(1, objects.size());
  		assertEquals("Teste", objects.get(0).get(1));
  		assertEquals(null, objects.get(0).get(0));
  	}
	
  	@Test
  	public void testListNomeRecursosENomeAplicacaoAtravesDeAplicacaoComoObjeto() {
  		final List<Object[]> objects = repository
  				.from(Aplicacao.class, Object[].class)
  				.select(Aplicacao_.descricao)
  				.select(Aplicacao_.recursos, Recurso_.nome)
  				.where(1, Comparators.GREATER_THAN_OR_EQUAL, Aplicacao_.id)
  				.list();
  		assertEquals(1, objects.size());
  		assertEquals("Teste", objects.get(0)[1]);
  		assertEquals(null, objects.get(0)[0]);
  	}	
	
  	@Test
  	public void testComoObjeto() {
  		final List<?> objects = repository
  				.from(Aplicacao.class, Object[].class)
  				.select(Aplicacao_.nome)
  				.where(1, Comparators.GREATER_THAN_OR_EQUAL, Aplicacao_.id)
  				.list();
  		assertEquals(1, objects.size());
  	}
}
