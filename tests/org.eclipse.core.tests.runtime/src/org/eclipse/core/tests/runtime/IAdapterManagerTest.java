/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.runtime;

import junit.framework.*;
import junit.framework.TestCase;
import org.eclipse.core.runtime.*;

/**
 * Tests API on the IAdapterManager class.
 */
public class IAdapterManagerTest extends TestCase {
	private static final String NON_EXISTING = "com.does.not.Exist";
	private static final String TEST_ADAPTER = "org.eclipse.core.tests.runtime.TestAdapter";
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
}