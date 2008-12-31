/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search2.internal.ui.basic.views;

import org.eclipse.jface.action.Action;

import org.eclipse.search.internal.ui.SearchPluginImages;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;

import org.eclipse.search2.internal.ui.SearchMessages;

public class RemoveSelectedMatchesAction extends Action {

	private AbstractTextSearchViewPage fPage;

	public RemoveSelectedMatchesAction(AbstractTextSearchViewPage page) {
		fPage= page;
		setText(SearchMessages.RemoveSelectedMatchesAction_label);
		setToolTipText(SearchMessages.RemoveSelectedMatchesAction_tooltip);
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_REM);
	}

	public void run() {
		fPage.internalRemoveSelected();
	}
}
