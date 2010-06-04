/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.application;

import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.internal.workbench.E4XMIResource;
import org.eclipse.e4.ui.internal.workbench.ResourceHandler;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.osgi.service.datalocation.Location;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;

/**
 *
 */
@SuppressWarnings("restriction")
public class ResourceHandlerTest extends HeadlessStartupTest {
	private ServiceTracker locationTracker;

	public Location getInstanceLocation() {
		if (locationTracker == null) {
			BundleContext context = FrameworkUtil.getBundle(
					ResourceHandlerTest.class).getBundleContext();
			Filter filter = null;
			try {
				filter = context.createFilter(Location.INSTANCE_FILTER);
			} catch (InvalidSyntaxException e) {
				// ignore this. It should never happen as we have tested the
				// above format.
			}
			locationTracker = new ServiceTracker(context, filter, null);
			locationTracker.open();
		}
		return (Location) locationTracker.getService();
	}

	public void testLoadMostRecent() {
		URI uri = URI.createPlatformPluginURI(
				"org.eclipse.e4.ui.tests/xmi/InvalidContainment.e4xmi", true);

		ResourceHandler handler = new ResourceHandler(getInstanceLocation(),
				uri, true, (Logger) applicationContext.get(Logger.class
						.getName()));
		Resource resource = handler.loadMostRecentModel();
		assertNotNull(resource);
		assertEquals(E4XMIResource.class, resource.getClass());
		checkData(resource);
	}

	public void testLoadBaseModel() {
		URI uri = URI.createPlatformPluginURI(
				"org.eclipse.e4.ui.tests/xmi/InvalidContainment.e4xmi", true);

		ResourceHandler handler = new ResourceHandler(getInstanceLocation(),
				uri, true, (Logger) applicationContext.get(Logger.class
						.getName()));
		Resource resource = handler.loadBaseModel();
		assertNotNull(resource);
		assertEquals(E4XMIResource.class, resource.getClass());
		checkData(resource);
	}

	private void checkData(Resource resource) {
		assertNotNull(resource);
		assertEquals(1, resource.getContents().size());
		MApplication app = (MApplication) resource.getContents().get(0);
		assertEquals(1, app.getChildren().size());
		MWindow w = app.getChildren().get(0);
		assertEquals("window1", w.getElementId());
		assertEquals(2, w.getChildren().size());
		MPartStack stack = (MPartStack) w.getChildren().get(0);
		assertEquals("window1.partstack1", stack.getElementId());
		assertEquals(2, stack.getChildren().size());
		assertEquals("window1.partstack1.part1", stack.getChildren().get(0)
				.getElementId());
		assertEquals("window1.partstack1.inputpart1", stack.getChildren()
				.get(1).getElementId());

		stack = (MPartStack) w.getChildren().get(1);
		assertEquals("window1.partstack2", stack.getElementId());
		assertEquals(1, stack.getChildren().size());
		assertEquals("window1.partstack2.part1", stack.getChildren().get(0)
				.getElementId());
	}
}
