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
package org.eclipse.search.internal.ui.text;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;

import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.text.AbstractTextSearchResult;

import org.eclipse.search.internal.core.SearchScope;
import org.eclipse.search.internal.core.text.ITextSearchResultCollector;
import org.eclipse.search.internal.core.text.MatchLocator;
import org.eclipse.search.internal.core.text.TextSearchEngine;
import org.eclipse.search.internal.ui.Messages;
import org.eclipse.search.internal.ui.SearchMessages;


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
	private SearchScope fScope;
	private FileSearchResult fResult;
	private boolean fVisitDerived;

	public FileSearchQuery(SearchScope scope, String options, String searchString, boolean visitDerived) {
		fVisitDerived= visitDerived;
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
		ITextSearchResultCollector collector= new TextSearchResultCollector(textResult, pm);
		String searchString= fSearchString;
		if (searchString.trim().equals(String.valueOf('*'))) {
			searchString= new String();
		}
		return new TextSearchEngine().search(fScope, fVisitDerived, collector, new MatchLocator(searchString, isCaseSensitive(), isRegexSearch()));
	}

	public String getLabel() {
		return SearchMessages.FileSearchQuery_label; 
	}
	
	public String getSearchString() {
		return fSearchString;
	}
	
	private String getSearchOptions() {
		return fSearchOptions;
	}

	public String getResultLabel(int nMatches) {
		if (nMatches == 1) {
			if (fSearchString.length() > 0) {
				Object[] args= { fSearchString, fScope.getDescription() };
				return Messages.format(SearchMessages.FileSearchQuery_singularLabel, args); 
			}
			Object[] args= { fScope.getFileNamePatternDescription(), fScope.getDescription() };
			return Messages.format(SearchMessages.FileSearchQuery_singularLabel_fileNameSearch, args); 
		}
		if (fSearchString.length() > 0) {
			Object[] args= { fSearchString, new Integer(nMatches), fScope.getDescription() };
			return Messages.format(SearchMessages.FileSearchQuery_pluralPattern, args); 
		}
		Object[] args= { fScope.getFileNamePatternDescription(), new Integer(nMatches), fScope.getDescription() };
		return Messages.format(SearchMessages.FileSearchQuery_pluralPattern_fileNameSearch, args); 
	}

	/**
	 * @param result all result are added to this search result
	 * @param monitor the progress monitor to use
	 * @param file the file to search in
	 * @return returns the status of the operation
	 */
	public IStatus searchInFile(final AbstractTextSearchResult result, final IProgressMonitor monitor, IFile file) {
		ITextSearchResultCollector collector= new TextSearchResultCollector(result, monitor);
		SearchScope scope= SearchScope.newSearchScope("", new IResource[] { file }); //$NON-NLS-1$
		return new TextSearchEngine().search(scope, fVisitDerived, collector, new MatchLocator(fSearchString, isCaseSensitive(), isRegexSearch()));
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
