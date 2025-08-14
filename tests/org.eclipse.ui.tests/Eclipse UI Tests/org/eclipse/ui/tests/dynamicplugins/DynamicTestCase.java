/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
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

import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.eclipse.ui.tests.leaks.LeakTests;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

/**
 * Baseclass for all dynamic tests.
 *
 * @since 3.1
 */
public abstract class DynamicTestCase extends UITestCase implements
		IRegistryChangeListener {

	private volatile boolean addedRecieved;

	private Bundle newBundle;

	private volatile boolean removedRecieved;

	private WeakReference<IExtensionDelta> addedDelta;

	private WeakReference<IExtensionDelta> removedDelta;

	private ReferenceQueue<IExtensionDelta> queue;

	public DynamicTestCase(String testName) {
		super(testName);
	}

	@Override
	protected void doTearDown() throws Exception {
		super.doTearDown();
		try {
			removeBundle();
		}
		finally {
			Platform.getExtensionRegistry().removeRegistryChangeListener(this);
			queue = null;
		}
	}

	protected static final IWorkbench getWorkbench() {
		return PlatformUI.getWorkbench();
	}

	/**
	 * Get the bundle for this test.
	 *
	 * @return the bundle for this test
	 */
	protected final Bundle getBundle() {
		if (newBundle == null) {
			Platform.getExtensionRegistry().addRegistryChangeListener(this);
			reset();
			queue = new ReferenceQueue<>();
			// Just try to find the new perspective. Don't actually try to
			// do anything with it as the class it refers to does not exist.
			try {
				newBundle = DynamicUtils.installPlugin(getInstallLocation());
			} catch (Exception e) {
				fail(e.getMessage());
			}

			long startTime = System.currentTimeMillis();
			long potentialEndTime = startTime + 5000;
			boolean timeToFail = false;
			while (!hasAddedEventPropagated() && !timeToFail) {
				processEvents();
				timeToFail = System.currentTimeMillis() > potentialEndTime;
				Thread.yield();
			}
			assertTrue("Expected ADDED event did not arrive in time", hasAddedEventPropagated());
			try {
				LeakTests.checkRef(queue, addedDelta);
			} catch (IllegalArgumentException | InterruptedException e1) {
				e1.printStackTrace();
			}
			processEvents();
			Platform.getExtensionRegistry().removeRegistryChangeListener(this);
		}
		return newBundle;
	}

	/**
	 * Return the namespace of the plugin that defines the extension point being
	 * tested. Default is "org.eclipse.ui".
	 *
	 * @return the namespace of the declaring plugin
	 */
	protected String getDeclaringNamespace() {
		return WorkbenchPlugin.PI_WORKBENCH;
	}

	/**
	 * Return the id of the extension to be tested.
	 *
	 * @return the id of the extension to be tested
	 */
	protected abstract String getExtensionId();

	/**
	 * Return the name of the extension point that is being tested.
	 *
	 * @return the extension point being tested
	 */
	protected abstract String getExtensionPoint();

	/**
	 * Return the install location of the bundle to test.
	 *
	 * @return the install location of the bundle to test
	 */
	protected abstract String getInstallLocation();

	/**
	 * Return a <code>Class</code> that we know to be in teh bundle to test.
	 *
	 * @return a <code>Class</code> that we know to be in teh bundle to test.  May be <code>null</code>.
	 * @since 3.1
	 */
	protected String getMarkerClass() {
		return null;
	}

	/**
	 * Tests to ensure that the marker class is released when the bundle is unloaded.
	 * If <code>getMarkerClass()</code> returns <code>null</code> then this method
	 * will always succeed.
	 *
	 * @since 3.1
	 */
	@Test
	public void testClass() throws Exception {
		String className = getMarkerClass();
		if (className == null) {
			return;
		}

		Bundle bundle = getBundle();

		Class<?> clazz = bundle.loadClass(className);
		assertNotNull(clazz);
		ReferenceQueue<ClassLoader> myQueue = new ReferenceQueue<>();
		WeakReference<ClassLoader> ref = new WeakReference<>(clazz.getClassLoader(), myQueue);
		clazz = null; //null our refs
		bundle = null;
		removeBundle();
		LeakTests.checkRef(myQueue, ref);
	}

	/**
	 * Return whether the bundle ADDED event has been recieved.
	 *
	 * @return whether the bundle ADDED event has been recieved
	 */
	protected final boolean hasAddedEventPropagated() {
		return addedRecieved;
	}

	/**
	 * Return whether the bundle REMOVED event has been recieved.
	 *
	 * @return whether the bundle REMOVED event has been recieved
	 */
	protected final boolean hasRemovedEventPropagated() {
		return removedRecieved;
	}

	/**
	 * This method will ensure recording of addition and removal of extensions
	 * described by a combination of
	 * {@link DynamicTestCase#getDeclaringNamespace()},
	 * {@link DynamicTestCase#getExtensionPoint()}, and
	 * {@link DynamicTestCase#getExtensionId()}.
	 *
	 * Custom implementationss should ensure that addition and removal of the
	 * target extension are recorded.
	 *
	 * @see DynamicTestCase#setAddedEventPropagated(boolean)
	 * @see DynamicTestCase#setRemovedEventPropagated(boolean)
	 */
	@Override
	public void registryChanged(IRegistryChangeEvent event) {
		IExtensionDelta delta = event.getExtensionDelta(
				getDeclaringNamespace(), getExtensionPoint(), getExtensionId());
		if (delta != null) {
			if (delta.getKind() == IExtensionDelta.ADDED) {
				addedDelta = new WeakReference<>(delta, queue);
				setAddedEventPropagated(true);
			}
			else if (delta.getKind() == IExtensionDelta.REMOVED) {
				removedDelta = new WeakReference<>(delta, queue);
				setRemovedEventPropagated(true);
			}
		}
	}

	/**
	 * Unload the bundle, if present.
	 */
	protected final void removeBundle() {
		if (newBundle != null) {
			Platform.getExtensionRegistry().addRegistryChangeListener(this);
			queue = new ReferenceQueue<>();
			try {
				DynamicUtils.uninstallPlugin(newBundle);
				long startTime = System.currentTimeMillis();
				long potentialEndTime = startTime + 5000;
				boolean timeToFail = false;
				while (!hasRemovedEventPropagated() && !timeToFail) {
					processEvents();
					timeToFail = System.currentTimeMillis() > potentialEndTime;
					Thread.yield();
				}
				assertTrue("Expected REMOVED event did not arrive in time", hasRemovedEventPropagated());
				try {
					LeakTests.checkRef(queue, removedDelta);
				} catch (IllegalArgumentException | InterruptedException e1) {
					e1.printStackTrace();
				}
			} catch (BundleException e) {
				fail(e.getMessage());
			} finally {
				newBundle = null;
			}
			processEvents();
			Platform.getExtensionRegistry().removeRegistryChangeListener(this);
		}
	}

	/**
	 * Reset the added/removed flags.
	 */
	private void reset() {
		addedDelta = null;
		removedDelta = null;
		setAddedEventPropagated(false);
		setRemovedEventPropagated(false);
	}

	/**
	 * Set whether the bundle ADDED event has been recieved.
	 *
	 * @param added
	 *            whether the bundle ADDED event has been recieved
	 */
	protected final void setAddedEventPropagated(boolean added) {
		this.addedRecieved = added;
	}

	/**
	 * Set whether the bundle REMOVED event has been recieved.
	 *
	 * @param removed
	 *            whether the bundle REMOVED event has been recieved
	 */
	protected final void setRemovedEventPropagated(boolean removed) {
		this.removedRecieved = removed;
	}
}
