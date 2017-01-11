/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Juerg Billeter, juergbi@ethz.ch - 47136 Search view should show match objects
 *     Ulrich Etter, etteru@ethz.ch - 47136 Search view should show match objects
 *     Roman Fuchs, fuchsro@ethz.ch - 47136 Search view should show match objects
 *     Christian Walther (Indel AG) - Bug 399094: Add whole word option to file search
 *     Terry Parker <tparker@google.com> (Google Inc.) - Bug 441016 - Speed up text search by parallelizing it using JobGroups
 *******************************************************************************/
package org.eclipse.search.internal.ui.text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.search.core.text.TextSearchEngine;
import org.eclipse.search.core.text.TextSearchMatchAccess;
import org.eclipse.search.core.text.TextSearchRequestor;
import org.eclipse.search.internal.core.text.PatternConstructor;
import org.eclipse.search.internal.ui.Messages;
import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.FileTextSearchScope;
import org.eclipse.search.ui.text.Match;


public class FileSearchQuery implements ISearchQuery {

	private final static class TextSearchResultCollector extends TextSearchRequestor {

		private final AbstractTextSearchResult fResult;
		private final boolean fIsFileSearchOnly;
		private final boolean fSearchInBinaries;

		private final boolean fIsLightweightAutoRefresh;
		private Map<IFile, ArrayList<FileMatch>> fCachedMatches;
		private Object fLock= new Object();

		private TextSearchResultCollector(AbstractTextSearchResult result, boolean isFileSearchOnly, boolean searchInBinaries) {
			fResult= result;
			fIsFileSearchOnly= isFileSearchOnly;
			fSearchInBinaries= searchInBinaries;
			fIsLightweightAutoRefresh= Platform.getPreferencesService().getBoolean(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PREF_LIGHTWEIGHT_AUTO_REFRESH, false, null);

		}

		@Override
		public boolean canRunInParallel() {
			return true;
		}

		@Override
		public boolean acceptFile(IFile file) throws CoreException {
			if (fIsLightweightAutoRefresh && !file.exists())
				return false;

			if (fIsFileSearchOnly) {
				synchronized (fLock) {
					fResult.addMatch(new FileMatch(file));
				}
			}
			flushMatches();
			return true;
		}

		@Override
		public boolean reportBinaryFile(IFile file) {
			return fSearchInBinaries;
		}

		@Override
		public boolean acceptPatternMatch(TextSearchMatchAccess matchRequestor) throws CoreException {
			ArrayList<FileMatch> matches;
			synchronized(fLock) {
				// fCachedMatches is set to null when the caller invokes endReporting(),
				// indicating that no further results are desired/expected, so discard
				// any additional results.
				if (fCachedMatches == null) {
					return false;
				}
				matches= fCachedMatches.get(matchRequestor.getFile());
			}

			int matchOffset= matchRequestor.getMatchOffset();

			/*
			 * Another job may call flushCaches() at any time, which will clear the cached matches.
			 * Any addition of matches to the cache needs to be protected against the flushing of
			 * the cache by other jobs. It is OK to call getLineElement() with an unprotected local
			 * reference to the matches for this file, because getLineElement() uses previous matches
			 * as an optimization when creating new matches but doesn't update the cache directly
			 * (and because each file is processed by at most one job).
			 */
			LineElement lineElement= getLineElement(matchOffset, matchRequestor, matches);
			if (lineElement != null) {
				FileMatch fileMatch= new FileMatch(matchRequestor.getFile(), matchOffset, matchRequestor.getMatchLength(), lineElement);
				synchronized(fLock) {
					// fCachedMatches is set to null when the caller invokes endReporting(),
					// indicating that no further results are desired/expected, so discard
					// any additional results.
					if (fCachedMatches == null) {
						return false;
					}
					matches= fCachedMatches.get(matchRequestor.getFile());
					if (matches == null) {
						matches= new ArrayList<>();
						fCachedMatches.put(matchRequestor.getFile(), matches);
					}
					matches.add(fileMatch);
				}
			}
			return true;
		}

