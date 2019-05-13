/*******************************************************************************
 * Copyright (c) 2003, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.navigator.extension;


import org.junit.Assert;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;

import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;

public class TestActionProviderDependent extends CommonActionProvider {

	private IAction action = null;

	@Override
	public void init(ICommonActionExtensionSite aConfig) {
		action = new TestActionDependent(aConfig.getViewSite().getShell(), aConfig.getExtensionId());
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		IMenuManager submenu = menu.findMenuUsingPath(TestActionProviderMenu.GROUP_TEST_MENU);
		Assert.assertNotNull("The submenu should have been added by TestActionProviderMenu!", submenu);
		submenu.insertAfter(TestActionProviderMenu.GROUP_TEST_DEPENDENCY, action);
	}

}
