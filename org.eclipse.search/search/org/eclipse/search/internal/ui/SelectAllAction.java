/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
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