		private LineElement getLineElement(int offset, TextSearchMatchAccess matchRequestor, ArrayList<FileMatch> matches) {
			int lineNumber= 1;
			int lineStart= 0;

			if (matches != null) {
				// match on same line as last?
				FileMatch last= matches.get(matches.size() - 1);
				LineElement lineElement= last.getLineElement();
				if (lineElement.contains(offset)) {
					return lineElement;
				}
				// start with the offset and line information from the last match
				lineStart= lineElement.getOffset() + lineElement.getLength();
				lineNumber= lineElement.getLine() + 1;
			}
			if (offset < lineStart) {
				return null; // offset before the last line
			}

			int i= lineStart;
			int contentLength= matchRequestor.getFileContentLength();
			while (i < contentLength) {
				char ch= matchRequestor.getFileContentChar(i++);
				if (ch == '\n' || ch == '\r') {
					if (ch == '\r' && i < contentLength && matchRequestor.getFileContentChar(i) == '\n') {
						i++;
					}
					if (offset < i) {
						String lineContent= getContents(matchRequestor, lineStart, i); // include line delimiter
						return new LineElement(matchRequestor.getFile(), lineNumber, lineStart, lineContent);
					}
					lineNumber++;
					lineStart= i;
				}
			}
			if (offset < i) {
				String lineContent= getContents(matchRequestor, lineStart, i); // until end of file
				return new LineElement(matchRequestor.getFile(), lineNumber, lineStart, lineContent);
			}
			return null; // offset outside of range
		}

		private static String getContents(TextSearchMatchAccess matchRequestor, int start, int end) {
			StringBuffer buf= new StringBuffer();
			for (int i= start; i < end; i++) {
				char ch= matchRequestor.getFileContentChar(i);
				if (Character.isWhitespace(ch) || Character.isISOControl(ch)) {
					buf.append(' ');
				} else {
					buf.append(ch);
				}
			}
			return buf.toString();
		}

		@Override
		public void beginReporting() {
			fCachedMatches= new HashMap<>();
		}

		@Override
		public void endReporting() {
			flushMatches();
			synchronized (fLock) {
				fCachedMatches= null;
			}
		}

		private void flushMatches() {
			synchronized (fLock) {
				if (fCachedMatches != null && !fCachedMatches.isEmpty()) {
					Iterator<ArrayList<FileMatch>> it = fCachedMatches.values().iterator();
					while(it.hasNext()) {
						ArrayList<FileMatch> matches= it.next();
						fResult.addMatches(matches.toArray(new Match[matches.size()]));
					}
					fCachedMatches.clear();
				}
			}
		}
	}

	private final FileTextSearchScope fScope;
	private final String fSearchText;
	private final boolean fIsRegEx;
	private final boolean fIsCaseSensitive;
	private final boolean fIsWholeWord;
	private FileSearchResult fResult;
	private boolean fSearchInBinaries;


	public FileSearchQuery(String searchText, boolean isRegEx, boolean isCaseSensitive, FileTextSearchScope scope) {
		this(searchText, isRegEx, isCaseSensitive, false, false, scope);
	}

	public FileSearchQuery(String searchText, boolean isRegEx, boolean isCaseSensitive, boolean isWholeWord, boolean searchInBinaries, FileTextSearchScope scope) {
		fSearchText= searchText;
		fIsRegEx= isRegEx;
		fIsCaseSensitive= isCaseSensitive;
		fIsWholeWord= isWholeWord;
		fScope= scope;
		fSearchInBinaries= searchInBinaries;
	}

	public FileTextSearchScope getSearchScope() {
		return fScope;
	}

	@Override
	public boolean canRunInBackground() {
		return true;
	}

