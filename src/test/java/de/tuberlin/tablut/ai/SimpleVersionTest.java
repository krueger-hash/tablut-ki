package de.tuberlin.tablut.ai;

import org.junit.Test;

public class SimpleVersionTest {
    @Test
    public void shouldReturnValidStringRepresentation(){
        SimpleVersion version = new SimpleVersion("Tablut AI", 0.1, true);
        String expected = "Tablut AI v0.1 is alive";
        String actual = version.toString();
        assert expected.equals(actual);
    }
}
