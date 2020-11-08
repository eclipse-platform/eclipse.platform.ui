package org.eclipse.jface.databinding.conformance.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to collect a list of all conformance tests to run.
 */
public class TestCollection {

	private final List<Object[]> tests = new ArrayList<>();

	/**
	 * @param testClass Expected to have exactly one public constructor, that takes
	 *                  one argument.
	 * @param delegate  Will be passed to the constructor of testClass.
	 */
	public void addTest(Class<?> testClass, Object delegate) {
		tests.add(new Object[] { testClass, delegate });
	}

	public Iterable<Object[]> getDataForParameterizedRunner() {
		return tests;
	}
}
