/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui;

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
		setText(SearchMessages.getString("SearchResultView.previousSearches.text")); //$NON-NLS-1$
		setToolTipText(SearchMessages.getString("SearchResultView.previousSearches.tooltip")); //$NON-NLS-1$
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_HISTORY);
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
		new MenuItem(menu, SWT.SEPARATOR);
		if (iter.hasNext()) {
			Action others= new ShowSearchesAction();
			others.setChecked(!checkedOne);
			addActionToMenu(menu, others);
		}
		addActionToMenu(menu, new RemoveAllSearchesAction());
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
