/*******************************************************************************
 * Copyright (c) 2007, 2008 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Matthew Hall - bug 208322
 ******************************************************************************/

package org.eclipse.jface.databinding.conformance.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.eclipse.jface.databinding.conformance.delegate.IObservableContractDelegate;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Builds a test suite.
 * 
 * @since 1.1
 */
public class SuiteBuilder {
	private LinkedHashSet content;

	public SuiteBuilder() {
		content = new LinkedHashSet();
	}

	/**
	 * Adds all test methods from the provided <code>testCase</code> to the
	 * suite.
	 * 
	 * @param testCase
	 * @return builder
	 */
	public SuiteBuilder addTests(Class testCase) {
		content.add(testCase);
		return this;
	}

	/**
	 * Adds all test methods from the provided <code>testCase</code> with the
	 * provided <code>parameters</code>. A constructor must exist in the
	 * testCase that accepts a String as the first parameter followed by
	 * parameters matching the provided parameters. If an appropriate
	 * constructor is not found an exception will be thrown.
	 * 
	 * @param testCase
	 * @param parameters
	 * @return builder
	 */
	public SuiteBuilder addParameterizedTests(Class testCase,
			Object[] parameters) {

		Constructor constructor = findConstructor(testCase, parameters);
		if (constructor == null) {
			throw new IllegalArgumentException(
					"The parameters provided don't match a constructor found in ["
							+ testCase.getName() + "]");
		}

		content.add(new ParameterizedTest(testCase, constructor, parameters));

		return this;
	}

	/**
	 * Convenience method for invoking
	 * {@link #addParameterizedTests(Class, Object[])} with a delegate.
	 * 
	 * @param testCase
	 * @param delegate
	 * @return builder
	 */
	public SuiteBuilder addObservableContractTest(Class testCase,
			IObservableContractDelegate delegate) {

		addParameterizedTests(testCase, new Object[] {delegate});
		return this;
	}

	/**
	 * Builds a new TestSuite out of the tests.
	 * 
	 * @return suite
	 */
	public TestSuite build() {
		TestSuite suite = new TestSuite();

		for (Iterator it = content.iterator(); it.hasNext();) {
			Object o = it.next();
			if (o instanceof Class) {
				suite.addTestSuite((Class) o);
			} else if (o instanceof ParameterizedTest) {
				ParameterizedTest test = (ParameterizedTest) o;

				// Outer test named for parameterized test class
				TestSuite testClassSuite = new TestSuite();
				testClassSuite.setName(test.testClass.getName());

				// Inner test named for parameter
				TestSuite parameterSuite = new TestSuite();
				parameterSuite.setName(test.parameters[0].getClass().getName());
				testClassSuite.addTest(parameterSuite);

				Method[] methods = test.testClass.getMethods();
				for (int i = 0; i < methods.length; i++) {
					String name = methods[i].getName();
					if (name.startsWith("test")) {
						try {
							parameterSuite.addTest((Test) test.constructor
									.newInstance(toParamArray(name,
											test.parameters)));
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
				}

				if (testClassSuite.countTestCases() > 0)
					suite.addTest(testClassSuite);
			}
		}

		return suite;
	}

	private Object[] toParamArray(String testName, Object[] parameters) {
		Object[] result = new Object[parameters.length + 1];
		result[0] = testName;
		System.arraycopy(parameters, 0, result, 1, parameters.length);
		return result;
	}

	/**
	 * Returns the constructor that has a String as the first parameters and
	 * then matches the type of the parameters.
	 * @param clazz 
	 * @param parameters
	 * @return constructor
	 */
	private static Constructor findConstructor(Class clazz, Object[] parameters) {
		Constructor[] constructors = clazz.getConstructors();
		int expectedParametersLength = parameters.length + 1;

		for (int i = 0; i < constructors.length; i++) {
			Constructor constructor = constructors[i];
			Class[] types = constructor.getParameterTypes();

			if (types.length != expectedParametersLength
					|| !String.class.equals(types[0])) {
				continue;
			}

			boolean skip = false;
			for (int j = 1; j < types.length; j++) {
				Class type = types[j];
				if (!type.isInstance(parameters[j - 1])) {
					skip = true;
					break;
				}
			}

			if (!skip) {
				return constructor;
			}
		}

		return null;
	}

	/* package */static class ParameterizedTest {
		final Constructor constructor;

		final Object[] parameters;

		private Class testClass;

		ParameterizedTest(Class testClass, Constructor constructor,
				Object[] parameterss) {
			this.testClass = testClass;
			this.constructor = constructor;
			this.parameters = parameterss;
		}
	}
}
