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
package org.eclipse.update.internal.ui.wizards;

import java.lang.reflect.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.search.*;

/**
 * Runs a search query
 */
public class SearchRunner {
	private Shell shell;
	private IRunnableContext context;
	private ISearchProvider searchProvider;
	private IUpdateSearchResultCollector collector;
	private boolean newSearchNeeded;
	
	public SearchRunner(Shell shell, IRunnableContext context) {
		this.shell = shell;
		this.context = context;
	}
	
	public void setResultCollector(IUpdateSearchResultCollector collector) {
		this.collector = collector;
	}
	
	public ISearchProvider getSearchProvider() {
		return searchProvider;
	}
	
	public void setSearchProvider(ISearchProvider searchProvider) {
		if (this.searchProvider!=searchProvider)
			newSearchNeeded = true;
		this.searchProvider = searchProvider;
	}
	
	public void setNewSearchNeeded(boolean value) {
		newSearchNeeded = value;
	}
	
	public boolean isNewSearchNeeded() {
		return newSearchNeeded;
	}

	public void runSearch() {
		if (searchProvider==null) return;
		try {
			context.run(true, true, getSearchOperation(collector));
			newSearchNeeded=false;
		} catch (InterruptedException e) {
			if (!"cancel".equals(e.getMessage()))
				UpdateUI.logException(e);
			newSearchNeeded=true;
			return;
		} catch (InvocationTargetException e) {
			Throwable t = e.getTargetException();
			if (t instanceof CoreException) {
				CoreException ce = (CoreException)t;
				IStatus status = ce.getStatus();
				if (status!=null &&
					status.getCode()==ISite.SITE_ACCESS_EXCEPTION) {
					// Just show this but do not throw exception
					// because there may be results anyway.
					ErrorDialog.openError(shell,UpdateUI.getString("SearchRunner.connectionError"), //$NON-NLS-1$
						null, 
						status);
					return;
				}
			}
			UpdateUI.logException(e);
			return;
		}
	}

	private IRunnableWithProgress getSearchOperation(final IUpdateSearchResultCollector collector) {
		final UpdateSearchRequest request = searchProvider.getSearchRequest();
		
		IRunnableWithProgress op = new IRunnableWithProgress () {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					request.performSearch(collector, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
					if (monitor.isCanceled()) {
						newSearchNeeded = true;
						throw new InterruptedException("cancel");
					}
				}
			}
		};
		return op;
	}
}
