/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui;

import org.eclipse.jface.action.Action;

class ShowSearchAction extends Action {
	private Search fSearch;
	
	/**
	 *	Create a new instance of this class
	 */
	public ShowSearchAction(Search search) {
		fSearch= search;
		String desc= search.getShortDescription();
		setText(desc);
		setToolTipText(desc);
		setImageDescriptor(search.getImageDescriptor());
	}
	/**
	 *	Invoke the resource wizard selection wizard
	 *
	 *	@param browser org.eclipse.jface.parts.Window
	 */
	public void run() {
		if (fSearch != SearchManager.getDefault().getCurrentSearch())
			SearchManager.getDefault().setCurrentSearch(fSearch);
	}
}
