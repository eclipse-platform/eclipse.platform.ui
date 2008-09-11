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
package org.eclipse.search2.internal.ui;

import com.ibm.icu.text.MessageFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;

import org.eclipse.search.internal.ui.SearchPluginImages;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.NewSearchUI;

class SearchHistoryDropDownAction extends Action implements IMenuCreator {

	private class ShowSearchFromHistoryAction extends Action {
		private ISearchResult fSearch;

		public ShowSearchFromHistoryAction(ISearchResult search) {
	        super("", AS_RADIO_BUTTON); //$NON-NLS-1$
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

		public void runWithEvent(Event event) {
			runIfChecked(event.stateMask == SWT.CTRL);
		}

		public void run() {
			runIfChecked(false);
		}

		private void runIfChecked(boolean openNewSearchView) {
			if (isChecked())
				InternalSearchUI.getInstance().showSearchResult(fSearchView, fSearch, openNewSearchView);
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
		setEnabled(hasQueries);
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

		ISearchQuery[] searches= NewSearchUI.getQueries();
		if (searches.length > 0) {
			for (int i= 0; i < searches.length; i++) {
				ISearchResult search= searches[i].getSearchResult();
				ShowSearchFromHistoryAction action= new ShowSearchFromHistoryAction(search);
				action.setChecked(search.equals(currentSearch));
				addActionToMenu(fMenu, action);
			}
			new MenuItem(fMenu, SWT.SEPARATOR);
			addActionToMenu(fMenu, new ShowSearchHistoryDialogAction(fSearchView));
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
