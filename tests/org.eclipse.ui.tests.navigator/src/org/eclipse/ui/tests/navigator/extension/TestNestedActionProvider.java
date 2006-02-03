/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.ui.tests.navigator.extension;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.CommonActionProviderConfig;
import org.eclipse.ui.navigator.ICommonMenuConstants;

public class TestNestedActionProvider extends CommonActionProvider {

	public static final String GROUP_TEST_MENU = "group.testMenu"; 

	public static final String GROUP_TEST_DEPENDENCY = "group.testDependency"; 
	
	private IAction action = null;
	
	public void init(CommonActionProviderConfig aConfig) {
		 action = new TestAction(aConfig.getViewSite().getShell(), "Nested action (only visible if test ext active)");
	}
	
	public void fillContextMenu(IMenuManager menu) {  
		menu.insertAfter(ICommonMenuConstants.GROUP_ADDITIONS, action);
	}

}
