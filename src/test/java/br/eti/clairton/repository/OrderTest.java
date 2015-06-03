package br.eti.clairton.repository;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class OrderTest {

	@Test
	public void testGetAttributes() {
		final Order order = new Order(Aplicacao_.id, Aplicacao_.nome);
		assertEquals(2, order.getAttributes().size());
	}
}
