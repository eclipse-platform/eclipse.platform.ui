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
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.text.AbstractTextSearchResult;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;


public class FileSearchQuery implements ISearchQuery {
	
	private final static class TextSearchResultCollector implements ITextSearchResultCollector {
		
		private final AbstractTextSearchResult fResult;
		private final IProgressMonitor fProgressMonitor;
		
		private TextSearchResultCollector(AbstractTextSearchResult result, IProgressMonitor monitor) {
			super();
			fResult= result;
			fProgressMonitor= monitor;
		}
		public IProgressMonitor getProgressMonitor() {
			return fProgressMonitor;
		}
		public void aboutToStart() {
			// do nothing
		}
		public void accept(IResourceProxy proxy, int start, int length) {
			IFile file= (IFile) proxy.requestResource();
			if (start < 0)
				start= 0;
			if (length < 0)
				length= 0;
			fResult.addMatch(new FileMatch(file, start, length));
		}
		public void done() {
			// do nothing
		}
	}

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
		ITextSearchResultCollector collector= new TextSearchResultCollector(textResult, pm);
		return new TextSearchEngine().search(fScope, fVisitDerived, collector, new MatchLocator(fSearchString, isCaseSensitive(), isRegexSearch()), true);
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
		ITextSearchResultCollector collector= new TextSearchResultCollector(result, monitor);
		SearchScope scope= new SearchScope("", new IResource[] { file }); //$NON-NLS-1$
		return new TextSearchEngine().search(scope, fVisitDerived, collector, new MatchLocator(fSearchString, isCaseSensitive(), isRegexSearch()), true);
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
}
