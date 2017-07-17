/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Terry Parker <tparker@google.com> (Google Inc.) - Bug 441016 - Speed up text search by parallelizing it using JobGroups
 *******************************************************************************/

package org.eclipse.search.tests.filesearch;

import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.resources.IFile;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.LocationKind;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import org.eclipse.search.core.text.TextSearchEngine;
import org.eclipse.search.core.text.TextSearchMatchAccess;
import org.eclipse.search.core.text.TextSearchRequestor;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.internal.ui.text.FileMatch;
import org.eclipse.search.internal.ui.text.FileSearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.FileTextSearchScope;

/**
 */
public class LineBasedFileSearch extends FileSearchQuery  {


	private static class LineBasedTextSearchResultCollector extends TextSearchRequestor {

		private final AbstractTextSearchResult fResult;
		private IFile fLastFile;
		private IDocument fLastDocument;
		private Object fLock= new Object();

		private LineBasedTextSearchResultCollector(AbstractTextSearchResult result) {
			fResult= result;
			fLastFile= null;
			fLastDocument= null;
		}

		@Override
		public boolean canRunInParallel() {
			return true;
		}

		@Override
		public boolean acceptPatternMatch(TextSearchMatchAccess matchRequestor) throws CoreException {
			IFile file= matchRequestor.getFile();
			try {
				IDocument doc= getDocument(file);
				if (doc == null) {
					throw new IllegalArgumentException("No document for file: " + file.getName());
				}

				int startLine= doc.getLineOfOffset(matchRequestor.getMatchOffset());
				int endLine= doc.getLineOfOffset(matchRequestor.getMatchOffset() + matchRequestor.getMatchLength());
				synchronized(fLock) {
					fResult.addMatch(new FileMatch(file, startLine, endLine - startLine + 1, null));
				}
			} catch (BadLocationException e) {
				throw new CoreException(new Status(IStatus.ERROR, SearchPlugin.getID(), IStatus.ERROR, "bad location", e));
			}
			return true;
		}

		private IDocument getDocument(IFile file) throws CoreException {
			if (file.equals(fLastFile)) {
				return fLastDocument;
			}
			if (fLastFile != null) {
				FileBuffers.getTextFileBufferManager().disconnect(fLastFile.getFullPath(), LocationKind.IFILE, null);
			}
			fLastFile= file;

			FileBuffers.getTextFileBufferManager().connect(file.getFullPath(), LocationKind.IFILE, null);
			fLastDocument= FileBuffers.getTextFileBufferManager().getTextFileBuffer(file.getFullPath(), LocationKind.IFILE).getDocument();
			return fLastDocument;
		}

		@Override
		public void endReporting() {
			if (fLastFile != null) {
				try {
					FileBuffers.getTextFileBufferManager().disconnect(fLastFile.getFullPath(), LocationKind.IFILE, null);
				} catch (CoreException e) {
				}
			}
		}
	}

	private final FileTextSearchScope fScope;


	public LineBasedFileSearch(FileTextSearchScope scope, boolean isRegEx, boolean isCaseSensitive, String searchString) {
		super(searchString, isRegEx, isCaseSensitive, scope);
		fScope= scope;
	}


	@Override
	public IStatus run(IProgressMonitor monitor) {
		AbstractTextSearchResult textResult= (AbstractTextSearchResult) getSearchResult();
		textResult.removeAll();

		LineBasedTextSearchResultCollector collector= new LineBasedTextSearchResultCollector(textResult);

		Pattern searchPattern= getSearchPattern();
		return TextSearchEngine.create().search(fScope, collector, searchPattern, monitor);
	}


}
