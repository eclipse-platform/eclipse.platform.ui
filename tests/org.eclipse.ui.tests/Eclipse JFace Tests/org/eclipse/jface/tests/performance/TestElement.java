package org.eclipse.jface.tests.performance;

public class TestElement {
	String name;

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param index
	 */
	public TestElement(int index) {
		name = "Element " + String.valueOf(index);
	}

	public String getText() {
		return name;
	}
}
