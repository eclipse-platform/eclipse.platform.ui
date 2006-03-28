/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui;

import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;

/**
 * @deprecated old search
 */
class SearchDropDownAction extends Action implements IMenuCreator {


	public static final int RESULTS_IN_DROP_DOWN= 10;

	private Menu fMenu;
	
	public SearchDropDownAction() {
		setText(SearchMessages.SearchResultView_previousSearches_text); 
		setToolTipText(SearchMessages.SearchResultView_previousSearches_tooltip); 
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_HISTORY);
		setMenuCreator(this);
	}

	public void dispose() {
		if (fMenu != null)  {
			fMenu.dispose();
			fMenu= null;
		}
	}

	public Menu getMenu(Menu parent) {
		return null;
	}

	public Menu getMenu(Control parent) {
		if (fMenu != null)
			fMenu.dispose();
		
		fMenu= new Menu(parent);
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
			addActionToMenu(fMenu, action);
		}
		new MenuItem(fMenu, SWT.SEPARATOR);
		if (iter.hasNext()) {
			Action others= new ShowSearchesAction();
			others.setChecked(!checkedOne);
			addActionToMenu(fMenu, others);
		}
		addActionToMenu(fMenu, new RemoveAllSearchesAction());
		return fMenu;
	}

	protected void addActionToMenu(Menu parent, Action action) {
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(parent, -1);
	}

	public void run() {
			new ShowSearchesAction().run(true);
	}

	/**
	 * Get's rid of the menu, because the menu hangs on to 
	 * the searches, etc.
	 */
	void clear() {
		dispose();
	}
}
