package eu.hellek.gba.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class PointTest {

	@Test
	public void testPoint() {
		Point p = new Point("Strasse", -34.0F, -54.0F, null);
		assertEquals(-34.0F, p.getLatlon().getLatitude(), 0.001F);
	}
}
