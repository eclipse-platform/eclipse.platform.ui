/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.e4.ui.tests.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.eclipse.e4.ui.internal.workbench.E4XMIResource;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.junit.jupiter.api.Test;

public class ModelRobustnessTest {

	@Test
	public void testLoadingInvalidContainments() {
		// E4XMIResourceFactory factory = new E4XMIResourceFactory();
		URI uri = URI.createPlatformPluginURI(
				"org.eclipse.e4.ui.tests/xmi/InvalidContainment.e4xmi", true);
		ResourceSet set = new ResourceSetImpl();
		Resource resource = null;

		assertThrows(Exception.class, () -> set.getResource(uri, true));
		resource = set.getResource(uri, false);

		assertNotNull(resource);
		assertEquals(E4XMIResource.class, resource.getClass());
		assertEquals(1, resource.getContents().size());
		MApplication app = (MApplication) resource.getContents().get(0);
		assertEquals(1, app.getChildren().size());
		MWindow w = app.getChildren().get(0);
		assertEquals("window1", w.getElementId());
		assertEquals(2, w.getChildren().size());
		MPartStack stack = (MPartStack) w.getChildren().get(0);
		assertEquals("window1.partstack1", stack.getElementId());
		assertEquals(1, stack.getChildren().size());
		assertEquals("window1.partstack1.part1", stack.getChildren().get(0)
				.getElementId());

		stack = (MPartStack) w.getChildren().get(1);
		assertEquals("window1.partstack2", stack.getElementId());
		assertEquals(1, stack.getChildren().size());
		assertEquals("window1.partstack2.part1", stack.getChildren().get(0)
				.getElementId());
	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testAddingInvalidElements() {
		MApplication app = MApplicationFactory.INSTANCE.createApplication();
		List l = app.getChildren();
		l.add(MBasicFactory.INSTANCE.createWindow());
		// EList.add says IllegalArgumentException is the expected exception, although
		// testing indicates ArrayStoreException or ClassCastException may be thrown.
		// See bug 407539
		assertThrows(RuntimeException.class, () -> l.add(MBasicFactory.INSTANCE.createPart()));

		l.add(MBasicFactory.INSTANCE.createWindow());
		assertEquals(2, l.size());
	}
}
