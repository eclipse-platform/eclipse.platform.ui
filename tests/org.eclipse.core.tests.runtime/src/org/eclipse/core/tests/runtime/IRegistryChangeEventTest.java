/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime;

import junit.framework.*;
import junit.framework.Test;
import junit.framework.TestCase;
import org.eclipse.core.internal.registry.*;
import org.eclipse.core.runtime.*;

/**
 * Tests the notification mechanism for registry changes.    
 */
public class IRegistryChangeEventTest extends TestCase {
	public IRegistryChangeEventTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	/**
	 * Allows test cases to wait for event notification so they can make assertions on the event.  
	 */
	class RegistryChangeListener implements IRegistryChangeListener {
		private IRegistryChangeEvent event;

		public synchronized void registryChanged(IRegistryChangeEvent newEvent) {
			if (this.event != null)
				return;
			this.event = newEvent;
			notify();
		}

		public synchronized IRegistryChangeEvent waitSignal(long timeout) {
			IRegistryChangeEvent result = event;
			if (event != null) {
				event = null;
				return result;
			}
			try {
				wait(timeout);
			} catch (InterruptedException e) {
				// who cares?
			}
			result = event;
			event = null;
			return result;
		}
	}

	public void testAddSinglePluginWithExtensionAndExtensionPoint() {
		ExtensionRegistry registry = new ExtensionRegistry(new ExtensionLinker());
		final BundleModel pluginA = new BundleModel();
		pluginA.setUniqueIdentifier("a");
		final ExtensionPoint xp1 = new ExtensionPoint();
		xp1.setSimpleIdentifier("xp1");
		final Extension ext1 = new Extension();
		ext1.setExtensionPointIdentifier("a.xp1");
		ext1.setSimpleIdentifier("ext1");
		pluginA.setExtensionPoints(new ExtensionPoint[] {xp1});
		xp1.setParent(pluginA);
		pluginA.setExtensions(new Extension[] {ext1});
		ext1.setParent(pluginA);
		RegistryChangeListener listener = new RegistryChangeListener();
		try {
			registry.addRegistryChangeListener(listener);
			registry.add(pluginA);
			IRegistryChangeEvent event = listener.waitSignal(10000);
			assertNotNull("1.0", event);
			assertEquals("1.1", 1, event.getExtensionDeltas().length);
			assertEquals("1.2", 1, event.getExtensionDeltas(pluginA.getUniqueIdentifier()).length);
			IExtensionDelta[] extensionDeltas = event.getExtensionDeltas(pluginA.getUniqueIdentifier());
			assertEquals("1.3", 1, extensionDeltas.length);
			assertTrue("1.4", extensionDeltas[0].getExtension() == ext1);
			assertTrue("1.5", extensionDeltas[0].getExtensionPoint() == xp1);
			assertEquals("1.6", IExtensionDelta.ADDED, extensionDeltas[0].getKind());
		} finally {
			registry.removeRegistryChangeListener(listener);
		}
	}

	public void testAddTwoPluginsExtensionPointProviderFirst() {
		ExtensionRegistry registry = new ExtensionRegistry(new ExtensionLinker());
		final BundleModel pluginA = new BundleModel();
		pluginA.setUniqueIdentifier("a");
		final ExtensionPoint xp1 = new ExtensionPoint();
		xp1.setSimpleIdentifier("xp1");
		pluginA.setExtensionPoints(new ExtensionPoint[] {xp1});
		xp1.setParent(pluginA);
		final BundleModel pluginB = new BundleModel();
		pluginB.setUniqueIdentifier("b");
		final Extension ext1 = new Extension();
		ext1.setExtensionPointIdentifier("a.xp1");
		ext1.setSimpleIdentifier("ext1");
		pluginB.setExtensions(new Extension[] {ext1});
		ext1.setParent(pluginB);
		RegistryChangeListener listener = new RegistryChangeListener();
		try {
			registry.addRegistryChangeListener(listener);
			registry.add(pluginA);
			assertNull("0.9", listener.waitSignal(500));
			registry.add(pluginB);
			IRegistryChangeEvent event = listener.waitSignal(10000);
			assertNotNull("1.0", event);
			assertEquals("1.1", 1, event.getExtensionDeltas().length);
			assertEquals("1.2", 1, event.getExtensionDeltas(pluginA.getUniqueIdentifier()).length);
			assertEquals("1.2a", 0, event.getExtensionDeltas(pluginB.getUniqueIdentifier()).length);
			IExtensionDelta[] extensionDeltas = event.getExtensionDeltas(pluginA.getUniqueIdentifier());
			assertEquals("1.3", 1, extensionDeltas.length);
			assertTrue("1.4", extensionDeltas[0].getExtension() == ext1);
			assertTrue("1.5", extensionDeltas[0].getExtensionPoint() == xp1);
			assertEquals("1.6", IExtensionDelta.ADDED, extensionDeltas[0].getKind());
		} finally {
			registry.removeRegistryChangeListener(listener);
		}
	}

