package eu.hellek.createstops.data;

import java.util.LinkedList;
import java.util.List;

public class StringStack {

	private List<String> stack;
	
	public StringStack() {
		stack = new LinkedList<String>();
	}
	
	public void push(String s) {
		stack.add(s);
	}
	
	public String pop() {
		String s = stack.get(stack.size()-1);
		stack.remove(stack.size()-1);
		return s; 
	}
	
}
