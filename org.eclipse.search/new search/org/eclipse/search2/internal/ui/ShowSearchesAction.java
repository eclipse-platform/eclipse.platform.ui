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
package org.eclipse.search2.internal.ui;

import java.util.ArrayList;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultManager;
import org.eclipse.search.ui.NewSearchUI;

/**
 * Invoke the resource creation wizard selection Wizard.
 * This action will retarget to the active view.
 */
class ShowSearchesAction extends Action {
	private SearchView fSearchView;

	/*private static final class SearchesLabelProvider extends LabelProvider {
		
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
	}*/

	/**
	 *	Create a new instance of this class
	 */
	public ShowSearchesAction(SearchView searchView) {
		super(SearchMessages.getString("ShowSearchesAction.label")); //$NON-NLS-1$
		setToolTipText(SearchMessages.getString("ShowSearchesAction.tooltip")); //$NON-NLS-1$
		fSearchView= searchView;
	}
	 
	public void run() {
		ISearchResultManager sm= NewSearchUI.getSearchManager();
		ISearchResult[] searches= sm.getSearchResults();

		ArrayList input= new ArrayList();
		int i= 0;
		for (int j= 0; j < searches.length; j++) {
			ISearchResult search= searches[i];
			String label= searches[i].getText();
			String tooltip= search.getTooltip();
			ImageDescriptor image= search.getImageDescriptor();
			ShowSearchAction action= new ShowSearchAction(fSearchView, searches[i], label, image, tooltip );
			input.add(action);
		}
	}
}
