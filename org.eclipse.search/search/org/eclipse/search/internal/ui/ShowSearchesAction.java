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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;

import org.eclipse.search.internal.ui.util.ListDialog;

/**
 * Invoke the resource creation wizard selection Wizard.
 * This action will retarget to the active view.
 */
class ShowSearchesAction extends Action {

	private static final class SearchesLabelProvider extends LabelProvider {
		
		private ArrayList fImages= new ArrayList();
		
		public String getText(Object element) {
			if (!(element instanceof ShowSearchAction))
				return ""; //$NON-NLS-1$
			return ((ShowSearchAction)element).getText();
		}
		public Image getImage(Object element) {
			if (!(element instanceof ShowSearchAction))
				return null;

			ImageDescriptor imageDescriptor= ((ShowSearchAction)element).getImageDescriptor(); 
			if (imageDescriptor == null)
				return null;
			
			Image image= imageDescriptor.createImage();
			fImages.add(image);

			return image;
		}
		
		public void dispose() {
			Iterator iter= fImages.iterator();
			while (iter.hasNext())
				((Image)iter.next()).dispose();
			
			fImages= null;
		}
	}

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
		
		LabelProvider labelProvider=new SearchesLabelProvider();

		ListDialog dlg= new ListDialog(SearchPlugin.getActiveWorkbenchShell(),input, title, message, new SearchResultContentProvider(), labelProvider);
		if (selectedAction != null) {
			Object[] selected= new Object[1];
			selected[0]= selectedAction;
			dlg.setInitialSelections(selected);
		}
		if (dlg.open() == Window.OK) {
			List result= Arrays.asList(dlg.getResult());
			if (result != null && result.size() == 1) {
				((ShowSearchAction)result.get(0)).run();
			}
		}
	}
}
