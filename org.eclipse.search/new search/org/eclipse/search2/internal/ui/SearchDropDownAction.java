/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search2.internal.ui;

import java.text.MessageFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;

import org.eclipse.search.internal.ui.SearchPluginImages;

class SearchDropDownAction extends Action implements IMenuCreator {


	public static final int RESULTS_IN_DROP_DOWN= 10;

	private Menu fMenu;
	private SearchView fSearchView;
	
	public SearchDropDownAction(SearchView searchView) {
		setText(SearchMessages.SearchDropDownAction_label); 
		setToolTipText(SearchMessages.SearchDropDownAction_tooltip); 
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_HISTORY);
		fSearchView= searchView;
		setMenuCreator(this);
	}

	public void dispose() {
		disposeMenu();
	}

	void disposeMenu() {
		if (fMenu != null)
			fMenu.dispose();
	}

	public Menu getMenu(Menu parent) {
		return null;
	}

	public Menu getMenu(Control parent) {
		ISearchQuery currentQuery= null;
		ISearchResult currentSearch= fSearchView.getCurrentSearchResult();
		if (currentSearch != null)
			currentQuery= currentSearch.getQuery();
		disposeMenu();
		
		fMenu= new Menu(parent);
		ISearchQuery[] searches= InternalSearchUI.getInstance().getSearchManager().getQueries();
		for (int i= 0; i < searches.length; i++) {
			ISearchResult search= searches[i].getSearchResult();
			String label= escapeAmp(search.getLabel());
			String tooltip= search.getTooltip();
			ImageDescriptor image= search.getImageDescriptor();
			if (InternalSearchUI.getInstance().isQueryRunning(search.getQuery()))
				label= MessageFormat.format(SearchMessages.SearchDropDownAction_running_message, new String[] { label }); 
			ShowSearchAction action= new ShowSearchAction(fSearchView, search, label, image, tooltip );
			if (searches[i].equals(currentQuery))
				action.setChecked(true);
			addActionToMenu(fMenu, action);
		}
		if (searches.length > 0) {
			new MenuItem(fMenu, SWT.SEPARATOR);
			addActionToMenu(fMenu, new RemoveAllSearchesAction());
		}
		return fMenu;
	}

	private String escapeAmp(String label) {
		StringBuffer buf= new StringBuffer();
		for (int i= 0; i < label.length(); i++) {
			char ch= label.charAt(i);
			buf.append(ch);
			if (ch == '&') {
				buf.append('&');
			}
		}
		return buf.toString();
	}

	protected void addActionToMenu(Menu parent, Action action) {
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(parent, -1);
	}

	public void run() {
		new ShowSearchesAction(fSearchView).run();
	}
}
