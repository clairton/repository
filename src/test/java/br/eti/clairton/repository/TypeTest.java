package br.eti.clairton.repository;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import br.eti.clairton.repository.Order.Direction;

public class TypeTest {

	@Test
	public void test() {
		assertEquals(Direction.ASC, Direction.byString("asc"));
	}

}
