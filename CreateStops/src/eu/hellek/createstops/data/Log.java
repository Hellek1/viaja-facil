package eu.hellek.createstops.data;

public class Log {
	
	public static void i(String s) {
		System.err.println(s);
	}
	
	public static void i(String s1, String s2) {
		String s = s1 + ": " + s2;
		System.err.println(s);
	}
	
	public static void d(String s1, String s2) {
		i(s1, s2);
	}
}
