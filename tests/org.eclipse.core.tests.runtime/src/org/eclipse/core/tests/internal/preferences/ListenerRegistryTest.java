/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.preferences;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.preferences.ListenerRegistry;
import org.eclipse.core.tests.runtime.RuntimeTest;

/**
 * @since 3.1
 */
public class ListenerRegistryTest extends RuntimeTest {

	public ListenerRegistryTest(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(ListenerRegistryTest.class);
		//		TestSuite suite = new TestSuite();
		//		suite.addTest(new ListenerRegistryTest("test"));
		//		return suite;
	}

	public void test() {
		ListenerRegistry registry = new ListenerRegistry();
		String key = "/my/path";

		// empty
		Object[] listeners = registry.getListeners(key);
		assertNotNull("1.0", listeners);
		assertEquals("1.1", 0, listeners.length);

		// add a listener
		Object myListener = new Object();
		registry.add(key, myListener);
		listeners = registry.getListeners(key);
		assertNotNull("2.0", listeners);
		assertEquals("2.1", 1, listeners.length);
		assertSame("2.2", myListener, listeners[0]);

		// remove it
		registry.remove(key, myListener);
		listeners = registry.getListeners(key);
		assertNotNull("3.0", listeners);
		assertEquals("3.1", 0, listeners.length);

		// add two
		Object myOtherListener = new Object();
		registry.add(key, myListener);
		registry.add(key, myOtherListener);
		listeners = registry.getListeners(key);
		assertNotNull("4.0", listeners);
		assertEquals("4.1", 2, listeners.length);
		assertTrue("4.2", myListener == listeners[0] || myListener == listeners[1]);
		assertTrue("4.3", myOtherListener == listeners[0] || myOtherListener == listeners[1]);

		// remove one
		registry.remove(key, myListener);
		listeners = registry.getListeners(key);
		assertNotNull("5.0", listeners);
		assertEquals("5.1", 1, listeners.length);
		assertSame("5.2", myOtherListener, listeners[0]);
	}

}