	public void testAddTwoPluginsExtensionProviderFirst() {
		ExtensionRegistry registry = new ExtensionRegistry(new ExtensionLinker());
		final BundleModel pluginA = new BundleModel();
		pluginA.setUniqueIdentifier("a");
		final ExtensionPoint xp1 = new ExtensionPoint();
		xp1.setSimpleIdentifier("xp1");
		pluginA.setExtensionPoints(new ExtensionPoint[] {xp1});
		xp1.setParent(pluginA);
		final BundleModel pluginB = new BundleModel();
		pluginB.setUniqueIdentifier("b");
		final Extension ext1 = new Extension();
		ext1.setExtensionPointIdentifier("a.xp1");
		ext1.setSimpleIdentifier("ext1");
		pluginB.setExtensions(new Extension[] {ext1});
		ext1.setParent(pluginB);
		RegistryChangeListener listener = new RegistryChangeListener();
		try {
			registry.addRegistryChangeListener(listener);
			registry.add(pluginB);
			assertNull("0.9", listener.waitSignal(500));
			registry.add(pluginA);
			IRegistryChangeEvent event = listener.waitSignal(10000);
			assertNotNull("1.0", event);
			assertEquals("1.1", 1, event.getExtensionDeltas().length);
			assertEquals("1.2", 1, event.getExtensionDeltas(pluginA.getUniqueIdentifier()).length);
			assertEquals("1.2a", 0, event.getExtensionDeltas(pluginB.getUniqueIdentifier()).length);
			IExtensionDelta[] extensionDeltas = event.getExtensionDeltas(pluginA.getUniqueIdentifier());
			assertEquals("1.3", 1, extensionDeltas.length);
			assertTrue("1.4", extensionDeltas[0].getExtension() == ext1);
			assertTrue("1.5", extensionDeltas[0].getExtensionPoint() == xp1);
			assertEquals("1.6", IExtensionDelta.ADDED, extensionDeltas[0].getKind());
		} finally {
			registry.removeRegistryChangeListener(listener);
		}
	}

	public void testRemoveSinglePluginWithExtensionAndExtensionPoint() {
		ExtensionRegistry registry = new ExtensionRegistry(new ExtensionLinker());
		final BundleModel pluginA = new BundleModel();
		pluginA.setUniqueIdentifier("a");
		final ExtensionPoint xp1 = new ExtensionPoint();
		xp1.setSimpleIdentifier("xp1");
		final Extension ext1 = new Extension();
		ext1.setExtensionPointIdentifier("a.xp1");
		ext1.setSimpleIdentifier("ext1");
		pluginA.setExtensionPoints(new ExtensionPoint[] {xp1});
		xp1.setParent(pluginA);
		pluginA.setExtensions(new Extension[] {ext1});
		ext1.setParent(pluginA);
		RegistryChangeListener listener = new RegistryChangeListener();
		registry.add(pluginA);
		try {
			registry.addRegistryChangeListener(listener);
			registry.remove(pluginA.getUniqueIdentifier(), pluginA.getId());
			IRegistryChangeEvent event = listener.waitSignal(10000);
			assertNotNull("1.0", event);
			assertEquals("1.1", 1, event.getExtensionDeltas().length);
			assertEquals("1.2", 1, event.getExtensionDeltas(pluginA.getUniqueIdentifier()).length);
			IExtensionDelta[] extensionDeltas = event.getExtensionDeltas(pluginA.getUniqueIdentifier());
			assertEquals("1.3", 1, extensionDeltas.length);
			assertTrue("1.4", extensionDeltas[0].getExtension() == ext1);
			assertTrue("1.5", extensionDeltas[0].getExtensionPoint() == xp1);
			assertEquals("1.6", IExtensionDelta.REMOVED, extensionDeltas[0].getKind());
		} finally {
			registry.removeRegistryChangeListener(listener);
		}
	}

