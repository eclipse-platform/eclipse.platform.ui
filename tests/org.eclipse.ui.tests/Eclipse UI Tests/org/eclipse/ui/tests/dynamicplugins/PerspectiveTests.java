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

import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchConstants;

/**
 * Tests to check the addition of a new perspective once the perspective
 * registry is loaded.
 */
public class PerspectiveTests extends DynamicTestCase {

	public PerspectiveTests(String testName) {
		super(testName);
	}

	/**
	 * Tests to ensure that the descriptor is added and removed with bundle
	 * loading/unloading.
	 */
	public void testFindPerspectiveInRegistry() {
		getBundle(); //ensure the bundle is loaded
		IPerspectiveRegistry reg = PlatformUI.getWorkbench()
				.getPerspectiveRegistry();

		assertNotNull(reg
				.findPerspectiveWithId("org.eclipse.newPerspective1.newPerspective1"));

		removeBundle(); //unload the bundle

		assertNull(reg
				.findPerspectiveWithId("org.eclipse.newPerspective1.newPerspective1"));
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

}