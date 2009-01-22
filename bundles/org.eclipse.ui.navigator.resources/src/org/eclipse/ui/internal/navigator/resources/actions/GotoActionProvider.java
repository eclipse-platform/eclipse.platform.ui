/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Oakland Software (Francis Upton - francisu@ieee.org) 
 *        bug 214271 Undo/redo not enabled if nothing selected
 ******************************************************************************/

package org.eclipse.ui.internal.navigator.resources.actions;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;

/**
 * @since 3.3
 * 
 */
public class GotoActionProvider extends CommonActionProvider {

	private GotoResourceAction gotoAction;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.CommonActionProvider#init(org.eclipse.ui.navigator.ICommonActionExtensionSite)
	 */
	public void init(ICommonActionExtensionSite anActionSite) {
		gotoAction = new GotoResourceAction(anActionSite.getViewSite().getShell(), (CommonViewer)anActionSite.getStructuredViewer());
	}


	public void fillActionBars(IActionBars actionBars) {
		actionBars.setGlobalActionHandler(
				IWorkbenchActionConstants.GO_TO_RESOURCE, gotoAction);
		updateActionBars();
	}

}
