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
package org.eclipse.ui.tests.dynamicplugins;

import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.tests.util.UITestCase;
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

	/**
	 * @param testName
	 */
	public DynamicTestCase(String testName) {
		super(testName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.util.UITestCase#doSetUp()
	 */
	protected void doSetUp() throws Exception {
		super.doSetUp();
		Platform.getExtensionRegistry().addRegistryChangeListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.util.UITestCase#doTearDown()
	 */
	protected void doTearDown() throws Exception {
		super.doTearDown();
		try {
			removeBundle();
		}
		finally {
			Platform.getExtensionRegistry().removeRegistryChangeListener(this);
		}
	}

	/**
	 * Get the bundle for this test.
	 * 
	 * @return the bundle for this test
	 */
	protected final Bundle getBundle() {
		if (newBundle == null) {
			reset();
			// Just try to find the new perspective. Don't actually try to
			// do anything with it as the class it refers to does not exist.
			try {
				newBundle = DynamicUtils.installPlugin(getInstallLocation());
			} catch (Exception e) {
				fail(e.getMessage());
			}

			long startTime = System.currentTimeMillis();
			long potentialEndTime = startTime + 1000;
			boolean timeToFail = false;
			while (!hasAddedEventPropagated() && !timeToFail) {
				processEvents();
				timeToFail = System.currentTimeMillis() > potentialEndTime;
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
			assertFalse("Test failed due to timeout on addition", timeToFail);
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
	public void registryChanged(IRegistryChangeEvent event) {
		IExtensionDelta delta = event.getExtensionDelta(
				getDeclaringNamespace(), getExtensionPoint(), getExtensionId());
		if (delta != null) {
			if (delta.getKind() == IExtensionDelta.ADDED)
				setAddedEventPropagated(true);
			else if (delta.getKind() == IExtensionDelta.REMOVED)
				setRemovedEventPropagated(true);
		}
	}

	/**
	 * Unload the bundle, if present.
	 */
	protected final void removeBundle() {
		if (newBundle != null) {
			try {
				DynamicUtils.uninstallPlugin(newBundle);
				long startTime = System.currentTimeMillis();
				long potentialEndTime = startTime + 1000;
				boolean timeToFail = false;
				while (!hasRemovedEventPropagated() && !timeToFail) {
					processEvents();
					timeToFail = System.currentTimeMillis() > potentialEndTime;
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
				}
				assertFalse("Test failed due to timeout on removal", timeToFail);

			} catch (BundleException e) {
				fail(e.getMessage());
			} finally {
				newBundle = null;
			}
		}
	}

	/**
	 * Reset the added/removed flags.
	 */
	private void reset() {
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
	 * @param added
	 *            whether the bundle REMOVED event has been recieved
	 */
	protected final void setRemovedEventPropagated(boolean removed) {
		this.removedRecieved = removed;
	}
}