	@Override
	public IStatus run(final IProgressMonitor monitor) {
		AbstractTextSearchResult textResult= (AbstractTextSearchResult) getSearchResult();
		textResult.removeAll();

		Pattern searchPattern= getSearchPattern();

		TextSearchResultCollector collector= new TextSearchResultCollector(textResult, isFileNameSearch(), fSearchInBinaries);
		return TextSearchEngine.create().search(fScope, collector, searchPattern, monitor);
	}

	private boolean isScopeAllFileTypes() {
		String[] fileNamePatterns= fScope.getFileNamePatterns();
		if (fileNamePatterns == null)
			return true;
		for (int i= 0; i < fileNamePatterns.length; i++) {
			if ("*".equals(fileNamePatterns[i])) { //$NON-NLS-1$
				return true;
			}
		}
		return false;
	}


	@Override
	public String getLabel() {
		return SearchMessages.FileSearchQuery_label;
	}

	public String getSearchString() {
		return fSearchText;
	}

	public String getResultLabel(int nMatches) {
		String searchString= getSearchString();
		if (searchString.length() > 0) {
			// text search
			if (isScopeAllFileTypes()) {
				// search all file extensions
				if (nMatches == 1) {
					Object[] args= { searchString, fScope.getDescription() };
					return Messages.format(SearchMessages.FileSearchQuery_singularLabel, args);
				}
				Object[] args= { searchString, Integer.valueOf(nMatches), fScope.getDescription() };
				return Messages.format(SearchMessages.FileSearchQuery_pluralPattern, args);
			}
			// search selected file extensions
			if (nMatches == 1) {
				Object[] args= { searchString, fScope.getDescription(), fScope.getFilterDescription() };
				return Messages.format(SearchMessages.FileSearchQuery_singularPatternWithFileExt, args);
			}
			Object[] args= { searchString, Integer.valueOf(nMatches), fScope.getDescription(), fScope.getFilterDescription() };
			return Messages.format(SearchMessages.FileSearchQuery_pluralPatternWithFileExt, args);
		}
		// file search
		if (nMatches == 1) {
			Object[] args= { fScope.getFilterDescription(), fScope.getDescription() };
			return Messages.format(SearchMessages.FileSearchQuery_singularLabel_fileNameSearch, args);
		}
		Object[] args= { fScope.getFilterDescription(), Integer.valueOf(nMatches), fScope.getDescription() };
		return Messages.format(SearchMessages.FileSearchQuery_pluralPattern_fileNameSearch, args);
	}

	/**
	 * @param result all result are added to this search result
	 * @param monitor the progress monitor to use
	 * @param file the file to search in
	 * @return returns the status of the operation
	 */
	public IStatus searchInFile(final AbstractTextSearchResult result, final IProgressMonitor monitor, IFile file) {
		FileTextSearchScope scope= FileTextSearchScope.newSearchScope(new IResource[] { file }, new String[] { "*" }, true); //$NON-NLS-1$

		Pattern searchPattern= getSearchPattern();
		TextSearchResultCollector collector= new TextSearchResultCollector(result, isFileNameSearch(), fSearchInBinaries);

		return TextSearchEngine.create().search(scope, collector, searchPattern, monitor);
	}

	protected Pattern getSearchPattern() {
		return PatternConstructor.createPattern(fSearchText, fIsRegEx, true, fIsCaseSensitive, fIsWholeWord);
	}

	public boolean isFileNameSearch() {
		return fSearchText.length() == 0;
	}

	public boolean isRegexSearch() {
		return fIsRegEx;
	}

	public boolean isCaseSensitive() {
		return fIsCaseSensitive;
	}

	public boolean isWholeWord() {
		return fIsWholeWord;
	}

	@Override
	public boolean canRerun() {
		return true;
	}

	@Override
	public ISearchResult getSearchResult() {
		if (fResult == null) {
			fResult= new FileSearchResult(this);
			new SearchResultUpdater(fResult);
		}
		return fResult;
	}
}
