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
package org.eclipse.search.internal.ui.text;

import java.text.MessageFormat;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.search.internal.core.SearchScope;
import org.eclipse.search.internal.core.text.ITextSearchResultCollector;
import org.eclipse.search.internal.core.text.MatchLocator;
import org.eclipse.search.internal.core.text.TextSearchEngine;
import org.eclipse.search.internal.core.text.TextSearchScope;
import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.text.AbstractTextSearchResult;


public class FileSearchQuery implements ISearchQuery {
	private String fSearchString;
	private String fSearchOptions;
	private TextSearchScope fScope;
	private FileSearchResult fResult;

	public FileSearchQuery(TextSearchScope scope, String options, String searchString) {
		
		fScope= scope;
		fSearchOptions= options;
		fSearchString= searchString;
	}

	public boolean canRunInBackground() {
		return true;
	}

	public IStatus run(final IProgressMonitor pm) {
		final AbstractTextSearchResult textResult= (AbstractTextSearchResult) getSearchResult();
		textResult.removeAll();
		ITextSearchResultCollector collector= new ITextSearchResultCollector() {
			public IProgressMonitor getProgressMonitor() {
				return pm;
			}

			public void aboutToStart() {
				// do nothing
			}

			public void accept(IResourceProxy proxy, String line, int start, int length, int lineNumber) {
				IResource resource= proxy.requestResource();
				if (start < 0)
					start= 0;
				if (length < 0)
					length= 0;
				textResult.addMatch(new FileMatch((IFile) resource, start, length));
			}

			public void done() {
				// do nothing
			}
		};
		return new TextSearchEngine().search(SearchPlugin.getWorkspace(), fScope, collector, new MatchLocator(fSearchString, isCaseSensitive(), isRegexSearch()));
	}

	public String getLabel() {
		String format= SearchMessages.getString("FileSearchQuery.label"); //$NON-NLS-1$
		return MessageFormat.format(format, new String[] { fSearchString });
	}
	
	public String getSearchString() {
		return fSearchString;
	}
	
	private String getSearchOptions() {
		return fSearchOptions;
	}

	String getSingularLabel() {
		String[] args= new String[] { fSearchString, fScope.getDescription() };
		String format= "\"{0}\" - 1 match in {1}"; //$NON-NLS-1$
		return MessageFormat.format(format, args);
	}
	
	String getPluralPattern() {
		String[] args= new String[] { fSearchString, "{0}", fScope.getDescription() }; //$NON-NLS-1$
		String format= "\"{0}\" - {1} matches in {2}"; //$NON-NLS-1$
		return MessageFormat.format(format, args);
	}

	/**
	 * @return
	 */
	public IStatus searchInFile(final AbstractTextSearchResult result, final IProgressMonitor monitor, IFile file) {
		ITextSearchResultCollector collector= new ITextSearchResultCollector() {
			public IProgressMonitor getProgressMonitor() {
				return monitor;
			}

			public void aboutToStart() {
				// do nothing
			}

			public void accept(IResourceProxy proxy, String line, int start, int length, int lineNumber) {
				IResource resource= proxy.requestResource();
				if (start < 0)
					start= 0;
				if (length < 0)
					length= 0;
				result.addMatch(new FileMatch((IFile) resource, start, length));
			}

			public void done() {
				// do nothing
			}
		};
		SearchScope scope= new SearchScope("", new IResource[] { file }); //$NON-NLS-1$
		new TextSearchEngine().search(SearchPlugin.getWorkspace(), scope, collector, new MatchLocator(fSearchString, isCaseSensitive(), isRegexSearch()));
		return Status.OK_STATUS; //$NON-NLS-1$
	}
	
	public boolean isRegexSearch() {
		return isRegexSearch(getSearchOptions());
	}
	
	static boolean isRegexSearch(String options) {
		return options.indexOf('r') != -1;
	}

	public boolean isCaseSensitive() {
		return isCaseSensitive(getSearchOptions());
	}

	static boolean isCaseSensitive(String options) {
		return options.indexOf('i') == -1;
	}

	public boolean canRerun() {
		return true;
	}

	public ISearchResult getSearchResult() {
		if (fResult == null) {
			fResult= new FileSearchResult(this);
			new SearchResultUpdater(fResult);
		}
		return fResult;
	}
}
