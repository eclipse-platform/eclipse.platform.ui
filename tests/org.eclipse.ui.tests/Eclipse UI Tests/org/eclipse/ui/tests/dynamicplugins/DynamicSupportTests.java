/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dynamicplugins;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.ui.tests.leaks.LeakTests;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.1
 */
public class DynamicSupportTests {

	private IExtensionTracker tracker;
	private IExtension e1, e2;
	private Object o1, o2;

	@Before
	public void setUp() throws Exception {
		tracker = new ExtensionTracker();
		IExtension [] elements = Platform.getExtensionRegistry().getExtensionPoint("org.eclipse.ui.views").getExtensions();
		assertNotNull(elements);
		assertFalse(elements.length < 2);
		e1 = elements[0];
		e2 = elements[1];

		o1 = new Object();
		o2 = new WeakReference<>(o1);
	}

	@After
	public void tearDown() throws Exception {
		((ExtensionTracker)tracker).close();
	}

	@Test
	public void testConfigurationElementTracker1() {
		tracker.registerObject(e1, o1, IExtensionTracker.REF_WEAK);
		Object [] results = tracker.getObjects(e1);
		assertNotNull(results);
		assertEquals(1, results.length);
		assertEquals(o1, results[0]);
	}

	@Test
	public void testConfigurationElementTracker2() throws Exception {
		tracker.registerObject(e1, o1, IExtensionTracker.REF_WEAK);
		ReferenceQueue<Object> queue = new ReferenceQueue<>();
		WeakReference<Object> ref = new WeakReference<>(o1, queue);
		o1 = null;
		LeakTests.checkRef(queue, ref);
		Object [] results = tracker.getObjects(e1);
		assertNotNull(results);
		assertEquals(0, results.length);
	}

	@Test
	public void testConfigurationElementTracker3() {
		tracker.registerObject(e2, o2, IExtensionTracker.REF_WEAK);
		Object [] results = tracker.getObjects(e2);
		assertNotNull(results);
		assertEquals(1, results.length);
		assertEquals(o2, results[0]);
	}

	@Test
	public void testConfigurationElementTracker4() throws Exception {
		tracker.registerObject(e1, o1, IExtensionTracker.REF_STRONG);
		ReferenceQueue<Object> queue = new ReferenceQueue<>();
		WeakReference<Object> ref = new WeakReference<>(o1, queue);
		o1 = null;
		assertThrows(Throwable.class, () -> LeakTests.checkRef(queue, ref));
		Object [] results = tracker.getObjects(e1);
		assertNotNull(results);
		assertEquals(1, results.length);
		assertEquals(ref.get(), results[0]);
	}
}
