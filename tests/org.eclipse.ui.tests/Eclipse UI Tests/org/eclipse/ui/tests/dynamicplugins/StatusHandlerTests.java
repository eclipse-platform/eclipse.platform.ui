/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests to ensure the addition and removal of new status handlers with dynamic
 * plug-ins.
 */
@RunWith(JUnit4.class)
public class StatusHandlerTests extends DynamicTestCase {

	private static final String STATUS_HANDLER_ID1 = "org.eclipse.newStatusHandler1.newStatusHandler1";

	private static final String STATUS_HANDLER_ID2 = "org.eclipse.newStatusHandler1.newStatusHandler2";

	private static final String PLUGIN_PREFIX = "";

	private static final String PLUGIN_PREFIX2 = "plugin";

	public StatusHandlerTests() {
		super(StatusHandlerTests.class.getSimpleName());
	}

	/**
	 * Tests to ensure that the status handlers are removed when the plugin is
	 * unloaded.
	 *
	 * @throws InterruptedException
	 * @throws IllegalArgumentException
	 */
	@Test
	public void testStatusHandlerRemoval() throws CoreException, IllegalArgumentException, InterruptedException {
		getBundle();

		StatusHandlerDescriptor statusHandlerDescriptor1 = StatusHandlerRegistry
				.getDefault().getHandlerDescriptor(STATUS_HANDLER_ID1);
		assertNotNull(statusHandlerDescriptor1);
		AbstractStatusHandler statusHandler1 = statusHandlerDescriptor1
				.getStatusHandler();
		assertNotNull(statusHandler1);

		statusHandler1.handle(new StatusAdapter(new Status(IStatus.ERROR,
				PLUGIN_PREFIX2, "")), StatusManager.NONE);

		ReferenceQueue<StatusHandlerDescriptor> queue = new ReferenceQueue<>();
		ReferenceQueue<AbstractStatusHandler> queue2 = new ReferenceQueue<>();

		WeakReference<StatusHandlerDescriptor> ref = new WeakReference<>(statusHandlerDescriptor1, queue);
		WeakReference<AbstractStatusHandler> ref2 = new WeakReference<>(statusHandler1, queue2);

		statusHandlerDescriptor1 = null; // null the reference
		statusHandler1 = null; // null the reference

		removeBundle();

		LeakTests.checkRef(queue, ref);
		LeakTests.checkRef(queue2, ref2);

		assertNull(StatusHandlerRegistry.getDefault().getHandlerDescriptor(
				STATUS_HANDLER_ID1));
	}

	/**
	 * Tests to ensure that the status handlers are removed when the plugin is
	 * unloaded.
	 *
	 * @throws InterruptedException
	 * @throws IllegalArgumentException
	 */
	@Test
	public void testStatusHandlerRemoval2() throws CoreException, IllegalArgumentException, InterruptedException {
		getBundle();

		ReferenceQueue<StatusHandlerDescriptor> queue = new ReferenceQueue<>();
		ReferenceQueue<AbstractStatusHandler> queue2 = new ReferenceQueue<>();

		List<StatusHandlerDescriptor> statusHandlerDescriptors = StatusHandlerRegistry.getDefault()
				.getHandlerDescriptors(PLUGIN_PREFIX);

		assertNotNull(statusHandlerDescriptors);

		StatusHandlerDescriptor statusHandlerDescriptor1 = null;
		for (Iterator<StatusHandlerDescriptor> it = statusHandlerDescriptors.iterator(); it.hasNext();) {
			statusHandlerDescriptor1 = it.next();
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

		WeakReference<StatusHandlerDescriptor> ref = new WeakReference<>(statusHandlerDescriptor1, queue);
		WeakReference<AbstractStatusHandler> ref2 = new WeakReference<>(statusHandler1, queue2);

		statusHandlerDescriptors = null;
		statusHandlerDescriptor1 = null; // null the reference
		statusHandler1 = null;

		removeBundle();

		LeakTests.checkRef(queue, ref);
		LeakTests.checkRef(queue2, ref2);

		assertNull(StatusHandlerRegistry.getDefault().getHandlerDescriptor(
				STATUS_HANDLER_ID1));
	}

	/**
	 * Tests to ensure that the status handlers are removed when the plugin is
	 * unloaded. Checks if the default product handlers are handled correctly.
	 *
	 * @throws InterruptedException
	 * @throws IllegalArgumentException
	 */
	@Test
	public void testProductBindingRemoval() throws CoreException, IllegalArgumentException, InterruptedException {
		getBundle();

		ReferenceQueue<StatusHandlerDescriptor> queue = new ReferenceQueue<>();
		ReferenceQueue<AbstractStatusHandler> queue2 = new ReferenceQueue<>();

		StatusHandlerDescriptor productStatusHandlerDescriptor = StatusHandlerRegistry
				.getDefault().getDefaultHandlerDescriptor();
		assertNotNull(productStatusHandlerDescriptor);
		AbstractStatusHandler productStatusHandler = productStatusHandlerDescriptor
				.getStatusHandler();
		assertNotNull(productStatusHandler);

		productStatusHandler.handle(new StatusAdapter(new Status(IStatus.ERROR,
				PLUGIN_PREFIX2, "")), StatusManager.NONE);

		WeakReference<StatusHandlerDescriptor> ref = new WeakReference<>(productStatusHandlerDescriptor,
				queue);
		WeakReference<AbstractStatusHandler> ref2 = new WeakReference<>(productStatusHandler, queue2);

		productStatusHandlerDescriptor = null; // null the reference
		productStatusHandler = null;

		removeBundle();

		LeakTests.checkRef(queue, ref);
		LeakTests.checkRef(queue2, ref2);

		assertNull(StatusHandlerRegistry.getDefault().getHandlerDescriptor(
				STATUS_HANDLER_ID1));
	}

	@Override
	protected String getExtensionId() {
		return "newStatusHandler1.testDynamicStatusHandlerAddition";
	}

	@Override
	protected String getExtensionPoint() {
		return "statusHandlers";
	}

	@Override
	protected String getInstallLocation() {
		return "data/org.eclipse.newStatusHandler1";
	}

	@Override
	protected String getMarkerClass() {
		return "org.eclipse.ui.dynamic.DynamicStatusHandler";
	}
}
