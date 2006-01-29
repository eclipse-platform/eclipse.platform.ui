/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Markus Schorn - initial API and implementation 
 *******************************************************************************/

package org.eclipse.search2.internal.ui.text2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import org.eclipse.ui.IWorkbenchPage;

import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;

import org.eclipse.search.core.text.AbstractTextFileScanner;
import org.eclipse.search.core.text.TextSearchEngine;
import org.eclipse.search.core.text.TextSearchMatchAccess;
import org.eclipse.search.core.text.TextSearchRequestor;
import org.eclipse.search.core.text.TextSearchScope;
import org.eclipse.search.core.text.AbstractTextFileScanner.LineInformation;

import org.eclipse.search.internal.core.text.PatternConstructor;
import org.eclipse.search.internal.ui.text.SearchResultUpdater;

import org.eclipse.search2.internal.ui.SearchMessages;

public class RetrieverQuery implements ISearchQuery, IRetrieverKeys {
	private String fSearchPattern= ""; //$NON-NLS-1$
	private boolean fIsCaseSensitive;
	private boolean fIsRegularExpression;
	private boolean fIsWholeWord;
	private boolean fConsiderDerivedResources;
	private boolean fUseCaseSensitiveFilePatterns;
	private IScopeDescription fScope;
	private String[] fFilePatterns;
	private RetrieverLine fCurrentLine;
	private int fCurrentLineOffset;
	private RetrieverResult fResult;
	private ArrayList fLinesOfCurrentFile;
	private IWorkbenchPage fWorkbenchPage;
	private AbstractTextFileScanner fScanner;
	private Comparator fSearchOrder;


	public RetrieverQuery(IWorkbenchPage page) {
		fWorkbenchPage= page;
	}

	public void setSearchString(String searchFor) {
		fSearchPattern= searchFor == null ? "" : searchFor; //$NON-NLS-1$
	}
	public void setIsCaseSensitive(boolean isCaseSensitive) {
		fIsCaseSensitive= isCaseSensitive;
	}
	public void setIsRegularExpression(boolean isRegularExpression) {
		fIsRegularExpression= isRegularExpression;
	}
	public void setIsWholeWord(boolean isWholeWord) {
		fIsWholeWord= isWholeWord;
	}

	public void setSearchScope(IScopeDescription scope) {
		fScope= scope;
	}

	public void setSearchScope(IScopeDescription scope, boolean considerDerived) {
		fScope= scope;
		fConsiderDerivedResources= considerDerived;
	}

	public void setFilePatterns(String filePatterns, boolean caseSensitive) {
		fFilePatterns= filePatterns.split(FilePatternSelectionDialog.FILE_PATTERN_SEPERATOR);
		fUseCaseSensitiveFilePatterns= caseSensitive;
	}

	public void setFilePatterns(String[] fileNamePatterns) {
		fFilePatterns= (String[]) fileNamePatterns.clone();
	}

	public String getLabel() {
		return SearchMessages.RetrieverQuery_label;
	}

	public boolean canRerun() {
		return true;
	}

	public boolean canRunInBackground() {
		return true;
	}

	public ISearchResult getSearchResult() {
		if (fResult == null) {
			fResult= new RetrieverResult(this);
			new SearchResultUpdater(fResult);
		}
		return fResult;
	}

	public IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
		if (fSearchPattern.length() == 0) {
			return Status.OK_STATUS;
		}
		if (fResult != null) {
			fResult.removeAll();
			fResult.setComplete(false);
		}

