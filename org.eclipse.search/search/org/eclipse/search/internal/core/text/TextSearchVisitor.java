/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.core.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.search.ui.SearchUI;

import org.eclipse.search.internal.core.ISearchScope;
import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.internal.ui.util.StringMatcher;

/**
 * The visitor that does the actual work.
 */
public class TextSearchVisitor extends TypedResourceVisitor {
	protected static final int fgLF= '\n';
	protected static final int fgCR= '\r';

	private String fPattern;
	private ISearchScope fScope;
	private ITextSearchResultCollector fCollector;
	private IEditorPart[] fEditors;
		
	private IProgressMonitor fProgressMonitor;
	private StringMatcher fMatcher;
	private Integer[] fMessageFormatArgs;

	private int fNumberOfScannedFiles;
	private int fNumberOfFilesToScan;
	private long fLastUpdateTime;
	
	protected int fPushbackChar;
	protected boolean fPushback;
	
	
	public TextSearchVisitor(String pattern, String options, ISearchScope scope, ITextSearchResultCollector collector, MultiStatus status, int fileCount) {
		super(status);
		fPattern= pattern;
		fScope= scope;
		fCollector= collector;
		fPushback= false;

		fProgressMonitor= collector.getProgressMonitor();
		fMatcher= new StringMatcher(pattern, options.indexOf('i') != -1, false);
		fNumberOfScannedFiles= 0;
		fNumberOfFilesToScan= fileCount;
		fMessageFormatArgs= new Integer[] { new Integer(0), new Integer(fileCount) };
	}
	
	public void process(Collection projects) {
		Iterator i= projects.iterator();
		while(i.hasNext()) {
			IProject project= (IProject)i.next();
			try {
				project.accept(this, IResource.NONE);
			} catch (CoreException ex) {
				addToStatus(ex);
			}
		}
	}

	/**
	 * Returns an array of all editors that have an unsaved content. If the identical content is 
	 * presented in more than one editor, only one of those editor parts is part of the result.
	 * 
	 * @return an array of all editor parts.
	 */
	public static IEditorPart[] getEditors() {
		Set inputs= new HashSet();
		List result= new ArrayList(0);
		IWorkbench workbench= SearchPlugin.getDefault().getWorkbench();
		IWorkbenchWindow[] windows= workbench.getWorkbenchWindows();
		for (int i= 0; i < windows.length; i++) {
			IWorkbenchPage[] pages= windows[i].getPages();
			for (int x= 0; x < pages.length; x++) {
				IEditorReference[] editorRefs= pages[x].getEditorReferences();
				for (int z= 0; z < editorRefs.length; z++) {
					IEditorPart ep= editorRefs[z].getEditor(false);
					if (ep != null) {
						IEditorInput input= ep.getEditorInput();
						if (!inputs.contains(input)) {
							inputs.add(input);
							result.add(ep);
						}
					}
				}
			}
		}
		return (IEditorPart[])result.toArray(new IEditorPart[result.size()]);
	}

	
	protected boolean visitFile(IResourceProxy proxy) throws CoreException {
		if (! fScope.encloses(proxy))
			return false;

		// Exclude to derived resources
		if (proxy.isDerived())
			return false;

		if (fPattern.length() == 0) {
			fCollector.accept(proxy, "", -1, 0, -1); //$NON-NLS-1$
			updateProgressMonitor();
			return true;
		}
		IFile file= (IFile)proxy.requestResource();
		try {
			BufferedReader reader= null;
			ITextEditor editor= findEditorFor(file);
			if (editor != null) {
				String s= editor.getDocumentProvider().getDocument(editor.getEditorInput()).get();
				reader= new BufferedReader(new StringReader(s));
			} else {
				InputStream stream= file.getContents(false);
				reader= new BufferedReader(new InputStreamReader(stream, ResourcesPlugin.getEncoding()));
			}
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
					StringMatcher.Position match;
					while (start < lineLength) {
						if ((match= fMatcher.find(line, start, lineLength)) != null) {
							start= charCounter + match.getStart();
							int length= match.getEnd() - match.getStart();
							fCollector.accept(proxy, line.trim(), start, length, lineCounter);
							start= match.getEnd();
						}
						else	// no match in this line
							start= lineLength;
					}
					charCounter+= lineLength + eolStrLength;
					lineCounter++;
					if (fProgressMonitor.isCanceled())
						throw new OperationCanceledException(SearchMessages.getString("TextSearchVisitor.canceled")); //$NON-NLS-1$
				}
			} finally {
				if (reader != null)
					reader.close();
			}
		} catch (IOException e) {
			String message= SearchMessages.getFormattedString("TextSearchVisitor.error", file.getFullPath()); //$NON-NLS-1$
			throw new CoreException(new Status(IStatus.ERROR, SearchUI.PLUGIN_ID, Platform.PLUGIN_ERROR, message, e));
		}
		finally {
			updateProgressMonitor();
		}		
		return true;
	}

	private void updateProgressMonitor() {
		fNumberOfScannedFiles++;
		if (fNumberOfScannedFiles < fNumberOfFilesToScan) {
			if (System.currentTimeMillis() - fLastUpdateTime > 1000) {
				fMessageFormatArgs[0]= new Integer(fNumberOfScannedFiles+1);
				fProgressMonitor.setTaskName(SearchMessages.getFormattedString("TextSearchVisitor.scanning", fMessageFormatArgs)); //$NON-NLS-1$
				fLastUpdateTime= System.currentTimeMillis();
			}
		}
		fProgressMonitor.worked(1);
		if (fProgressMonitor.isCanceled())
			throw new OperationCanceledException(SearchMessages.getString("TextSearchVisitor.canceled")); //$NON-NLS-1$
	}

	private ITextEditor findEditorFor(IFile file) {
		int i= 0;
		while (i < fEditors.length) {
			IEditorPart editor= fEditors[i];
			IEditorInput input= editor.getEditorInput();
			if (input instanceof IFileEditorInput && editor instanceof ITextEditor)
				if (((IFileEditorInput)input).getFile().equals(file))
					return (ITextEditor)editor;
			i++;
		}
		return null;
	}
	
	protected int readLine(BufferedReader reader, StringBuffer sb) throws IOException {
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
	
	/*
	 * @see IResourceProxyVisitor#visit(IResourceProxy)
	 */
	public boolean visit(IResourceProxy proxy) {
		fEditors= getEditors();
		return super.visit(proxy);
	}
}

