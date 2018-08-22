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

import org.eclipse.jface.action.Action;

/**
 * @deprecated old search
 */
@Deprecated
class ShowSearchAction extends Action {
	private Search fSearch;

	/**
	 * Create a new instance of this class.
	 *
	 * @param search the search
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
	 */
	@Override
	public void run() {
		if (fSearch != SearchManager.getDefault().getCurrentSearch())
			SearchManager.getDefault().setCurrentSearch(fSearch);
	}
}