		TextSearchScope scope= createSearchScope(fWorkbenchPage);
		return performSearch(monitor, scope);
	}

	private IStatus performSearch(IProgressMonitor monitor, TextSearchScope scope) {
		TextSearchEngine engine= TextSearchEngine.create();
		engine.setSearchOrderHint(fSearchOrder);
		TextSearchRequestor requestor= createRequestor();
		Pattern searchPattern= createSearchPattern();

		if (fResult != null) {
			fResult.setComplete(false);
		}

		IStatus status= engine.search(scope, requestor, searchPattern, monitor);
		return status;
	}

	Pattern createSearchPattern() {
		return PatternConstructor.createPattern(fSearchPattern, fIsRegularExpression, false, fIsCaseSensitive, fIsWholeWord);
	}

	private TextSearchScope createSearchScope(IWorkbenchPage page) {
		RetrieverSearchScope scope= new RetrieverSearchScope(fScope.getRoots(page), fFilePatterns, fUseCaseSensitiveFilePatterns);
		scope.setVisitDerived(fConsiderDerivedResources);
		return scope;
	}

	private TextSearchRequestor createRequestor() {
		return new TextSearchRequestor() {
			public boolean acceptFile(IFile file) throws CoreException {
				onAcceptFile(file);
				return true;
			}
			public boolean acceptPatternMatch(TextSearchMatchAccess matchAccess) throws CoreException {
				onAcceptPatternMatch(matchAccess);
				return true;
			}
			public void beginReporting() {
				onBeginReporting();
			}
			public void endReporting() {
				onEndReporting();
			}
		};
	}

	protected void onBeginReporting() {
		fLinesOfCurrentFile= new ArrayList();
		getSearchResult();
		fScanner= null;
		fCurrentLine= null;
	}

	protected void onAcceptFile(IFile file) {
		flushMatches();
		if (fScanner != null) {
			fScanner.reset();
		}
		fScanner= null;
		fCurrentLine= null;
	}

	protected void onAcceptPatternMatch(TextSearchMatchAccess matchAccess) {
		if (fScanner == null) {
			fScanner= selectScanner(matchAccess);
		}
		int offset= matchAccess.getMatchOffset();
		int length= matchAccess.getMatchLength();
		int kind= fScanner.getLocationKind(matchAccess);
		int shiftedkind= 1;
		if (kind >= 0 && kind < 32) {
			shiftedkind= 1 << kind;
		}
		setupCurrentLine(matchAccess);
		int column= offset - fCurrentLineOffset;
		int endcolumn= column + length;
		String original= null;
		if (fCurrentLine.getLength() >= endcolumn) {
			original= fCurrentLine.substring(column, endcolumn);
		} else {
			StringBuffer match= new StringBuffer(length);
			int end= offset + length;
			for (int i= offset; i < end; i++) {
				match.append(matchAccess.getFileContentChar(i));
			}
			original= match.toString();
		}
		RetrieverMatch match= new RetrieverMatch(fCurrentLine, original, offset, length, column, shiftedkind);
		fCurrentLine.addMatch(match);
	}

	protected void onEndReporting() {
		fResult.setComplete(true);
		flushMatches();
		if (fScanner != null) {
			fScanner.reset();
		}
		fScanner= null;
		fCurrentLine= null;
		fLinesOfCurrentFile= null;
	}

	private void setupCurrentLine(TextSearchMatchAccess match) {
		LineInformation info= fScanner.getLineInformation(match);
		int lineNumber= info.getLineNumber();
		if (fCurrentLine == null || fCurrentLine.getLineNumber() != lineNumber) {
			fCurrentLineOffset= info.getLineOffset();
			final int lineLength= Math.min(info.getLineLength(), MAX_COMBINED_LINE_LENGTH);
			final int lineEndOffset= fCurrentLineOffset + lineLength;
			final int matchEndOffset= match.getMatchOffset() + match.getMatchLength();

			StringBuffer lineData= new StringBuffer(lineLength);
			for (int i= fCurrentLineOffset; i < lineEndOffset; i++) {
				lineData.append(match.getFileContentChar(i));
			}

			// check the last two characters separately
			if (lineEndOffset > matchEndOffset && lineLength > 0) {
				char last= lineData.charAt(lineLength - 1);
				if (last == '\n') {
					if (lineEndOffset > matchEndOffset + 1 && lineLength > 1 && lineData.charAt(lineLength - 2) == '\r') {
						lineData.setLength(lineLength - 2);
					} else {
						lineData.setLength(lineLength - 1);
					}
				} else
					if (last == '\r') {
						lineData.setLength(lineLength - 1);
					}
			}

			fCurrentLine= new RetrieverLine(match.getFile(), lineNumber);
			fCurrentLine.setData(lineData.toString());
			fLinesOfCurrentFile.add(fCurrentLine);
		}
	}

	private void flushMatches() {
		if (!fLinesOfCurrentFile.isEmpty()) {
			fResult.setLinesForFile(fCurrentLine.getParent(), fLinesOfCurrentFile);
			fLinesOfCurrentFile.clear();
			fCurrentLine= null;
		}
	}

	private AbstractTextFileScanner selectScanner(TextSearchMatchAccess matchAccess) {
		AbstractTextFileScanner scanner= TextFileScannerRegistry.getInstance().findScanner(matchAccess.getFile());
		if (scanner == null) {
			scanner= TextFileScannerRegistry.getInstance().getLineNumberScanner();
		}
		return scanner;
	}


	public IScopeDescription getScopeDescription() {
		return fScope;
	}

	public boolean isCaseSensitive() {
		return fIsCaseSensitive;
	}

	public boolean isRegularExpression() {
		return fIsRegularExpression;
	}

	public boolean isWholeWord() {
		return fIsWholeWord;
	}

	public String getSearchText() {
		return fSearchPattern;
	}

	public boolean getUseCaseSensitiveFilePatterns() {
		return fUseCaseSensitiveFilePatterns;
	}

	public String getFilePatterns() {
		StringBuffer buffer= new StringBuffer();
		for (int i= 0; i < fFilePatterns.length; i++) {
			if (i > 0) {
				buffer.append(FilePatternSelectionDialog.FILE_PATTERN_SEPERATOR);
			}
			buffer.append(fFilePatterns[i]);
		}

		return buffer.toString();
	}

	public boolean getConsiderDerivedResources() {
		return fConsiderDerivedResources;
	}

	public void searchAgain(Collection outdated, IProgressMonitor monitor) {
		TextSearchScope scope= new RetrieverSearchScope((IResource[]) outdated.toArray(new IResource[outdated.size()]), new String[] {"*"}, true); //$NON-NLS-1$
		performSearch(monitor, scope);
	}

	public void setSearchOrder(Comparator searchOrder) {
		fSearchOrder= searchOrder;
	}
}
