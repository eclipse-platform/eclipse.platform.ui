/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dynamicplugins;

import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

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
		MPerspective persp = findPerspective(window, desc.getId());
		assertNull(persp);
		

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
		MPerspective persp = findPerspective(window, PERSPECTIVE_ID);
		assertNull(persp);

		assertFalse(window.getActivePage().getPerspective().getId()
				.equals(PERSPECTIVE_ID));
		assertEquals(IDE.RESOURCE_PERSPECTIVE_ID, window.getActivePage()
				.getPerspective().getId());
	}
	
	private MPerspective findPerspective(IWorkbenchWindow window, String id) {
		EModelService modelService = window
				.getService(EModelService.class);
		return (MPerspective) modelService.find(id,
				((WorkbenchWindow) window).getModel());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getInstallLocation()
	 */
	@Override
	protected String getInstallLocation() {
		return "data/org.eclipse.newPerspective1";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getExtensionPoint()
	 */
	@Override
	protected String getExtensionPoint() {
		return IWorkbenchRegistryConstants.PL_PERSPECTIVES;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getExtensionId()
	 */
	@Override
	protected String getExtensionId() {
		return "newPerspective1.testDynamicPerspectiveAddition";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getMarkerClass()
	 */
	@Override
	protected String getMarkerClass() {
		return "org.eclipse.ui.dynamic.DynamicPerspective";
	}
}
