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

package org.eclipse.search.tests.filesearch;

import org.eclipse.search.internal.core.text.ITextSearchResultCollector;
import org.eclipse.search.internal.core.text.MatchLocator;
import org.eclipse.search.internal.core.text.TextSearchEngine;
import org.eclipse.search.internal.core.text.TextSearchScope;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.internal.ui.text.FileSearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.Match;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/**
 */
public class LineBasedFileSearch extends FileSearchQuery  {
	
	public static class LineBasedMatch extends Match {
		private long fCreationTimeStamp;
		
		public LineBasedMatch(IFile element, int offset, int length) {
			super(element, Match.UNIT_LINE, offset, length);
			fCreationTimeStamp= element.getModificationStamp();
		}
		
		public IFile getFile() {
			return (IFile) getElement();
		}

		public long getCreationTimeStamp() {
			return fCreationTimeStamp;
		}
		
		
	}
	
	private static class LineBasedTextSearchResultCollector implements ITextSearchResultCollector {
		
		private final AbstractTextSearchResult fResult;
		private final IProgressMonitor fProgressMonitor;
		private IFile fLastFile;
		private IDocument fLastDocument;	
		
		private LineBasedTextSearchResultCollector(AbstractTextSearchResult result, IProgressMonitor monitor) {
			fResult= result;
			fProgressMonitor= monitor;
			fLastFile= null;
			fLastDocument= null;
		}
		public IProgressMonitor getProgressMonitor() {
			return fProgressMonitor;
		}
		public void aboutToStart() {
			// do nothing
		}
		public void accept(IResourceProxy proxy, int start, int length) throws CoreException {
			IFile file= (IFile) proxy.requestResource();
			try {
				IDocument doc= getDocument(file);
				if (doc == null) {
					throw new IllegalArgumentException("No document for file: " + file.getName());
				}

				int startLine= doc.getLineOfOffset(start);
				int endLine= doc.getLineOfOffset(start + length);
				fResult.addMatch(new LineBasedMatch(file, startLine, endLine - startLine + 1));
			} catch (BadLocationException e) {
				throw new CoreException(new Status(IStatus.ERROR, SearchPlugin.getID(), IStatus.ERROR, "bad location", e));
			}
		}

		private IDocument getDocument(IFile file) throws CoreException {
			if (file.equals(fLastFile)) {
				return fLastDocument;
			}
			if (fLastFile != null) {
				FileBuffers.getTextFileBufferManager().disconnect(fLastFile.getFullPath(), null);
			}
			fLastFile= file;
			
			FileBuffers.getTextFileBufferManager().connect(file.getFullPath(), null);
			fLastDocument= FileBuffers.getTextFileBufferManager().getTextFileBuffer(file.getFullPath()).getDocument();
			return fLastDocument;
		}
		
		public void done() throws CoreException {
			if (fLastFile != null) {
				FileBuffers.getTextFileBufferManager().disconnect(fLastFile.getFullPath(), null);
			}
		}
	}
	
	private final TextSearchScope fScope;
	
	
	public LineBasedFileSearch(TextSearchScope scope, String options, String searchString) {
		super(scope, options, searchString);
		fScope= scope;
	}


	public IStatus run(final IProgressMonitor pm) {
		final AbstractTextSearchResult textResult= (AbstractTextSearchResult) getSearchResult();
		textResult.removeAll();
		ITextSearchResultCollector collector= new LineBasedTextSearchResultCollector(textResult, pm);
		return new TextSearchEngine().search(fScope, false, collector, new MatchLocator(getSearchString(), isCaseSensitive(), isRegexSearch()), true);
	}
	

}