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

import org.eclipse.jface.util.Assert;

import org.eclipse.search.ui.NewSearchUI;

import org.eclipse.search.internal.core.SearchScope;
import org.eclipse.search.internal.ui.Messages;
import org.eclipse.search.internal.ui.SearchMessages;

public class TextSearchEngine {
		
	public IStatus search(SearchScope scope, boolean visitDerived, ITextSearchResultCollector collector, MatchLocator matchLocator) {
		Assert.isNotNull(scope);
		Assert.isNotNull(collector);
		Assert.isNotNull(matchLocator);
		IProgressMonitor monitor= collector.getProgressMonitor();
		
		String message= SearchMessages.TextSearchEngine_statusMessage; 
		MultiStatus status= new MultiStatus(NewSearchUI.PLUGIN_ID, IStatus.OK, message, null);
		
		int amountOfWork= new AmountOfWorkCalculator(scope, status, visitDerived).process();
		try {
			monitor.beginTask("", amountOfWork); //$NON-NLS-1$
			if (amountOfWork > 0) {
				Integer[] args= new Integer[] {new Integer(1), new Integer(amountOfWork)};
				monitor.setTaskName(Messages.format(SearchMessages.TextSearchEngine_scanning, args)); 
			}				
			collector.aboutToStart();
			TextSearchVisitor visitor= new TextSearchVisitor(matchLocator, scope, visitDerived, collector, status, amountOfWork);
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
