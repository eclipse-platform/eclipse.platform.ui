package org.eclipse.search.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000, 2001
 */
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;

class SearchDropDownAction extends Action implements IMenuCreator {


	public static final int RESULTS_IN_DROP_DOWN= 10;

	private SearchResultViewer fViewer;
	
	public SearchDropDownAction(SearchResultViewer viewer) {
		fViewer= viewer;
		setText(SearchPlugin.getResourceString("SearchResultView.previousSearches.text"));
		setToolTipText(SearchPlugin.getResourceString("SearchResultView.previousSearches.tooltip"));
		setImageDescriptor(SearchPluginImages.DESC_CLCL_SEARCH_HISTROY);
		setMenuCreator(this);
	}

	public void dispose() {
		fViewer= null;
	}

	public Menu getMenu(Menu parent) {
		return null;
	}

	public Menu getMenu(Control parent) {
		Menu menu= new Menu(parent);
		boolean checkedOne= false;
		Iterator iter= SearchManager.getDefault().getPreviousSearches().iterator();
		Search selected= SearchManager.getDefault().getCurrentSearch();
		int i= 0;
		while (iter.hasNext() && i++ < RESULTS_IN_DROP_DOWN) {
			Search search= (Search)iter.next();
			ShowSearchAction action= new ShowSearchAction(search);
			action.setChecked(search.equals(selected));
			if (search.equals(selected))
				checkedOne= true;
			addActionToMenu(menu, action);
		}
		if (iter.hasNext()) {
			new MenuItem(menu, SWT.SEPARATOR);
			Action others= new ShowSearchesAction();
			others.setChecked(!checkedOne);
			addActionToMenu(menu, others);
		}
		return menu;
	}

	protected void addActionToMenu(Menu parent, Action action) {
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(parent, -1);
	}

	public void run() {
			new ShowSearchesAction().run(true);
	}
}
