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
package org.eclipse.search2.internal.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;


class RemoveAllSearchesAction extends Action {

	public RemoveAllSearchesAction() {
		super(SearchMessages.getString("RemoveAllSearchesAction.label")); //$NON-NLS-1$
		setToolTipText(SearchMessages.getString("RemoveAllSearchesAction.tooltip")); //$NON-NLS-1$
	}	
	
	public void run() {
		ISearchQuery[] queries= NewSearchUI.getQueries();
		for (int i = 0; i < queries.length; i++) {
			if (!NewSearchUI.isQueryRunning(queries[i]))
				InternalSearchUI.getInstance().removeQuery(queries[i]);
		}
	}
}
