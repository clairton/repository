package br.eti.clairton.repository;

import static org.junit.Assert.*;

import org.junit.Test;

import br.eti.clairton.repository.Order.Type;

public class TypeTest {

	@Test
	public void test() {
		assertEquals(Type.ASC, Type.byString("asc"));
	}

}
