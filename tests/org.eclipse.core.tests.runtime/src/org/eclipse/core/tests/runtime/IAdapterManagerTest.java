/*******************************************************************************
 *  Copyright (c) 2004, 2012 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime;

import java.io.IOException;
import java.net.MalformedURLException;
import junit.framework.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.BundleTestingHelper;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

/**
 * Tests API on the IAdapterManager class.
 */
public class IAdapterManagerTest extends TestCase {
	//following classes are for testComputeClassOrder
	static interface C {
	}

	static interface D {
	}

	static interface M {
	}

	static interface N {
	}

	static interface O {
	}

	interface A extends M, N {
	}

	interface B extends O {
	}

	class Y implements C, D {
	}

	class X extends Y implements A, B {
	}

	private static final String NON_EXISTING = "com.does.not.Exist";
	private static final String TEST_ADAPTER = "org.eclipse.core.tests.runtime.TestAdapter";
	private static final String TEST_ADAPTER_CL = "testAdapter.testUnknown";
	private IAdapterManager manager;

	public IAdapterManagerTest(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(IAdapterManagerTest.class);
	}

	public IAdapterManagerTest() {
		super("");
	}

	protected void setUp() throws Exception {
		manager = Platform.getAdapterManager();
	}

	/**
	 * Tests API method IAdapterManager.hasAdapter.
	 */
	public void testHasAdapter() {
		TestAdaptable adaptable = new TestAdaptable();
		//request non-existing adaptable
		assertTrue("1.0", !manager.hasAdapter("", NON_EXISTING));

		//request adapter that is in XML but has no registered factory
		assertTrue("1.1", manager.hasAdapter(adaptable, TEST_ADAPTER));

		//request adapter that is not in XML
		assertTrue("1.2", !manager.hasAdapter(adaptable, "java.lang.String"));

		//register an adapter factory that maps adaptables to strings
		IAdapterFactory fac = new IAdapterFactory() {
			public Object getAdapter(Object adaptableObject, Class adapterType) {
				if (adapterType == String.class)
					return adaptableObject.toString();
				return null;
			}

			public Class[] getAdapterList() {
				return new Class[] {String.class};
			}
		};
		manager.registerAdapters(fac, TestAdaptable.class);
		try {
			//request adapter for factory that we've just added
			assertTrue("1.3", manager.hasAdapter(adaptable, "java.lang.String"));
		} finally {
			manager.unregisterAdapters(fac, TestAdaptable.class);
		}

		//request adapter that was unloaded
		assertTrue("1.4", !manager.hasAdapter(adaptable, "java.lang.String"));
	}

	/**
	 * Tests API method IAdapterManager.getAdapter.
	 */
	public void testGetAdapter() {
		TestAdaptable adaptable = new TestAdaptable();
		//request non-existing adaptable
		assertNull("1.0", manager.getAdapter("", NON_EXISTING));

		//request adapter that is in XML but has no registered factory
		Object result = manager.getAdapter(adaptable, TEST_ADAPTER);
		assertTrue("1.1", result instanceof TestAdapter);

		//request adapter that is not in XML
		assertNull("1.2", manager.getAdapter(adaptable, "java.lang.String"));

		//register an adapter factory that maps adaptables to strings
		IAdapterFactory fac = new IAdapterFactory() {
			public Object getAdapter(Object adaptableObject, Class adapterType) {
				if (adapterType == String.class)
					return adaptableObject.toString();
				return null;
			}

			public Class[] getAdapterList() {
				return new Class[] {String.class};
			}
		};
		manager.registerAdapters(fac, TestAdaptable.class);
		try {
			//request adapter for factory that we've just added
			result = manager.getAdapter(adaptable, "java.lang.String");
			assertTrue("1.3", result instanceof String);
		} finally {
			manager.unregisterAdapters(fac, TestAdaptable.class);
		}
		//request adapter that was unloaded
		assertNull("1.4", manager.getAdapter(adaptable, "java.lang.String"));
	}

	public void testGetAdapterNullArgs() {
		TestAdaptable adaptable = new TestAdaptable();
		try {
			manager.getAdapter(adaptable, (Class) null);
			fail("1.0");
		} catch (RuntimeException e) {
			//expected
		}
		try {
			manager.getAdapter(null, NON_EXISTING);
			fail("1.0");
		} catch (RuntimeException e) {
			//expected
		}

	}

	/**
	 * Tests API method IAdapterManager.loadAdapter.
	 */
	public void testLoadAdapter() {
		TestAdaptable adaptable = new TestAdaptable();
		//request non-existing adaptable
		assertNull("1.0", manager.loadAdapter("", NON_EXISTING));

		//request adapter that is in XML but has no registered factory
		Object result = manager.loadAdapter(adaptable, TEST_ADAPTER);
		assertTrue("1.1", result instanceof TestAdapter);

		//request adapter that is not in XML
		assertNull("1.2", manager.loadAdapter(adaptable, "java.lang.String"));

		//register an adapter factory that maps adaptables to strings
		IAdapterFactory fac = new IAdapterFactory() {
			public Object getAdapter(Object adaptableObject, Class adapterType) {
				if (adapterType == String.class)
					return adaptableObject.toString();
				return null;
			}

			public Class[] getAdapterList() {
				return new Class[] {String.class};
			}
		};
		manager.registerAdapters(fac, TestAdaptable.class);
		try {
			//request adapter for factory that we've just added
			result = manager.loadAdapter(adaptable, "java.lang.String");
			assertTrue("1.3", result instanceof String);
		} finally {
			manager.unregisterAdapters(fac, TestAdaptable.class);
		}
		//request adapter that was unloaded
		assertNull("1.4", manager.loadAdapter(adaptable, "java.lang.String"));
	}

	/**
	 * Test adapting to classes not reachable by the default bundle class loader
	 * (bug 200068).
	 * NOTE: This test uses .class file compiled with 1.4 JRE. As a result,
	 * the test can not be run on pre-1.4 JRE.
	 */
	public void testAdapterClassLoader() throws MalformedURLException, BundleException, IOException {
		TestAdaptable adaptable = new TestAdaptable();
		assertTrue(manager.hasAdapter(adaptable, TEST_ADAPTER_CL));
		assertNull(manager.loadAdapter(adaptable, TEST_ADAPTER_CL));
		Bundle bundle = null;
		try {
			bundle = BundleTestingHelper.installBundle("0.1", RuntimeTestsPlugin.getContext(), RuntimeTestsPlugin.TEST_FILES_ROOT + "adapters/testAdapter_1.0.0");
			BundleTestingHelper.refreshPackages(RuntimeTestsPlugin.getContext(), new Bundle[] {bundle});

			assertTrue(manager.hasAdapter(adaptable, TEST_ADAPTER_CL));
			Object result = manager.loadAdapter(adaptable, TEST_ADAPTER_CL);
			assertNotNull(result);
			assertTrue(TEST_ADAPTER_CL.equals(result.getClass().getName()));
		} finally {
			if (bundle != null)
				bundle.uninstall();
		}
	}

	/**
	 * Tests for {@link IAdapterManager#computeClassOrder(Class)}.
	 */
	public void testComputeClassOrder() {
		Class[] expected = new Class[] {X.class, Y.class, Object.class, A.class, B.class, M.class, N.class, O.class, C.class, D.class};
		Class[] actual = manager.computeClassOrder(X.class);
		assertEquals("1.0", expected.length, actual.length);
		for (int i = 0; i < actual.length; i++) {
			assertEquals("1.1." + i, expected[i], actual[i]);
		}
	}
}
