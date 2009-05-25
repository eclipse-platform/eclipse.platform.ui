/*******************************************************************************
 * Copyright (c) 2003, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.navigator.extension;

import junit.framework.Assert;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;

public class TestActionProviderDependent extends CommonActionProvider {

	private IAction action = null;
	
	public void init(ICommonActionExtensionSite aConfig) {
		 action = new TestActionDependent(aConfig.getViewSite().getShell(), aConfig.getExtensionId());
	}
	
	public void fillContextMenu(IMenuManager menu) { 
		IMenuManager submenu = menu.findMenuUsingPath(TestActionProviderMenu.GROUP_TEST_MENU);
		Assert.assertNotNull("The submenu should have been added by TestActionProviderMenu!", submenu);
		submenu.insertAfter(TestActionProviderMenu.GROUP_TEST_DEPENDENCY, action);
	}

}
