/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
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

package org.eclipse.ui.tests.dynamicplugins;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

/**
 * @since 3.5
 */
public class DynamicInvalidContributionTest extends DynamicTestCase {

	public DynamicInvalidContributionTest(String testName) {
		super(testName);
	}

	public void testInvalidMenuContribution() throws Exception {
		// open a window
		IWorkbenchWindow window = openTestWindow();
		// start up our bundle
		getBundle();
		// open another window, now that our invalid contribution is there, it
		// should be parsed and loaded, this ensures the workbench window can
		// still go up even if someone is contributing an invalid contribution
		fWorkbench.openWorkbenchWindow(window.getActivePage().getPerspective().getId(), null);
	}

	@Override
	protected String getExtensionId() {
		return "menu.invalid.menu.contribution";
	}

	@Override
	protected String getExtensionPoint() {
		return IWorkbenchRegistryConstants.PL_MENUS;
	}

	@Override
	protected String getInstallLocation() {
		return "data/org.eclipse.newInvalidMenuContribution1";
	}

}
