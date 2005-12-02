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
package org.eclipse.search.internal.core.text;

import java.io.IOException;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;

import org.eclipse.jface.text.IDocument;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.search.ui.NewSearchUI;

import org.eclipse.search.internal.core.SearchScope;
import org.eclipse.search.internal.ui.Messages;
import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.internal.ui.SearchPlugin;

/**
 * The visitor that does the actual work.
 */
public class TextSearchVisitor implements IResourceProxyVisitor {

	private SearchScope fScope;
	private ITextSearchResultCollector fCollector;
	private MatchLocator fLocator;
	
	private Map fDocumentsInEditors;
		
	private IProgressMonitor fProgressMonitor;

	private int fNumberOfScannedFiles;
	private int fNumberOfFilesToScan;
	private long fLastUpdateTime;
	private boolean fVisitDerived;
	private final MultiStatus fStatus;
	
	private final FileCharSequenceProvider fFileCharSequenceProvider;
	
	public TextSearchVisitor(MatchLocator locator, SearchScope scope, boolean visitDerived, ITextSearchResultCollector collector, MultiStatus status, int fileCount) {
		fScope= scope;
		fCollector= collector;
		fStatus= status;

		fProgressMonitor= collector.getProgressMonitor();

		fLocator= locator;
		
		fNumberOfScannedFiles= 0;
		fNumberOfFilesToScan= fileCount;
		fVisitDerived= visitDerived;
		
		fFileCharSequenceProvider= new FileCharSequenceProvider();
	}
		
	public void process() {
		fDocumentsInEditors= evalNonFileBufferDocuments();
		
		IResource[] roots= fScope.getRootElements();
		for (int i= 0; i < roots.length; i++) {
			try {
				roots[i].accept(this, 0);
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
						result.put(file, document);
					}
				}
			}
		}
	}

	public boolean visit(IResourceProxy proxy) {
		if (!fVisitDerived && proxy.isDerived()) {
			return false; // all resources in a derived folder are considered to be derived, see bug 103576
		}
		
		if (proxy.getType() != IResource.FILE) {
			return true; // only interested in files
		}
		
		if (!fScope.matchesFileName(proxy.getName())) {
			return false; // finish, files don't have children
		}
		
		try {
			if (fLocator.isEmpty()) {
				fCollector.accept(proxy, -1, 0);
				return false; // finish, files don't have children
			}
			IFile file= (IFile) proxy.requestResource();
			IDocument document= getOpenDocument(file);
			if (document != null) {
				fLocator.locateMatches(fProgressMonitor, new DocumentCharSequence(document), fCollector, proxy);
			} else {
				CharSequence seq= null;
				try {
					seq= fFileCharSequenceProvider.newCharSequence(file);
					fLocator.locateMatches(fProgressMonitor, seq, fCollector, proxy);
				} catch (FileCharSequenceProvider.FileCharSequenceException e) {
					e.throwWrappedException();
				} finally {
					if (seq != null) {
						try {
							fFileCharSequenceProvider.releaseCharSequence(seq);
						} catch (IOException e) {
							SearchPlugin.log(e);
						}
					}
				}
			}
		} catch (UnsupportedCharsetException e) {
			IFile file= (IFile) proxy.requestResource();
			String[] args= { getCharSetName(file), file.getFullPath().makeRelative().toString()};
			String message= Messages.format(SearchMessages.TextSearchVisitor_unsupportedcharset, args); 
			fStatus.add(new Status(IStatus.ERROR, NewSearchUI.PLUGIN_ID, Platform.PLUGIN_ERROR, message, e));
		} catch (IllegalCharsetNameException e) {
			IFile file= (IFile) proxy.requestResource();
			String[] args= { getCharSetName(file), file.getFullPath().makeRelative().toString()};
			String message= Messages.format(SearchMessages.TextSearchVisitor_illegalcharset, args);
			fStatus.add(new Status(IStatus.ERROR, NewSearchUI.PLUGIN_ID, Platform.PLUGIN_ERROR, message, e));
		} catch (IOException e) {
			IFile file= (IFile) proxy.requestResource();
			String[] args= { getExceptionMessage(e), file.getFullPath().makeRelative().toString()};
			String message= Messages.format(SearchMessages.TextSearchVisitor_error, args); 
			fStatus.add(new Status(IStatus.ERROR, NewSearchUI.PLUGIN_ID, Platform.PLUGIN_ERROR, message, e));
		} catch (CoreException e) {
			IFile file= (IFile) proxy.requestResource();
			String[] args= { getExceptionMessage(e), file.getFullPath().makeRelative().toString()};
			String message= Messages.format(SearchMessages.TextSearchVisitor_error, args); 
			fStatus.add(new Status(IStatus.ERROR, NewSearchUI.PLUGIN_ID, Platform.PLUGIN_ERROR, message, e));
		} finally {
			updateProgressMonitor();
		}		
		return false; // finish, files don't have children
	}
	
	private String getExceptionMessage(Exception e) {
		String message= e.getLocalizedMessage();
		if (message == null) {
			return e.getClass().getName();
		}
		return message;
	}

	private IDocument getOpenDocument(IFile file) {
		IDocument document= (IDocument) fDocumentsInEditors.get(file);
		if (document == null) {
			ITextFileBufferManager bufferManager= FileBuffers.getTextFileBufferManager();
			ITextFileBuffer textFileBuffer= bufferManager.getTextFileBuffer(file.getFullPath());
			if (textFileBuffer != null) {
				document= textFileBuffer.getDocument();
			}
		}
		return document;
	}
	
	private String getCharSetName(IFile file) {
		try {
			return file.getCharset();
		} catch (CoreException e) {
			return "unknown"; //$NON-NLS-1$
		}
	}

	private void updateProgressMonitor() {
		fNumberOfScannedFiles++;
		if (fNumberOfScannedFiles < fNumberOfFilesToScan) {
			long currTime= System.currentTimeMillis();
			if (currTime - fLastUpdateTime > 1000) {
				Object[] args= { new Integer(fNumberOfScannedFiles + 1),  new Integer(fNumberOfFilesToScan)};
				fProgressMonitor.setTaskName(Messages.format(SearchMessages.TextSearchVisitor_scanning, args)); 
				fLastUpdateTime= currTime;
			}
		}
		fProgressMonitor.worked(1);
		if (fProgressMonitor.isCanceled())
			throw new OperationCanceledException(SearchMessages.TextSearchVisitor_canceled); 
	}
}

