/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.core.text;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.ui.ISearchJob;
import org.eclipse.search.ui.text.ITextSearchResult;
import org.eclipse.search.ui.text.Match;


/**
 * @author Thomas Mäder */
public class TextSearchJob implements ISearchJob {
	private ITextSearchResult fSearch;
	private String fSearchString;
	private String fSearchOptions;
	private TextSearchScope fScope;
	private String fName;

	public TextSearchJob(ITextSearchResult search, TextSearchScope scope, String options, String searchString, String description) {
		
		fName= description;
		fSearch= search;
		fScope= scope;
		fSearchOptions= options;
		fSearchString= searchString;
	}

	public boolean canRunInBackground() {
		return true;
	}

	public IStatus run(final IProgressMonitor pm) {
		fSearch.removeAll();
		ITextSearchResultCollector collector= new ITextSearchResultCollector() {
			public IProgressMonitor getProgressMonitor() {
				return pm;
			}

			public void aboutToStart() {
				fSearch.jobStarted();
			}

			public void accept(IResourceProxy proxy, String line, int start, int length, int lineNumber) {
				IResource resource= proxy.requestResource();
				if (start < 0)
					start= 0;
				if (length < 0)
					length= 0;
				fSearch.addMatch(new Match(resource, start, length));
			}

			public void done() {
				fSearch.jobFinished();
			}
		};
		new TextSearchEngine().search(SearchPlugin.getWorkspace(), fScope, collector, new MatchLocator(fSearchString, fSearchOptions));
		return new Status(IStatus.OK, "some plugin", 0, "Dummy message", null);
	}

	public String getName() {
		return fName;
	}

}
