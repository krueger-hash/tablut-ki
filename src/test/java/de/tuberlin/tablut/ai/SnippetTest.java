package de.tuberlin.tablut.ai;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SnippetTest {

	@Test
	public void testAdd(){
		Snippet s = new Snippet();
		assertEquals(5,Snippet.add(2,3));
	}
}
