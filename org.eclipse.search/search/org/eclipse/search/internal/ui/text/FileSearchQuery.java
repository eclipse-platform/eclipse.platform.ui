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
package org.eclipse.search.internal.ui.text;

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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;


public class FileSearchQuery implements ISearchQuery {
	private String fSearchString;
	private String fSearchOptions;
	private TextSearchScope fScope;
	private FileSearchResult fResult;
	private boolean fVisitDerived;

	public FileSearchQuery(TextSearchScope scope, String options, String searchString, boolean visitDerived) {
		fVisitDerived= visitDerived;
		fScope= scope;
		fSearchOptions= options;
		fSearchString= searchString;
	}

	public FileSearchQuery(TextSearchScope scope, String options, String searchString) {
		this(scope, options, searchString, false);
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
				textResult.addMatch(createMatch((IFile)resource, start, length, lineNumber));
			}
	
			public void done() {
				// do nothing
			}
		};
		return new TextSearchEngine().search(SearchPlugin.getWorkspace(), fScope, fVisitDerived, collector, new MatchLocator(fSearchString, isCaseSensitive(), isRegexSearch()));
	}

	public String getLabel() {
		return SearchMessages.getString("FileSearchQuery.label"); //$NON-NLS-1$
	}
	
	public String getSearchString() {
		return fSearchString;
	}
	
	private String getSearchOptions() {
		return fSearchOptions;
	}

	String getSingularLabel() {
		String[] args= new String[] { fSearchString, fScope.getDescription() };
		return SearchMessages.getFormattedString("FileSearchQuery.singularLabel", args); //$NON-NLS-1$;
	}
	
	String getPluralPattern() {
		String[] args= new String[] { quote(fSearchString), "{0}", fScope.getDescription() }; //$NON-NLS-1$
		return SearchMessages.getFormattedString("FileSearchQuery.pluralPattern", args); //$NON-NLS-1$;
	}

	public static String quote(String searchString) {
		searchString= searchString.replaceAll("\\'", "''"); //$NON-NLS-1$ //$NON-NLS-2$
		return searchString.replaceAll("\\{", "'{'"); //$NON-NLS-1$ //$NON-NLS-2$
		
	}

	/**
	 * @param result all result are added to this search result
	 * @param monitor the progress monitor to use
	 * @param file the file to search in
	 * @return returns the status of the operation
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
		return new TextSearchEngine().search(SearchPlugin.getWorkspace(), scope, fVisitDerived, collector, new MatchLocator(fSearchString, isCaseSensitive(), isRegexSearch()));
	}
	
	public boolean isRegexSearch() {
		return isRegexSearch(getSearchOptions());
	}
	
	public static boolean isRegexSearch(String options) {
		return options.indexOf('r') != -1;
	}

	public boolean isCaseSensitive() {
		return isCaseSensitive(getSearchOptions());
	}

	public static boolean isCaseSensitive(String options) {
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

	protected FileMatch createMatch(IFile file, int start, int length, int lineNumber) {
		return new FileMatch(file, start, length);
	}
}
