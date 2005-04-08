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
package org.eclipse.search.internal.core.text;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;

import org.eclipse.core.resources.IWorkspace;

import org.eclipse.jface.util.Assert;

import org.eclipse.search.ui.NewSearchUI;

import org.eclipse.search.internal.core.SearchScope;
import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.internal.ui.SearchPlugin;

public class TextSearchEngine {
	
	/**
	 * @param workspace Current worspave
	 * @param scope Search scope
	 * @param visitDerived Select to visit derived resource
	 * @param collector
	 * @param matchLocator
	 * @return Returns the status
	 * @deprecated Use {@link #search(SearchScope, boolean, ITextSearchResultCollector, MatchLocator)} instead
	 */
	public IStatus search(IWorkspace workspace, SearchScope scope, boolean visitDerived, ITextSearchResultCollector collector, MatchLocator matchLocator) {
		return search(scope, visitDerived, collector, matchLocator);
	}
	
	public IStatus search(SearchScope scope, boolean visitDerived, ITextSearchResultCollector collector, MatchLocator matchLocator) {
		boolean disableNIOSearch= SearchPlugin.getDefault().getPluginPreferences().getBoolean("org.eclipse.search.disableNIOSearch"); //$NON-NLS-1$
		return search(scope, visitDerived, collector, matchLocator, !disableNIOSearch);
	}
	
	public IStatus search(SearchScope scope, boolean visitDerived, ITextSearchResultCollector collector, MatchLocator matchLocator, boolean allowNIOSearch) {
		Assert.isNotNull(scope);
		Assert.isNotNull(collector);
		Assert.isNotNull(matchLocator);
		IProgressMonitor monitor= collector.getProgressMonitor();
		
		String message= SearchMessages.getString("TextSearchEngine.statusMessage"); //$NON-NLS-1$
		MultiStatus status= new MultiStatus(NewSearchUI.PLUGIN_ID, IStatus.OK, message, null);
		
		int amountOfWork= new AmountOfWorkCalculator(scope, status, visitDerived).process();
		try {
			monitor.beginTask("", amountOfWork); //$NON-NLS-1$
			if (amountOfWork > 0) {
				Integer[] args= new Integer[] {new Integer(1), new Integer(amountOfWork)};
				monitor.setTaskName(SearchMessages.getFormattedString("TextSearchEngine.scanning", args)); //$NON-NLS-1$
			}				
			collector.aboutToStart();
			TextSearchVisitor visitor= new TextSearchVisitor(matchLocator, scope, visitDerived, collector, status, amountOfWork);
			visitor.setAllowNIOSearch(allowNIOSearch);
			visitor.process();
		} catch (CoreException ex) {
			status.add(ex.getStatus());
		} finally {
			monitor.done();
			try {
				collector.done();
			} catch (CoreException ex) {
				status.add(ex.getStatus());
			}
		}
		return status;
	}
}
