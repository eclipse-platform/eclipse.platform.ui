/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
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

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.ui.menus.IMenuService;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @since 3.5
 */
@Ignore("Bug 405296")
public class DynamicContributionTest extends DynamicTestCase {

	@Test
	public void testMenuContribution() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		IMenuService menus = window
				.getService(IMenuService.class);
		MenuManager manager = new MenuManager();
		try {
			menus.populateContributionManager(manager,
					"popup:org.eclipse.newDynamicMenuContribution");
			assertEquals(0, manager.getSize());
			getBundle();
			assertEquals(1, manager.getSize());
			assertFalse(BundleUtility
					.isActive("org.eclipse.newDynamicMenuContribution"));
			manager.createContextMenu(window.getShell());
			manager.updateAll(true);
			assertTrue(BundleUtility
					.isActive("org.eclipse.newDynamicMenuContribution"));
		} finally {
			menus.releaseContributions(manager);
		}
	}

	@Override
	protected String getExtensionId() {
		return "menu.dynamic.contribution";
	}

	@Override
	protected String getExtensionPoint() {
		return IWorkbenchRegistryConstants.PL_MENUS;
	}

	@Override
	protected String getInstallLocation() {
		return "data/org.eclipse.newDynamicMenuContribution";
	}

}
