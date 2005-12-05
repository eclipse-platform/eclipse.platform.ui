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

package org.eclipse.search.tests.filesearch;

import java.util.regex.Pattern;

import org.eclipse.core.filebuffers.FileBuffers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.resources.IFile;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.Match;

import org.eclipse.search.internal.core.text.FileNamePatternSearchScope;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.internal.ui.text.FileSearchQuery;

import org.eclipse.search.core.text.TextSearchEngine;
import org.eclipse.search.core.text.TextSearchMatchAccess;
import org.eclipse.search.core.text.TextSearchRequestor;

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
	
	private static class LineBasedTextSearchResultCollector extends TextSearchRequestor {
		
		private final AbstractTextSearchResult fResult;
		private IFile fLastFile;
		private IDocument fLastDocument;	
		
		private LineBasedTextSearchResultCollector(AbstractTextSearchResult result) {
			fResult= result;
			fLastFile= null;
			fLastDocument= null;
		}


		/* (non-Javadoc)
		 * @see org.eclipse.search.core.text.FileSearchRequestor#acceptPatternMatch(org.eclipse.search.core.text.FileSearchMatchRequestor)
		 */
		public boolean acceptPatternMatch(TextSearchMatchAccess matchRequestor) throws CoreException {
			IFile file= matchRequestor.getFile();
			try {
				IDocument doc= getDocument(file);
				if (doc == null) {
					throw new IllegalArgumentException("No document for file: " + file.getName());
				}

				int startLine= doc.getLineOfOffset(matchRequestor.getMatchOffset());
				int endLine= doc.getLineOfOffset(matchRequestor.getMatchOffset() + matchRequestor.getMatchLength());
				fResult.addMatch(new LineBasedMatch(file, startLine, endLine - startLine + 1));
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
				FileBuffers.getTextFileBufferManager().disconnect(fLastFile.getFullPath(), null);
			}
			fLastFile= file;
			
			FileBuffers.getTextFileBufferManager().connect(file.getFullPath(), null);
			fLastDocument= FileBuffers.getTextFileBufferManager().getTextFileBuffer(file.getFullPath()).getDocument();
			return fLastDocument;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.search.core.text.FileSearchRequestor#endReporting()
		 */
		public void endReporting() {
			if (fLastFile != null) {
				try {
					FileBuffers.getTextFileBufferManager().disconnect(fLastFile.getFullPath(), null);
				} catch (CoreException e) {
				}
			}
		}
	}
	
	private final FileNamePatternSearchScope fScope;
	
	
	public LineBasedFileSearch(FileNamePatternSearchScope scope, String options, String searchString) {
		super(scope, options, searchString);
		fScope= scope;
	}


	public IStatus run(IProgressMonitor monitor) {
		AbstractTextSearchResult textResult= (AbstractTextSearchResult) getSearchResult();
		textResult.removeAll();
		
		LineBasedTextSearchResultCollector collector= new LineBasedTextSearchResultCollector(textResult);
		
		Pattern searchPattern= getSearchPattern();
		return TextSearchEngine.create().search(fScope, collector, searchPattern, monitor);
	}
	

}
