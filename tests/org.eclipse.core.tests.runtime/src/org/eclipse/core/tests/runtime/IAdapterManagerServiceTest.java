/*******************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime;

import junit.framework.*;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IAdapterManager;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Tests API on the IAdapterManager class accessed via an OSGi service.
 * 
 * This class is a copy of IAdapterManagerTest modified to use an OSGi service
 * instead of the Platform API.
 */
public class IAdapterManagerServiceTest extends TestCase {
	private static final String NON_EXISTING = "com.does.not.Exist";
	private static final String TEST_ADAPTER = "org.eclipse.core.tests.runtime.TestAdapter";

	private ServiceTracker adapterManagerTracker = null;

	public IAdapterManagerServiceTest(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(IAdapterManagerServiceTest.class);
	}

	public IAdapterManagerServiceTest() {
		super("");
	}

	/*
	 * Return the framework log service, if available.
	 */
	public IAdapterManager getAdapterManager() {
		if (adapterManagerTracker == null) {
			BundleContext context = RuntimeTestsPlugin.getContext();
			adapterManagerTracker = new ServiceTracker(context, IAdapterManager.class.getName(), null);
			adapterManagerTracker.open();
		}
		return (IAdapterManager) adapterManagerTracker.getService();
	}

	protected void tearDown() throws Exception {
		if (adapterManagerTracker != null) {
			adapterManagerTracker.close();
			adapterManagerTracker = null;
		}
		super.tearDown();
	}

	/**
	 * Tests API method IAdapterManager.hasAdapter.
	 */
	public void testHasAdapter() {
		IAdapterManager manager = getAdapterManager();

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
		IAdapterManager manager = getAdapterManager();

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
		IAdapterManager manager = getAdapterManager();

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
