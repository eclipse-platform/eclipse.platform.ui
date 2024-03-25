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
package org.eclipse.search.internal.ui.text;

import org.eclipse.jface.action.Action;

public class SortAction extends Action {
	private int fSortOrder;
	private FileSearchPage fPage;

	public SortAction(String label, FileSearchPage page, int sortOrder) {
		super(label);
		fPage= page;
		fSortOrder= sortOrder;
	}

	@Override
	public void run() {
		fPage.setSortOrder(fSortOrder);
	}

	public int getSortOrder() {
		return fSortOrder;
	}
}
