/*******************************************************************************
 * Copyright (c) 2008, 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.expressions.tests;

import java.util.ArrayList;
import java.util.Collections;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import org.eclipse.core.internal.expressions.ExpressionPlugin;
import org.eclipse.core.internal.expressions.Expressions;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests for cache used in {@link Expressions#isInstanceOf(Object, String)}.
 * <p>
 * <b>WARNING:</b> These tests start, stop, and re-start the <code>com.ibm.icu</code> bundle.
 * Don't include these in another test suite!
 */
public class ExpressionTestsPluginUnloading extends TestCase {

	public static Test suite() {
		TestSuite suite= new TestSuite(ExpressionTestsPluginUnloading.class);
		// ensure lexicographical ordering:
		ArrayList<Test> tests = Collections.list(suite.tests());
		Collections.sort(tests, (o1, o2) -> ((TestCase) o1).getName().compareTo(((TestCase) o2).getName()));
		TestSuite result= new TestSuite();
		for (Test test : tests) {
			result.addTest(test);
		}
		return result;
	}

	public ExpressionTestsPluginUnloading(String name) {
		super(name);
	}


	public void test01PluginStopping() throws Exception {
		Bundle bundle= getBundle("com.ibm.icu");

		assertEquals(Bundle.STARTING, bundle.getState());

		doTestInstanceofICUDecimalFormat(bundle);
		assertEquals(Bundle.ACTIVE, bundle.getState());

		bundle.stop();
		assertEquals(Bundle.RESOLVED, bundle.getState());

		bundle.start();
		assertEquals(Bundle.ACTIVE, bundle.getState());

		doTestInstanceofICUDecimalFormat(bundle);
	}

	public void test02MultipleClassloaders() throws Exception {
		Bundle expr= getBundle("org.eclipse.core.expressions.tests");
		Bundle icu= getBundle("com.ibm.icu");

		Class<?> exprClass = expr.loadClass("com.ibm.icu.text.DecimalFormat");
		Class<?> icuClass = icu.loadClass("com.ibm.icu.text.DecimalFormat");
		assertNotSame(exprClass, icuClass);

		Object exprObj = exprClass.getDeclaredConstructor().newInstance();
		Object icuObj = icuClass.getDeclaredConstructor().newInstance();

		assertInstanceOf(exprObj, "java.lang.Runnable", "java.lang.String");
		assertInstanceOf(exprObj, "java.lang.Object", "java.io.Serializable");

		assertInstanceOf(icuObj, "java.io.Serializable", "java.lang.String");
		assertInstanceOf(icuObj, "java.text.Format", "java.lang.Runnable");
	}

	private void assertInstanceOf(Object obj, String isInstance, String isNotInstance) throws Exception {
		Class<?> clazz = obj.getClass();

		System.out.println("ExpressionTestsPluginUnloading#" + getName() + "() - " + clazz.getName() + ": " + clazz.hashCode());
		System.out.println("ExpressionTestsPluginUnloading#" + getName() + "() - ClassLoader: " + clazz.getClassLoader().hashCode());

		for (int i= 0; i < 2; i++) { // test twice, second time is cached:
			assertTrue(Expressions.isInstanceOf(obj, isInstance));
			assertFalse(Expressions.isInstanceOf(obj, isNotInstance));
		}
	}

	private void doTestInstanceofICUDecimalFormat(Bundle bundle) throws Exception {
		Class<?> clazz = bundle.loadClass("com.ibm.icu.text.DecimalFormat");
		Object decimalFormat = clazz.getDeclaredConstructor().newInstance();
		assertInstanceOf(decimalFormat, "com.ibm.icu.text.DecimalFormat", "java.text.NumberFormat");
	}

	private static Bundle getBundle(String bundleName) {
		BundleContext bundleContext= ExpressionPlugin.getDefault().getBundleContext();
		Bundle[] bundles= bundleContext.getBundles();
		for (Bundle bundle : bundles) {
			if (bundleName.equals(bundle.getSymbolicName())) {
				return bundle;
			}
		}
		fail("Could not find bundle: " + bundleName);
		return null;
	}
}
