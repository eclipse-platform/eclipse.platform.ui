/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search2.internal.ui.basic.views;

import org.eclipse.jface.action.Action;

import org.eclipse.search2.internal.ui.SearchMessages;

class ToggleModeAction extends Action {

	private DefaultSearchViewPage fPage;

	public ToggleModeAction(DefaultSearchViewPage page) {
		fPage= page;
		setText(SearchMessages.getString("ToggleModeAction.label")); //$NON-NLS-1$
		setToolTipText(SearchMessages.getString("ToggleModeAction.tooltip")); //$NON-NLS-1$
		setChecked(!page.isFlatMode());
	}
	
	public void run() {
		fPage.toggleMode();
		setChecked(!fPage.isFlatMode());
	}
	
}
