/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.search2.internal.ui.text2;


import org.eclipse.core.runtime.CoreException;

import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.TextSearchQueryProvider;

import org.eclipse.search2.internal.ui.SearchMessages;


/**
 * @author markus.schorn@windriver.com
 */
public class FindInWorkspaceActionDelegate extends FindInRecentScopeActionDelegate {

	public FindInWorkspaceActionDelegate() {
		super(SearchMessages.FindInWorkspaceActionDelegate_text);
		setActionDefinitionId("org.eclipse.search.ui.performTextSearchWorkspace"); //$NON-NLS-1$
	}

	@Override
	protected ISearchQuery createQuery(TextSearchQueryProvider provider, String searchForString) throws CoreException {
		return provider.createQuery(searchForString);
	}
}
