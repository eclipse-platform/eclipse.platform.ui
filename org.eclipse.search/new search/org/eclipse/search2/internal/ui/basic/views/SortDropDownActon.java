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
package org.eclipse.search2.internal.ui.basic.views;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;

import org.eclipse.search.internal.ui.SearchPluginImages;

import org.eclipse.search2.internal.ui.SearchMessages;

public class SortDropDownActon extends Action implements IMenuCreator {


	public static final int RESULTS_IN_DROP_DOWN= 10;

	private Menu fMenu;
	private DefaultSearchViewPage fPage;
	
	public SortDropDownActon(DefaultSearchViewPage page) {
		super(SearchMessages.getString("SortDropDownActon.label"), Action.AS_DROP_DOWN_MENU); //$NON-NLS-1$
		setToolTipText(SearchMessages.getString("SortDropDownActon.tooltip")); //$NON-NLS-1$
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_SORT);
		fPage= page;
		setMenuCreator(this);
	}

	public void dispose() {
		if (fMenu != null && !fMenu.isDisposed())
			fMenu.dispose();
		fMenu= null;
	}

	public Menu getMenu(Menu parent) {
		dispose();
		
		MenuManager mgr= new MenuManager();
		fMenu= new Menu(parent);
		fillMenu(mgr);
		mgr.fill(parent, 0);
		return fMenu;
	}

	public Menu getMenu(Control parent) {
		
		dispose();
		
		MenuManager mgr= new MenuManager("popup"); //$NON-NLS-1$
		fillMenu(mgr);
		fMenu= mgr.createContextMenu(parent);
		return fMenu;
	}

	void fillMenu(IMenuManager menu) {
		String[] attributes= fPage.getSortOrder();
		for (int i= 0; i < attributes.length; i++) {
			Action action= new SortByAction(fPage, attributes[i]);
			addActionToMenu(menu, action);
		}
		Separator separator= new Separator();
		menu.add(separator);
		NoSortAction action= new NoSortAction(fPage, SearchMessages.getString("SortDropDownActon.ascending.label"), DefaultSearchViewPage.SORT_ASCENDING); //$NON-NLS-1$
		action.setChecked(fPage.getSortDirection() == DefaultSearchViewPage.SORT_ASCENDING);
		addActionToMenu(menu, action);

		action= new NoSortAction(fPage, SearchMessages.getString("SortDropDownActon.descending.label"), DefaultSearchViewPage.SORT_DESCENDING); //$NON-NLS-1$
		action.setChecked(fPage.getSortDirection() == DefaultSearchViewPage.SORT_DESCENDING);
		addActionToMenu(menu, action);

		action= new NoSortAction(fPage, SearchMessages.getString("SortDropDownActon.nosort.label"), DefaultSearchViewPage.SORT_NOT); //$NON-NLS-1$
		action.setChecked(fPage.getSortDirection() == DefaultSearchViewPage.SORT_NOT);
		addActionToMenu(menu, action);
	}

	protected void addActionToMenu(IMenuManager parent, Action action) {
		parent.add(action);
	}
	
	public void run() {
		fPage.cycleSortAttribute();
	}
}
