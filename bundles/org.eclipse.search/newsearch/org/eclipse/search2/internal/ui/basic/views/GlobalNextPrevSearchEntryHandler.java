/*******************************************************************************
 * Copyright (c) 2025 Eclipse Foundation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search2.internal.ui.basic.views;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;

import org.eclipse.search.ui.ISearchResultPage;
import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;

/**
 * Global handler for navigating to next/previous search results without
 * requiring focus on the Search view. Navigates directly through the active
 * search result page, keeping focus in the editor throughout.
 * <p>
 * Configured via the {@code :next} or {@code :previous} data suffix in
 * {@code plugin.xml}, e.g.:
 * {@code defaultHandler="...GlobalNextPrevSearchEntryHandler:previous"}
 * </p>
 */
public class GlobalNextPrevSearchEntryHandler extends AbstractHandler implements IExecutableExtension {

	private boolean navigateNext = true;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISearchResultViewPart viewPart = NewSearchUI.getSearchResultView();
		if (viewPart == null) {
			return null; // No search has been run yet
		}
		ISearchResultPage page = viewPart.getActivePage();
		if (page instanceof AbstractTextSearchViewPage textPage) {
			if (navigateNext) {
				textPage.gotoNextMatch();
			} else {
				textPage.gotoPreviousMatch();
			}
		}
		return null;
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException {
		navigateNext = !"previous".equals(data); //$NON-NLS-1$
	}
}
