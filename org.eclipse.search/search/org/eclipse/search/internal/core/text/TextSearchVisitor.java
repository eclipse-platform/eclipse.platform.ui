/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
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
	private String fOptions;
	private IEditorPart[] fDirtyEditors;
		
	private IProgressMonitor fProgressMonitor;
	private StringMatcher fMatcher;
	private String fErrorMessage;
	
	protected int fPushbackChar;
	protected boolean fPushback;
	
	
	public TextSearchVisitor(String pattern, String options, ISearchScope scope, ITextSearchResultCollector collector, MultiStatus status) {
		super(status);
		fPattern= pattern;
		fScope= scope;
		fCollector= collector;
		fPushback= false;
		if (options != null)
			fOptions= options;
		else
			fOptions= "";	 //$NON-NLS-1$

		fProgressMonitor= collector.getProgressMonitor();
		fMatcher= new StringMatcher(pattern, options.indexOf('i') != -1, false);
	}
	
	public void process(Collection projects) {
		Iterator i= projects.iterator();
		while(i.hasNext()) {
			IProject project= (IProject)i.next();
			try {
				project.accept(this);
			} catch (CoreException ex) {
				addToStatus(ex);
			}
		}
	}

	/**
	 * Returns an array of all editors that have an unsaved content. If the identical content is 
	 * presented in more than one editor, only one of those editor parts is part of the result.
	 * 
	 * @return an array of all dirty editor parts.
	 */
	private IEditorPart[] getDirtyEditors() {
		Set inputs= new HashSet(7);
		List result= new ArrayList(0);
		IWorkbench workbench= SearchPlugin.getDefault().getWorkbench();
		IWorkbenchWindow[] windows= workbench.getWorkbenchWindows();
		for (int i= 0; i < windows.length; i++) {
			IWorkbenchPage[] pages= windows[i].getPages();
			for (int x= 0; x < pages.length; x++) {
				IEditorPart[] editors= pages[x].getEditors();
				for (int z= 0; z < editors.length; z++) {
					IEditorPart editor= editors[z];
					IEditorInput input= editor.getEditorInput();
					if (editor.isDirty() && !inputs.contains(input)) {
						inputs.add(input);
						result.add(editor);
					}
				}
			}
		}
		return (IEditorPart[])result.toArray(new IEditorPart[result.size()]);
	}

	
	protected boolean visitFile(IFile file) throws CoreException {
		if (! fScope.encloses(file))
			return false;

		if (fPattern.length() == 0) {
			fCollector.accept(file, "", -1, 0, -1); //$NON-NLS-1$
			return true;
		}
			
		try {
			BufferedReader reader= null;
			ITextEditor editor= findDirtyEditorFor(file);
			if (editor != null) {
				String s= editor.getDocumentProvider().getDocument(editor.getEditorInput()).get();
				reader= new BufferedReader(new StringReader(s));
			} else {
				InputStream stream= file.getContents(false);
				reader= new BufferedReader(new InputStreamReader(stream));
			}
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
			fProgressMonitor.worked(1);
			if (fProgressMonitor.isCanceled())
				throw new OperationCanceledException(SearchMessages.getString("TextSearchVisitor.canceled")); //$NON-NLS-1$
		}		
		return true;
	}

	private ITextEditor findDirtyEditorFor(IFile file) {
		int i= 0;
		while (i < fDirtyEditors.length) {
			IEditorPart dirtyEditor= fDirtyEditors[i];
			IEditorInput input= dirtyEditor.getEditorInput();
			if (input instanceof IFileEditorInput && dirtyEditor instanceof ITextEditor)
				if (((IFileEditorInput)input).getFile().equals(file))
					return (ITextEditor)dirtyEditor;
			i++;
		}
		return null;
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
	
	/*
	 * @see IResourceVisitor#visit(IResource)
	 */
	public boolean visit(IResource resource) {
		fDirtyEditors= getDirtyEditors();
		return super.visit(resource);
	}
}

