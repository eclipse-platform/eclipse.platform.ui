/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
@Deprecated
class SearchDropDownAction extends Action implements IMenuCreator {


	public static final int RESULTS_IN_DROP_DOWN= 10;

	private Menu fMenu;

	public SearchDropDownAction() {
		setText(SearchMessages.SearchResultView_previousSearches_text);
		setToolTipText(SearchMessages.SearchResultView_previousSearches_tooltip);
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_HISTORY);
		setMenuCreator(this);
	}

	@Override
	public void dispose() {
		if (fMenu != null)  {
			fMenu.dispose();
			fMenu= null;
		}
	}

	@Override
	public Menu getMenu(Menu parent) {
		return null;
	}

	@Override
	public Menu getMenu(Control parent) {
		if (fMenu != null)
			fMenu.dispose();

		fMenu= new Menu(parent);
		boolean checkedOne= false;
		Iterator<Search> iter= SearchManager.getDefault().getPreviousSearches().iterator();
		Search selected= SearchManager.getDefault().getCurrentSearch();
		int i= 0;
		while (iter.hasNext() && i++ < RESULTS_IN_DROP_DOWN) {
			Search search= iter.next();
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

	@Override
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
