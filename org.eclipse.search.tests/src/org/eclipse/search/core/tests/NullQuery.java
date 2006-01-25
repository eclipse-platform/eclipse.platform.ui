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
package org.eclipse.search.core.tests;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;

public class NullQuery implements ISearchQuery {
	
	public IStatus run(IProgressMonitor monitor) {
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchQuery#getName()
	 */
	public String getLabel() {
		return "Null Query"; //$NON-NLS-1$
	}

	public boolean canRerun() {
		return true;
	}

	public boolean canRunInBackground() {
		return true;
	}

	public ISearchResult getSearchResult() {
		return new NullSearchResult(this);
	}
}
