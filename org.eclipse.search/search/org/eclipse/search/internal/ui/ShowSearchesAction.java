/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.LabelProvider;

import org.eclipse.search.internal.ui.util.ListDialog;

/**
 * Invoke the resource creation wizard selection Wizard.
 * This action will retarget to the active view.
 */
class ShowSearchesAction extends Action {

	private static final LabelProvider fgLabelProvider= new LabelProvider() {
		public String getText(Object element) {
			if (!(element instanceof ShowSearchAction))
				return ""; //$NON-NLS-1$
			return ((ShowSearchAction)element).getText();
		}
		public Image getImage(Object element) {
			if (!(element instanceof ShowSearchAction))
				return null;
			return ((ShowSearchAction)element).getImageDescriptor().createImage();
		}
	};

	/**
	 *	Create a new instance of this class
	 */
	public ShowSearchesAction() {
		super(SearchMessages.getString("ShowOtherSearchesAction.label")); //$NON-NLS-1$
		setToolTipText(SearchMessages.getString("ShowOtherSearchesAction.tooltip")); //$NON-NLS-1$
	}
	/*
	 * Overrides method from Action
	 */
	public void run() {
		run(false);
	}
	 
	public void run(boolean showAll) {
		Iterator iter= SearchManager.getDefault().getPreviousSearches().iterator();
		int cutOffSize;
		if (showAll)
			cutOffSize= 0;
		else
			cutOffSize= SearchDropDownAction.RESULTS_IN_DROP_DOWN;
		int size= SearchManager.getDefault().getPreviousSearches().size() - cutOffSize;
		Search selectedSearch= SearchManager.getDefault().getCurrentSearch();
		Action selectedAction = null;
		ArrayList input= new ArrayList(size);
		int i= 0;
		while (iter.hasNext()) {
			Search search= (Search)iter.next();
			if (i++ < cutOffSize)
				continue;
			Action action= new ShowSearchAction(search);
			input.add(action);
			if (selectedSearch == search)
				selectedAction= action;
		}

		// Open a list dialog.
		String title;
		String message;
		if (showAll) {
			title= SearchMessages.getString("PreviousSearchesDialog.title"); //$NON-NLS-1$
			message= SearchMessages.getString("PreviousSearchesDialog.message"); //$NON-NLS-1$
		}
		else {
			title= SearchMessages.getString("OtherSearchesDialog.title"); //$NON-NLS-1$
			message= SearchMessages.getString("OtherSearchesDialog.message"); //$NON-NLS-1$
		}		
		ListDialog dlg= new ListDialog(SearchPlugin.getActiveWorkbenchShell(),input, title, message, new SearchResultContentProvider(), fgLabelProvider);
		if (selectedAction != null) {
			Object[] selected= new Object[1];
			selected[0]= selectedAction;
			dlg.setInitialSelections(selected);
		}
		if (dlg.open() == dlg.OK) {
			List result= Arrays.asList(dlg.getResult());
			if (result != null && result.size() == 1) {
				((ShowSearchAction)result.get(0)).run();
			}
		}
	}
}
