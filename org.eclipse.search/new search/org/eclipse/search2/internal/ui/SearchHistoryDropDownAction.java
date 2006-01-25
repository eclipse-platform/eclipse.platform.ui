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

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;

import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;

import org.eclipse.search.internal.ui.SearchPluginImages;

import org.osgi.framework.Bundle;

class SearchHistoryDropDownAction extends Action implements IMenuCreator {

	private class ShowSearchFromHistoryAction extends Action {
		private ISearchResult fSearch;

		public ShowSearchFromHistoryAction(ISearchResult search) {
			fSearch= search;
			
			String label= escapeAmp(search.getLabel());
			if (InternalSearchUI.getInstance().isQueryRunning(search.getQuery()))
				label= MessageFormat.format(SearchMessages.SearchDropDownAction_running_message, new String[] { label }); 
			// fix for bug 38049
			if (label.indexOf('@') >= 0)
				label+= '@';
			setText(label);
			setImageDescriptor(search.getImageDescriptor());
			setToolTipText(search.getTooltip());
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
		
		public void run() {
			InternalSearchUI.getInstance().getSearchManager().touch(fSearch.getQuery());
			fSearchView.showSearchResult(fSearch);
		}
	}
	
	private class ShowEmptySearchAction extends Action {
		private final String fPageId;

		public ShowEmptySearchAction(IConfigurationElement elem) {
			fPageId= elem.getAttribute(SearchPageRegistry.ATTRIB_ID);
			
			String pageLabel= elem.getAttribute(SearchPageRegistry.ATTRIB_LABEL);
			String text= Messages.format(SearchMessages.SearchHistoryDropDownAction_showemptyview_title, pageLabel);
			setText(text);
			
			String tooltip= Messages.format(SearchMessages.SearchHistoryDropDownAction_showemptyview_tooltip, pageLabel);
			setToolTipText(tooltip);
			
			String imageName= elem.getAttribute(SearchPageRegistry.ATTRIB_ICON);
			if (imageName != null) {
				Bundle bundle = Platform.getBundle(elem.getNamespace());
				setImageDescriptor(SearchPluginImages.createImageDescriptor(bundle, new Path(imageName), true));
			}
		}
		
		public void run() {
			fSearchView.showEmptySearchPage(fPageId);
		}
	}
	

	public static final int RESULTS_IN_DROP_DOWN= 10;

	private Menu fMenu;
	private SearchView fSearchView;
	
	public SearchHistoryDropDownAction(SearchView searchView) {
		setText(SearchMessages.SearchDropDownAction_label); 
		setToolTipText(SearchMessages.SearchDropDownAction_tooltip); 
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_HISTORY);
		fSearchView= searchView;
		setMenuCreator(this);
	}
	
	public void updateEnablement() {
		boolean hasQueries= InternalSearchUI.getInstance().getSearchManager().hasQueries();
		boolean emptyPageExt= fSearchView.getSearchPageRegistry().hasEmptyPageExtensions();
		setEnabled(hasQueries || emptyPageExt);
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
		ISearchResult currentSearch= fSearchView.getCurrentSearchResult();
		disposeMenu();
		
		fMenu= new Menu(parent);
		
		IConfigurationElement[] emptyPageExtensions= fSearchView.getSearchPageRegistry().getEmptyPageExtensions();
		if (emptyPageExtensions.length > 0) {
			for (int i= 0; i < emptyPageExtensions.length; i++) {
				ShowEmptySearchAction action= new ShowEmptySearchAction(emptyPageExtensions[i]);
				addActionToMenu(fMenu, action);
			}
		}
		
		ISearchQuery[] searches= InternalSearchUI.getInstance().getSearchManager().getQueries();
		if (searches.length > 0) {
			if (emptyPageExtensions.length > 0) {
				new MenuItem(fMenu, SWT.SEPARATOR);
			}
			
			for (int i= 0; i < searches.length; i++) {
				ISearchResult search= searches[i].getSearchResult();
				ShowSearchFromHistoryAction action= new ShowSearchFromHistoryAction(search);
				action.setChecked(search.equals(currentSearch));
				addActionToMenu(fMenu, action);
			}
			new MenuItem(fMenu, SWT.SEPARATOR);
			addActionToMenu(fMenu, new RemoveAllSearchesAction());
		}
		return fMenu;
	}

	protected void addActionToMenu(Menu parent, Action action) {
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(parent, -1);
	}

	public void run() {
		new ShowSearchHistoryDialogAction(fSearchView).run();
	}
}
