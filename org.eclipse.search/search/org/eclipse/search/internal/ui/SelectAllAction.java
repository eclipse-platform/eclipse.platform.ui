/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui;

import org.eclipse.jface.action.Action;

import org.eclipse.ui.help.WorkbenchHelp;
 
/**
 * This action selects all entries currently showing in view.
 */
class SelectAllAction extends Action {

	private SearchResultViewer fViewer;

	/**
	 * Creates the action.
	 */
	SelectAllAction(SearchResultViewer viewer) {
		super("selectAll"); //$NON-NLS-1$
		setText(SearchMessages.getString("SelectAllAction.label")); //$NON-NLS-1$
		setToolTipText(SearchMessages.getString("SelectAllAction.tooltip")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, ISearchHelpContextIds.SELECT_ALL_ACTION);
		fViewer= viewer;
	}

	/**
	 * Selects all resources in the view.
	 */
	public void run() {
		fViewer.getTable().selectAll();
		// force viewer selection change
		fViewer.setSelection(fViewer.getSelection());
	}
}
