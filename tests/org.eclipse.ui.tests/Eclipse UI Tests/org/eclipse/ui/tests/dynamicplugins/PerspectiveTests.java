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

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchPage;

/**
 * Tests to check the addition of a new perspective once the perspective
 * registry is loaded.
 */
public class PerspectiveTests extends DynamicTestCase {

	private static final String PERSPECTIVE_ID = "org.eclipse.newPerspective1.newPerspective1";

	public PerspectiveTests(String testName) {
		super(testName);
	}

	/**
	 * Tests to ensure that the descriptor is added and removed with bundle
	 * loading/unloading.
	 */
	public void testFindPerspectiveInRegistry() {
		IPerspectiveRegistry reg = PlatformUI.getWorkbench()
				.getPerspectiveRegistry();

		assertNull(reg.findPerspectiveWithId(PERSPECTIVE_ID));

		getBundle(); // ensure the bundle is loaded
		assertNotNull(reg.findPerspectiveWithId(PERSPECTIVE_ID));

		removeBundle(); // unload the bundle

		assertNull(reg.findPerspectiveWithId(PERSPECTIVE_ID));
	}

	/**
	 * Tests that the perspective is closed if it is the currently active
	 * perspective.
	 */
	public void testPerspectiveClose1() {
		IPerspectiveRegistry reg = PlatformUI.getWorkbench()
				.getPerspectiveRegistry();

		getBundle();
		IPerspectiveDescriptor desc = reg.findPerspectiveWithId(PERSPECTIVE_ID);
		assertNotNull(desc);

		IWorkbenchWindow window = openTestWindow(IDE.RESOURCE_PERSPECTIVE_ID);
		window.getActivePage().setPerspective(desc);

		removeBundle();
		assertNull(((WorkbenchPage) window.getActivePage())
				.findPerspective(desc));
		assertFalse(window.getActivePage().getPerspective().getId().equals(
				desc.getId()));
		assertEquals(IDE.RESOURCE_PERSPECTIVE_ID, window.getActivePage()
				.getPerspective().getId());
	}

	/**
	 * Tests that the perspective is closed if it is not the currently active
	 * perspective.
	 */
	public void testPerspectiveClose2() {
		IPerspectiveRegistry reg = PlatformUI.getWorkbench()
				.getPerspectiveRegistry();

		getBundle();
		IPerspectiveDescriptor desc = reg.findPerspectiveWithId(PERSPECTIVE_ID);
		assertNotNull(desc);

		IWorkbenchWindow window = openTestWindow(PERSPECTIVE_ID);
		window.getActivePage().setPerspective(
				reg.findPerspectiveWithId(IDE.RESOURCE_PERSPECTIVE_ID));

		removeBundle();
		assertNull(((WorkbenchPage) window.getActivePage())
				.findPerspective(desc));

		assertFalse(window.getActivePage().getPerspective().getId().equals(
				PERSPECTIVE_ID));
		assertEquals(IDE.RESOURCE_PERSPECTIVE_ID, window.getActivePage()
				.getPerspective().getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getInstallLocation()
	 */
	protected String getInstallLocation() {
		return "data/org.eclipse.newPerspective1";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getExtensionPoint()
	 */
	protected String getExtensionPoint() {
		return IWorkbenchConstants.PL_PERSPECTIVES;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getExtensionId()
	 */
	protected String getExtensionId() {
		return "newPerspective1.testDynamicPerspectiveAddition";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getMarkerClass()
	 */
	protected String getMarkerClass() {
		return "org.eclipse.ui.dynamic.DynamicPerspective";
	}
}