	public void testRemoveTwoPluginsExtensionProviderFirst() {
		ExtensionRegistry registry = new ExtensionRegistry(new ExtensionLinker());
		final BundleModel pluginA = new BundleModel();
		pluginA.setUniqueIdentifier("a");
		final ExtensionPoint xp1 = new ExtensionPoint();
		xp1.setSimpleIdentifier("xp1");
		pluginA.setExtensionPoints(new ExtensionPoint[] {xp1});
		xp1.setParent(pluginA);
		final BundleModel pluginB = new BundleModel();
		pluginB.setUniqueIdentifier("b");
		final Extension ext1 = new Extension();
		ext1.setExtensionPointIdentifier("a.xp1");
		ext1.setSimpleIdentifier("ext1");
		pluginB.setExtensions(new Extension[] {ext1});
		ext1.setParent(pluginB);
		RegistryChangeListener listener = new RegistryChangeListener();
		registry.add(pluginA);
		registry.add(pluginB);
		try {
			registry.addRegistryChangeListener(listener);
			registry.remove(pluginB.getUniqueIdentifier(), pluginB.getId());
			IRegistryChangeEvent event = listener.waitSignal(10000);
			assertNotNull("1.0", event);
			assertEquals("1.1", 1, event.getExtensionDeltas().length);
			assertEquals("1.2", 1, event.getExtensionDeltas(pluginA.getUniqueIdentifier()).length);
			assertEquals("1.2a", 0, event.getExtensionDeltas(pluginB.getUniqueIdentifier()).length);
			IExtensionDelta[] extensionDeltas = event.getExtensionDeltas(pluginA.getUniqueIdentifier());
			assertEquals("1.3", 1, extensionDeltas.length);
			assertTrue("1.4", extensionDeltas[0].getExtension() == ext1);
			assertTrue("1.5", extensionDeltas[0].getExtensionPoint() == xp1);
			assertEquals("1.6", IExtensionDelta.REMOVED, extensionDeltas[0].getKind());
			registry.remove(pluginA.getUniqueIdentifier(), pluginA.getId());
			assertNull("1.7", listener.waitSignal(500));
		} finally {
			registry.removeRegistryChangeListener(listener);
		}
	}

	public void testRemoveTwoPluginsExtensionPointProviderFirst() {
		ExtensionRegistry registry = new ExtensionRegistry(new ExtensionLinker());
		final BundleModel pluginA = new BundleModel();
		pluginA.setUniqueIdentifier("a");
		final ExtensionPoint xp1 = new ExtensionPoint();
		xp1.setSimpleIdentifier("xp1");
		pluginA.setExtensionPoints(new ExtensionPoint[] {xp1});
		xp1.setParent(pluginA);
		final BundleModel pluginB = new BundleModel();
		pluginB.setUniqueIdentifier("b");
		final Extension ext1 = new Extension();
		ext1.setExtensionPointIdentifier("a.xp1");
		ext1.setSimpleIdentifier("ext1");
		pluginB.setExtensions(new Extension[] {ext1});
		ext1.setParent(pluginB);
		RegistryChangeListener listener = new RegistryChangeListener();
		registry.add(pluginA);
		registry.add(pluginB);
		try {
			registry.addRegistryChangeListener(listener);
			registry.remove(pluginB.getUniqueIdentifier(), pluginA.getId());
			IRegistryChangeEvent event = listener.waitSignal(10000);
			assertNotNull("1.0", event);
			assertEquals("1.1", 1, event.getExtensionDeltas().length);
			assertEquals("1.2", 1, event.getExtensionDeltas(pluginA.getUniqueIdentifier()).length);
			assertEquals("1.2a", 0, event.getExtensionDeltas(pluginB.getUniqueIdentifier()).length);
			IExtensionDelta[] extensionDeltas = event.getExtensionDeltas(pluginA.getUniqueIdentifier());
			assertEquals("1.3", 1, extensionDeltas.length);
			assertTrue("1.4", extensionDeltas[0].getExtension() == ext1);
			assertTrue("1.5", extensionDeltas[0].getExtensionPoint() == xp1);
			assertEquals("1.6", IExtensionDelta.REMOVED, extensionDeltas[0].getKind());
			registry.remove(pluginA.getUniqueIdentifier(), pluginB.getId());
			assertNull("1.7", listener.waitSignal(500));
		} finally {
			registry.removeRegistryChangeListener(listener);
		}
	}

	public static Test suite() {
		return new TestSuite(IRegistryChangeEventTest.class);
	}
}