/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dynamicplugins;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.ui.tests.leaks.LeakTests;

/**
 * @since 3.1
 */
public class DynamicSupportTests extends TestCase {

	private IExtensionTracker tracker;
	private IExtension e1, e2;
	private Object o1, o2;

	/**
	 * @param name
	 */
	public DynamicSupportTests(String name) {
		super(name);
	}
	
	
	protected void setUp() throws Exception {
		super.setUp();
		tracker = new ExtensionTracker();		
		IExtension [] elements = Platform.getExtensionRegistry().getExtensionPoint("org.eclipse.ui.views").getExtensions();
		assertNotNull(elements);
		assertFalse(elements.length < 2);
		e1 = elements[0];
		e2 = elements[1];
		
		o1 = new Object();
		o2 = new WeakReference(o1); 
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		((ExtensionTracker)tracker).close();
	}
	
	public void testConfigurationElementTracker1() {
		tracker.registerObject(e1, o1, IExtensionTracker.REF_WEAK);
		Object [] results = tracker.getObjects(e1);
		assertNotNull(results);
		assertEquals(1, results.length);
		assertEquals(o1, results[0]);
	}
	
	public void testConfigurationElementTracker2() throws Exception {
		tracker.registerObject(e1, o1, IExtensionTracker.REF_WEAK);
		ReferenceQueue queue = new ReferenceQueue();
		WeakReference ref = new WeakReference(o1, queue);
		o1 = null;
		LeakTests.checkRef(queue, ref);
		Object [] results = tracker.getObjects(e1);
		assertNotNull(results);
		assertEquals(0, results.length);
	}
	
	public void testConfigurationElementTracker3() {
		tracker.registerObject(e2, o2, IExtensionTracker.REF_WEAK);
		Object [] results = tracker.getObjects(e2);
		assertNotNull(results);
		assertEquals(1, results.length);
		assertEquals(o2, results[0]);
	}
	
	public void testConfigurationElementTracker4() throws Exception {
		tracker.registerObject(e1, o1, IExtensionTracker.REF_STRONG);
		ReferenceQueue queue = new ReferenceQueue();
		WeakReference ref = new WeakReference(o1, queue);
		o1 = null;
		try {
			LeakTests.checkRef(queue, ref);
			fail("Shouldn't have enqueued the ref");
		}
		catch (Throwable e) {
			//wont be enqueued
		}
		Object [] results = tracker.getObjects(e1);
		assertNotNull(results);
		assertEquals(1, results.length);
		assertEquals(ref.get(), results[0]);
	}
}
