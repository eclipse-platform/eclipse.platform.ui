/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
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
 * @since 3.8
 */
public class DynamicInvalidControlContributionTest extends DynamicTestCase {

	public DynamicInvalidControlContributionTest(String testName) {
		super(testName);
	}

	public void testInvalidControlContribution() throws Exception {
		// open a window
		IWorkbenchWindow window = openTestWindow();
		// start up our bundle
		getBundle();
		// open another window, now that our invalid contribution is there, it
		// should be parsed and loaded, this ensures the workbench window can
		// still go up even if someone is contributing an invalid contribution
		fWorkbench.openWorkbenchWindow(window.getActivePage().getPerspective()
				.getId(), null);
	}

	@Override
	protected String getExtensionId() {
		return "menu.invalid.toolbar.contribution.bug371611";
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
