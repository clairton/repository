package br.eti.clairton.repository;

import static org.junit.Assert.assertEquals;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.metamodel.Attribute;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

//@RunWith(CdiJUnit4Runner.class)
public class AttributeBuilderTest {

	private AttributeBuilder attributeBuilder;
	private static EntityManagerFactory emf;

	@BeforeClass
	public static void setUp() {
		emf = Persistence.createEntityManagerFactory("default");
	}

	@Before
	public void init() {
		final EntityManager em = emf.createEntityManager();
		attributeBuilder = new AttributeBuilder(em);
	}

	@Test
	public void testAdd() {
		final Attribute<?, ?>[] attributes = attributeBuilder
				.add(Operacao_.recurso).add(Recurso_.aplicacao)
				.add(Aplicacao_.nome).toArray();
		assertEquals(3, attributes.length);
	}

	@Test
	public void testWith3() {
		final Attribute<?, ?>[] attributes = attributeBuilder.with(
				Operacao.class, "recurso.aplicacao.nome");
		assertEquals(3, attributes.length);
	}

	@Test
	public void testWith2() {
		final Attribute<?, ?>[] attributes = attributeBuilder.with(
				Operacao.class, "recurso.nome");
		assertEquals(2, attributes.length);
	}

	@Test
	public void testWith1() {
		final Attribute<?, ?>[] attributes = attributeBuilder.with(
				Operacao.class, "recurso");
		assertEquals(1, attributes.length);
	}
}
