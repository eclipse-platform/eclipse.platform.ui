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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.search.internal.core.ISearchScope;
import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.ui.NewSearchUI;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.text.IDocument;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * The visitor that does the actual work.
 */
public class TextSearchVisitor implements IResourceProxyVisitor {

	private ISearchScope fScope;
	private ITextSearchResultCollector fCollector;
	private MatchLocator fLocator;
	
	private Map fDocumentsInEditors;
		
	private IProgressMonitor fProgressMonitor;

	private int fNumberOfScannedFiles;
	private int fNumberOfFilesToScan;
	private long fLastUpdateTime;
	private boolean fVisitDerived;
	private final MultiStatus fStatus;
	private boolean fAllowNIOSearch;	
	
	public TextSearchVisitor(MatchLocator locator, ISearchScope scope, boolean visitDerived, ITextSearchResultCollector collector, MultiStatus status, int fileCount) {
		fScope= scope;
		fCollector= collector;
		fStatus= status;

		fProgressMonitor= collector.getProgressMonitor();

		fLocator= locator;
		
		fNumberOfScannedFiles= 0;
		fNumberOfFilesToScan= fileCount;
		fVisitDerived= visitDerived;
		fAllowNIOSearch= true;
	}
	
	public void setAllowNIOSearch(boolean allowNIOSearch) {
		fAllowNIOSearch= allowNIOSearch;
	}
	
	public void process(Collection projects) {
		fDocumentsInEditors= evalNonFileBufferDocuments();
		
		Iterator i= projects.iterator();
		while(i.hasNext()) {
			IProject project= (IProject)i.next();
			try {
				project.accept(this, IResource.NONE);
			} catch (CoreException ex) {
				fStatus.add(ex.getStatus());
			}
		}
		
		fDocumentsInEditors= null;
	}

	/**
	 * @return returns a map from IFile to IDocument for all open, dirty editors
	 */
	private Map evalNonFileBufferDocuments() {
		Map result= new HashMap();
		IWorkbench workbench= SearchPlugin.getDefault().getWorkbench();
		IWorkbenchWindow[] windows= workbench.getWorkbenchWindows();
		for (int i= 0; i < windows.length; i++) {
			IWorkbenchPage[] pages= windows[i].getPages();
			for (int x= 0; x < pages.length; x++) {
				IEditorReference[] editorRefs= pages[x].getEditorReferences();
				for (int z= 0; z < editorRefs.length; z++) {
					IEditorPart ep= editorRefs[z].getEditor(false);
					if (ep instanceof ITextEditor && ep.isDirty()) { // only dirty editors
						evaluateTextEditor(result, ep);
					}
				}
			}
		}
		return result;
	}

	private void evaluateTextEditor(Map result, IEditorPart ep) {
		IEditorInput input= ep.getEditorInput();
		if (input instanceof IFileEditorInput) {
			IFile file= ((IFileEditorInput) input).getFile();
			if (!result.containsKey(file)) { // take the first editor found
				ITextFileBufferManager bufferManager= FileBuffers.getTextFileBufferManager();
				ITextFileBuffer textFileBuffer= bufferManager.getTextFileBuffer(file.getFullPath());
				if (textFileBuffer != null) {
					// filebuffer has precedence
					result.put(file, textFileBuffer.getDocument());
				} else {
					// use document provider
					IDocument document= ((ITextEditor) ep).getDocumentProvider().getDocument(input);
					if (document != null) {
						result.put(file, textFileBuffer.getDocument());
					}
				}
			}
		}
	}

	private boolean shouldVisit(IResourceProxy proxy) {
		if (!fScope.encloses(proxy))
			return false;
		return fVisitDerived || !proxy.isDerived();
	}

	public boolean visit(IResourceProxy proxy) throws CoreException {
		if (proxy.getType() != IResource.FILE) {
			return true; // only interested in files
		}
		
		if (!shouldVisit(proxy))
			return false;

		if (fLocator.isEmtpy()) {
			fCollector.accept(proxy, -1, 0); //$NON-NLS-1$
			updateProgressMonitor();
			return true;
		}
		IFile file= (IFile)proxy.requestResource();
		IDocument document= (IDocument) fDocumentsInEditors.get(file);
		if (document == null) {
			ITextFileBufferManager bufferManager= FileBuffers.getTextFileBufferManager();
			ITextFileBuffer textFileBuffer= bufferManager.getTextFileBuffer(file.getFullPath());
			if (textFileBuffer != null) {
				document= textFileBuffer.getDocument();
			}
		}
		try {
			if (document != null) {
				fLocator.locateMatches(fProgressMonitor, new DocumentCharSequence(document), fCollector, proxy);
			} else {
				InputStream stream= null;
				try {
					Charset charset= Charset.forName(file.getCharset());
					
					stream= file.getContents();
					if (fAllowNIOSearch && stream instanceof FileInputStream) {
						FileChannel channel= ((FileInputStream) stream).getChannel();
						try {
							MappedByteBuffer mappedBuffer= channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
							CharSequence searchInput= charset.decode(mappedBuffer);
							fLocator.locateMatches(fProgressMonitor, searchInput, fCollector, proxy);
						} finally {
							channel.close();
						}
					} else {
						BufferedReader reader= new BufferedReader(new InputStreamReader(stream, charset));
						fLocator.locateMatches(fProgressMonitor, reader, fCollector, proxy);
					}
				} catch (UnsupportedCharsetException e) {
					String[] args= { file.getCharset(), file.getFullPath().makeRelative().toString()};
					String message= SearchMessages.getFormattedString("TextSearchVisitor.unsupportedcharset", args); //$NON-NLS-1$
					fStatus.add(new Status(IStatus.ERROR, NewSearchUI.PLUGIN_ID, Platform.PLUGIN_ERROR, message, e));
				} catch (IOException e) {
					String message= SearchMessages.getFormattedString("TextSearchVisitor.error", file.getFullPath().makeRelative()); //$NON-NLS-1$
					fStatus.add(new Status(IStatus.ERROR, NewSearchUI.PLUGIN_ID, Platform.PLUGIN_ERROR, message, e));
				} finally {
					if (stream != null) {
						try { stream.close();	} catch (IOException ignored) {}
					}
				}	
			}
		} finally {
			updateProgressMonitor();
		}		
		return true;
	}

	private void updateProgressMonitor() {
		fNumberOfScannedFiles++;
		if (fNumberOfScannedFiles < fNumberOfFilesToScan) {
			if (System.currentTimeMillis() - fLastUpdateTime > 1000) {
				String[] args= { String.valueOf(fNumberOfScannedFiles + 1),  String.valueOf(fNumberOfFilesToScan)};
				fProgressMonitor.setTaskName(SearchMessages.getFormattedString("TextSearchVisitor.scanning", args)); //$NON-NLS-1$
				fLastUpdateTime= System.currentTimeMillis();
			}
		}
		fProgressMonitor.worked(1);
		if (fProgressMonitor.isCanceled())
			throw new OperationCanceledException(SearchMessages.getString("TextSearchVisitor.canceled")); //$NON-NLS-1$
	}
}

