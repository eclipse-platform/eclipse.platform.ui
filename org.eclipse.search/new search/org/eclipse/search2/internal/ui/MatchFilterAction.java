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

import java.util.ArrayList;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

import org.eclipse.ui.texteditor.IUpdate;

import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.MatchFilter;


public class MatchFilterAction extends Action implements IUpdate {

	private MatchFilter fFilter;
	private AbstractTextSearchViewPage fPage;

	public MatchFilterAction(AbstractTextSearchViewPage page, MatchFilter filter) {
		super(filter.getActionLabel(), IAction.AS_CHECK_BOX);
		fPage= page;
		fFilter= filter;
		setId("MatchFilterAction." + filter.getID()); //$NON-NLS-1$
		setChecked(isActiveMatchFilter());
	}

	public void run() {
		AbstractTextSearchResult input= fPage.getInput();
		if (input == null) {
			return;
		}
		ArrayList newFilters= new ArrayList();
		MatchFilter[] activeMatchFilters= input.getActiveMatchFilters();
		if (activeMatchFilters == null) {
			return;
		}

		for (int i= 0; i < activeMatchFilters.length; i++) {
			if (!activeMatchFilters[i].equals(fFilter)) {
				newFilters.add(activeMatchFilters[i]);
			}
		}
		boolean newState= isChecked();
		if (newState) {
			newFilters.add(fFilter);
		}
		input.setActiveMatchFilters((MatchFilter[]) newFilters.toArray(new MatchFilter[newFilters.size()]));
	}

	public MatchFilter getFilter() {
		return fFilter;
	}

	private boolean isActiveMatchFilter() {
		AbstractTextSearchResult input= fPage.getInput();
		if (input != null) {
			MatchFilter[] activeMatchFilters= input.getActiveMatchFilters();
			for (int i= 0; i < activeMatchFilters.length; i++) {
				if (fFilter.equals(activeMatchFilters[i])) {
					return true;
				}
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		setChecked(isActiveMatchFilter());
	}
}