/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.core.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.text.Position;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.search.internal.core.ISearchScope;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.internal.ui.util.StringMatcher;
import org.eclipse.search.ui.SearchUI;

/**
 * The visitor that does the actual work.
 */
public class TextSearchVisitor extends TypedResourceVisitor {
	protected static final int fgLF= '\n';
	protected static final int fgCR= '\r';

	private String fPattern;
	private ISearchScope fScope;
	private ITextSearchResultCollector fCollector;
	private String fOptions;
		
	private IProgressMonitor fProgressMonitor;
	private StringMatcher fMatcher;
	private String fErrorMessage;
	
	protected int fPushbackChar;
	protected boolean fPushback;
	
	
	public TextSearchVisitor(String pattern, String options, ISearchScope scope, ITextSearchResultCollector collector) 
			throws CoreException {
		fPattern= pattern;
		fScope= scope;
		fCollector= collector;
		fPushback= false;
		if (options != null)
			fOptions= options;
		else
			fOptions= "";	
		

		fProgressMonitor= collector.getProgressMonitor();
		
		fMatcher= new StringMatcher(pattern, options.indexOf('i') != -1, false);
	}
	
	public void process(Collection projects) throws CoreException {
		Iterator i= projects.iterator();
		while(i.hasNext()) {
			IProject project= (IProject)i.next();
			project.accept(this);
		}
	}
	
	protected boolean visitFile(IFile file) throws CoreException {
		if (! fScope.encloses(file))
			return false;
			
		try {
			InputStream stream= file.getContents(false);
			BufferedReader reader= new BufferedReader(new InputStreamReader(stream));
			StringBuffer sb= new StringBuffer(100);
			int lineCounter= 1;
			int charCounter=0;
			boolean eof= false;
			try {
				while (!eof) {
					int eolStrLength= readLine(reader, sb);
					int lineLength= sb.length();
					int start= 0;
					eof= eolStrLength == -1;
					String line= sb.toString();
					StringMatcher.Position match;
					while (start < lineLength) {
						if ((match= fMatcher.find(line, start, lineLength)) != null) {
							start= charCounter + match.getStart();
							int length= match.getEnd() - match.getStart();
							fCollector.accept(file, line.trim(), start, length, lineCounter);
							start= match.getEnd();
						}
						else	// no match in this line
							start= lineLength;
					}
					charCounter+= lineLength + eolStrLength;
					lineCounter++;
				}
			} finally {
				reader.close();
			}
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, SearchUI.PLUGIN_ID, Platform.PLUGIN_ERROR, SearchPlugin.getResourceString("TextSearchVisitor.error") + " " + file.getFullPath(), e));
		}
		finally {
			fProgressMonitor.worked(1);
			if (fProgressMonitor.isCanceled())
				throw new OperationCanceledException(SearchPlugin.getResourceString("TextSearchVisitor.canceled"));
		}		
		return true;
	}
	
	protected int readLine(BufferedReader reader, StringBuffer sb) throws IOException {
		int ch= -1;
		sb.setLength(0);
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

