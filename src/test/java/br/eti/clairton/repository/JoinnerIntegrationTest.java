package br.eti.clairton.repository;

import static org.junit.Assert.assertEquals;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiTestRunner.class)
public class JoinnerIntegrationTest {

	@Inject
	private EntityManager manager;

	private Joinner joinner;
	
	private CriteriaBuilder builder;
	
	private CriteriaQuery<?> query;
	
	private From<?, ?> from;
	
	@Before
	public void init(){
		builder = manager.getCriteriaBuilder();
		query = builder.createQuery();
		from = query.from(Aplicacao.class);
		joinner = new Joinner(builder, from);		
	}
	
	@Test
	public void testJoinWithOne() {
		final Join<?, ?> join1 = joinner.join(from, Aplicacao_.recursos);
		final Join<?, ?> join2 = joinner.join(from, Aplicacao_.recursos);
		assertEquals(join1, join2);
	}
	
	@Test
	public void testJoinWithTwo() {
		assertEquals(0, joinner.getIndex().size());
		joinner.join(Aplicacao_.recursos);
		assertEquals(0, joinner.getIndex().size());
		joinner.join(Aplicacao_.recursos, Recurso_.operacoes);
		assertEquals(1, joinner.getIndex().size());
		joinner.join(Aplicacao_.recursos, Recurso_.operacoes);
		assertEquals(1, joinner.getIndex().size());
	}
	
	@Test
	public void testJoinAndSelectTheSame() {
		assertEquals(0, joinner.getIndex().size());
		joinner.join(Aplicacao_.recursos, Recurso_.operacoes);
		assertEquals(1, joinner.getIndex().size());
		joinner.select(Aplicacao_.recursos, Recurso_.operacoes);
		assertEquals(1, joinner.getIndex().size());
	}
	
	@Test
	public void testJoinAndSelectOneMore() {
		joinner.join(Aplicacao_.recursos);
		assertEquals(0, joinner.getIndex().size());
		joinner.select(Aplicacao_.recursos, Recurso_.operacoes);
		assertEquals(1, joinner.getIndex().size());
	}

	@Test
	public void testJoinAndSelectOneLess() {
		joinner.join(Aplicacao_.recursos, Recurso_.operacoes);
		assertEquals(1, joinner.getIndex().size());
		joinner.select(Aplicacao_.recursos);
		assertEquals(1, joinner.getIndex().size());
	}
}
