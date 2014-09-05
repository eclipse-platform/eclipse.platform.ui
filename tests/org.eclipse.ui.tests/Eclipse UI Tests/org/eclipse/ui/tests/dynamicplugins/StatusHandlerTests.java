/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
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
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.statushandlers.StatusHandlerDescriptor;
import org.eclipse.ui.internal.statushandlers.StatusHandlerRegistry;
import org.eclipse.ui.statushandlers.AbstractStatusHandler;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.tests.leaks.LeakTests;

/**
 * Tests to ensure the addition and removal of new status handlers with dynamic
 * plug-ins.
 */
public class StatusHandlerTests extends DynamicTestCase {

	private static final String STATUS_HANDLER_ID1 = "org.eclipse.newStatusHandler1.newStatusHandler1";

	private static final String STATUS_HANDLER_ID2 = "org.eclipse.newStatusHandler1.newStatusHandler2";

	private static final String PLUGIN_PREFIX = "";

	private static final String PLUGIN_PREFIX2 = "plugin";

	/**
	 * @param testName
	 */
	public StatusHandlerTests(String testName) {
		super(testName);
	}

	/**
	 * Tests to ensure that the status handlers are removed when the plugin is
	 * unloaded.
	 */
	public void testStatusHandlerRemoval() throws CoreException {
		getBundle();

		StatusHandlerDescriptor statusHandlerDescriptor1 = StatusHandlerRegistry
				.getDefault().getHandlerDescriptor(STATUS_HANDLER_ID1);
		assertNotNull(statusHandlerDescriptor1);
		AbstractStatusHandler statusHandler1 = statusHandlerDescriptor1
				.getStatusHandler();
		assertNotNull(statusHandler1);

		statusHandler1.handle(new StatusAdapter(new Status(IStatus.ERROR,
				PLUGIN_PREFIX2, "")), StatusManager.NONE);

		ReferenceQueue queue = new ReferenceQueue();
		ReferenceQueue queue2 = new ReferenceQueue();

		WeakReference ref = new WeakReference(statusHandlerDescriptor1, queue);
		WeakReference ref2 = new WeakReference(statusHandler1, queue2);

		statusHandlerDescriptor1 = null; // null the reference
		statusHandler1 = null; // null the reference

		removeBundle();

		try {
			LeakTests.checkRef(queue, ref);
			LeakTests.checkRef(queue2, ref2);
		} catch (Exception e) {
			fail(e.getMessage());
		}

		assertNull(StatusHandlerRegistry.getDefault().getHandlerDescriptor(
				STATUS_HANDLER_ID1));
	}

	/**
	 * Tests to ensure that the status handlers are removed when the plugin is
	 * unloaded.
	 */
	public void testStatusHandlerRemoval2() throws CoreException {
		getBundle();

		ReferenceQueue queue = new ReferenceQueue();
		ReferenceQueue queue2 = new ReferenceQueue();

		List statusHandlerDescriptors = StatusHandlerRegistry.getDefault()
				.getHandlerDescriptors(PLUGIN_PREFIX);

		assertNotNull(statusHandlerDescriptors);

		StatusHandlerDescriptor statusHandlerDescriptor1 = null;
		for (Iterator it = statusHandlerDescriptors.iterator(); it.hasNext();) {
			statusHandlerDescriptor1 = (StatusHandlerDescriptor) it.next();
			if (statusHandlerDescriptor1.getId().equals(STATUS_HANDLER_ID2)) {
				break;
			}
			statusHandlerDescriptor1 = null;
		}

		assertNotNull(statusHandlerDescriptor1);
		AbstractStatusHandler statusHandler1 = statusHandlerDescriptor1
				.getStatusHandler();
		assertNotNull(statusHandler1);

		statusHandler1.handle(new StatusAdapter(new Status(IStatus.ERROR,
				PLUGIN_PREFIX2, "")), StatusManager.NONE);

		WeakReference ref = new WeakReference(statusHandlerDescriptor1, queue);
		WeakReference ref2 = new WeakReference(statusHandler1, queue2);

		statusHandlerDescriptors = null;
		statusHandlerDescriptor1 = null; // null the reference
		statusHandler1 = null;

		removeBundle();

		try {
			LeakTests.checkRef(queue, ref);
			LeakTests.checkRef(queue2, ref2);
		} catch (Exception e) {
			fail(e.getMessage());
		}

		assertNull(StatusHandlerRegistry.getDefault().getHandlerDescriptor(
				STATUS_HANDLER_ID1));
	}

	/**
	 * Tests to ensure that the status handlers are removed when the plugin is
	 * unloaded. Checks if the default product handlers are handled correctly.
	 */
	public void testProductBindingRemoval() throws CoreException {
		getBundle();

		ReferenceQueue queue = new ReferenceQueue();
		ReferenceQueue queue2 = new ReferenceQueue();

		StatusHandlerDescriptor productStatusHandlerDescriptor = StatusHandlerRegistry
				.getDefault().getDefaultHandlerDescriptor();
		assertNotNull(productStatusHandlerDescriptor);
		AbstractStatusHandler productStatusHandler = productStatusHandlerDescriptor
				.getStatusHandler();
		assertNotNull(productStatusHandler);

		productStatusHandler.handle(new StatusAdapter(new Status(IStatus.ERROR,
				PLUGIN_PREFIX2, "")), StatusManager.NONE);

		WeakReference ref = new WeakReference(productStatusHandlerDescriptor,
				queue);
		WeakReference ref2 = new WeakReference(productStatusHandler, queue2);

		productStatusHandlerDescriptor = null; // null the reference
		productStatusHandler = null;

		removeBundle();

		try {
			LeakTests.checkRef(queue, ref);
			LeakTests.checkRef(queue2, ref2);
		} catch (Exception e) {
			fail(e.getMessage());
		}

		assertNull(StatusHandlerRegistry.getDefault().getHandlerDescriptor(
				STATUS_HANDLER_ID1));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getExtensionId()
	 */
	@Override
	protected String getExtensionId() {
		return "newStatusHandler1.testDynamicStatusHandlerAddition";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getExtensionPoint()
	 */
	@Override
	protected String getExtensionPoint() {
		return "statusHandlers";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getInstallLocation()
	 */
	@Override
	protected String getInstallLocation() {
		return "data/org.eclipse.newStatusHandler1";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getMarkerClass()
	 */
	@Override
	protected String getMarkerClass() {
		return "org.eclipse.ui.dynamic.DynamicStatusHandler";
	}
}
