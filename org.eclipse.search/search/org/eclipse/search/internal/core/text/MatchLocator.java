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
import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import org.eclipse.search.internal.ui.SearchMessages;

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
	private String fPattern;
	
	public MatchLocator(String pattern, boolean isCaseSensitive, boolean isRegexSearch) throws PatternSyntaxException {
		fPattern= pattern;
		Pattern regExPattern;
		
		if (!isRegexSearch)
			pattern= asRegEx(pattern);
		
		if (!isCaseSensitive)
			regExPattern= Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
		else
			regExPattern= Pattern.compile(pattern);

		fMatcher= regExPattern.matcher(""); //$NON-NLS-1$
		
	}
	

	public boolean isEmtpy() {
		return getPattern().length() == 0;
	}
	
	public String getPattern() {
		return fPattern;
	}
	
	public void locateMatches(IProgressMonitor monitor, Reader reader, IMatchCollector collector) throws IOException, InvocationTargetException {
		int lineCounter= 1;
		int charCounter=0;
		boolean eof= false;
		try {
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
						collector.accept(line.trim(), start, length, lineCounter);
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
		} finally {
			if (reader != null)
				reader.close();
		}		
	}
	
	/*
	 * Converts '*' and '?' to regEx variables.
	 */
	private String asRegEx(String pattern) {
		
		StringBuffer out= new StringBuffer(pattern.length());
		
		boolean escaped= false;
		boolean quoting= false;
		
		int i= 0;
		while (i < pattern.length()) {
			char ch= pattern.charAt(i++);
			
			if (ch == '*' && !escaped) {
				if (quoting) {
					out.append("\\E"); //$NON-NLS-1$
					quoting= false;
				}
				out.append(".*"); //$NON-NLS-1$
				escaped= false;
				continue;
			} else if (ch == '?' && !escaped) {
				if (quoting) {
					out.append("\\E"); //$NON-NLS-1$
					quoting= false;
				}
				out.append("."); //$NON-NLS-1$
				escaped= false;
				continue;
			} else if (ch == '\\' && !escaped) {
				escaped= true;
				continue;								
				
			} else if (ch == '\\' && escaped) {
				escaped= false;
				if (quoting) {
					out.append("\\E"); //$NON-NLS-1$
					quoting= false;
				}
				out.append("\\\\"); //$NON-NLS-1$
				continue;								
			}
			
			if (!quoting) {
				out.append("\\Q"); //$NON-NLS-1$
				quoting= true;
			}
			if (escaped && ch != '*' && ch != '?' && ch != '\\')
				out.append('\\');
			out.append(ch);
			escaped= ch == '\\';
			
		}
		if (quoting)
			out.append("\\E"); //$NON-NLS-1$
		
		return out.toString();
	}

	protected int readLine(Reader reader, StringBuffer sb) throws IOException {
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
				else {
					fPushbackChar= ch;
					fPushback= true;
					return 1;
				}
			}
			sb.append((char)ch);
			ch= reader.read();
		}
		return -1;
	}	
}
