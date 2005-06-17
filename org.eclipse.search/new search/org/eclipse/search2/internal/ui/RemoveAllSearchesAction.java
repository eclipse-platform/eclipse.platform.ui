/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
		super(SearchMessages.RemoveAllSearchesAction_label); 
		setToolTipText(SearchMessages.RemoveAllSearchesAction_tooltip); 
	}	
	
	public void run() {
		ISearchQuery[] queries= NewSearchUI.getQueries();
		for (int i = 0; i < queries.length; i++) {
			if (!NewSearchUI.isQueryRunning(queries[i]))
				InternalSearchUI.getInstance().removeQuery(queries[i]);
		}
	}
}
