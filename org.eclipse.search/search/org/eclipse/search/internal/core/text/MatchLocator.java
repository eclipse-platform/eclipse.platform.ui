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
package org.eclipse.search.internal.core.text;

import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.search.internal.ui.SearchMessages;

import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * A class finding matches withing a file.
 * @since 3.0
 */
public class MatchLocator {
	
	protected static final int fgLF= '\n';
	protected static final int fgCR= '\r';
	
	private Matcher fMatcher;
	protected int fPushbackChar;
	protected boolean fPushback;
	
	public MatchLocator(Pattern pattern) {
		fMatcher= pattern.matcher(""); //$NON-NLS-1$
	}
	
	public MatchLocator(String pattern, boolean isCaseSensitive, boolean isRegexSearch) throws PatternSyntaxException {
		this(PatternConstructor.createPattern(pattern, isCaseSensitive, isRegexSearch));		
	}
	
	public boolean isEmtpy() {
		return fMatcher.pattern().pattern().length() == 0;
	}
	
	public void locateMatches(IProgressMonitor monitor, Reader reader, ITextSearchResultCollector collector, IResourceProxy proxy) throws IOException, CoreException {
		int lineCounter= 1;
		int charCounter=0;
		boolean eof= false;
		while (!eof) {
			StringBuffer sb= new StringBuffer(200);
			int eolStrLength= readLine(reader, sb);
			int lineLength= sb.length();
			int start= 0;
			eof= eolStrLength == -1;
			String line= sb.toString();
			while (start < lineLength) {
				fMatcher.reset(line);
				if (fMatcher.find(start)) {
					start= charCounter + fMatcher.start();
					int length= fMatcher.end() - fMatcher.start();
					collector.accept(proxy, start, length);
					start= fMatcher.end();
				}
				else	// no match in this line
					start= lineLength;
			}
			charCounter+= lineLength + eolStrLength;
			lineCounter++;
			if (monitor.isCanceled())
				throw new OperationCanceledException(SearchMessages.getString("TextSearchVisitor.canceled")); //$NON-NLS-1$
		}
	}
	
	private int readLine(Reader reader, StringBuffer sb) throws IOException {
		int ch= -1;
		if (fPushback) {
			ch= fPushbackChar;
			fPushback= false;
		}
		else
			ch= reader.read();
		while (ch >= 0) {
			if (ch == fgLF)
				return 1;
			if (ch == fgCR) {
				ch= reader.read();
				if (ch == fgLF)
					return 2;
				
				fPushbackChar= ch;
				fPushback= true;
				return 1;
			}
			sb.append((char)ch);
			ch= reader.read();
		}
		return -1;
	}

	public void locateMatches(IProgressMonitor progressMonitor, CharSequence searchInput, ITextSearchResultCollector collector, IResourceProxy proxy) throws CoreException {
		fMatcher.reset(searchInput);
		int pos= 0;
		int k= 0;
		while (pos < searchInput.length() && fMatcher.find(pos)) {
			int start= fMatcher.start();
			int end= fMatcher.end();
			collector.accept(proxy, start, end - start);
			if (end == start) {
				end++;
			}
			pos= end;
			if (k++ == 20) {
				if (progressMonitor.isCanceled()) {
					throw new OperationCanceledException(SearchMessages.getString("TextSearchVisitor.canceled")); //$NON-NLS-1$
				}
				k= 0;
			}
		}
	}



}
