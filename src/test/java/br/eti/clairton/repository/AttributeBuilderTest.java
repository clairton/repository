package br.eti.clairton.repository;

import static org.junit.Assert.assertEquals;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.metamodel.Attribute;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
	public void testWithColchete() {
		final Attribute<?, ?>[] attributes = attributeBuilder.with(
				Operacao.class, "recurso[aplicacao][nome]");
		assertEquals(3, attributes.length);
		// assertTrue(Operacao_.recurso.equals(attributes[1]));
		// assertTrue(Recurso_.aplicacao.equals(attributes[1]));
		// assertTrue(Aplicacao_.nome.equals(attributes[2]));
	}

	@Test
	public void testWith1() {
		final Attribute<?, ?>[] attributes = attributeBuilder.with(
				Operacao.class, "recurso");
		assertEquals(1, attributes.length);
	}
}
