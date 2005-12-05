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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
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

import org.eclipse.search.core.text.TextSearchMatchAccess;
import org.eclipse.search.core.text.TextSearchRequestor;
import org.eclipse.search.core.text.TextSearchScope;

import org.eclipse.search.internal.ui.Messages;
import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.internal.ui.SearchPlugin;

/**
 * The visitor that does the actual work.
 */
public class TextSearchVisitor implements IResourceProxyVisitor {
	
	public static class ReusableMatchAccess extends TextSearchMatchAccess {
		
		private int fOffset;
		private int fLength;
		private IFile fFile;
		private CharSequence fContent;
		
		public void initialize(IFile file, int offset, int length, CharSequence content) {
			fFile= file;
			fOffset= offset;
			fLength= length;
			fContent= content;
		}
				
		public IFile getFile() {
			return fFile;
		}
		
		public int getMatchOffset() {
			return fOffset;
		}
		
		public int getMatchLength() {
			return fLength;
		}

		public int getFileContentLength() {
			return fContent.length();
		}

		public char getFileContentChar(int offset) {
			return fContent.charAt(offset);
		}

		public String getFileContent(int offset, int length) {
			return fContent.subSequence(offset, offset + length).toString(); // must pass a copy!
		}
	}
	

	private final TextSearchScope fScope;
	private final TextSearchRequestor fCollector;
	private final Matcher fMatcher;
	
	private Map fDocumentsInEditors;
		
	private final IProgressMonitor fProgressMonitor;

	private int fNumberOfScannedFiles;
	private int fNumberOfFilesToScan;
	private long fLastUpdateTime;

	private final MultiStatus fStatus;
	
	private final FileCharSequenceProvider fFileCharSequenceProvider;
	
	private final ReusableMatchAccess fMatchAccess;
	
	public TextSearchVisitor(TextSearchScope scope, TextSearchRequestor collector, Pattern searchPattern, int fileCount, IProgressMonitor monitor) {
		fScope= scope;
		fCollector= collector;
		fProgressMonitor= monitor;
		fStatus= new MultiStatus(NewSearchUI.PLUGIN_ID, IStatus.OK, SearchMessages.TextSearchEngine_statusMessage, null);
		
		fMatcher= searchPattern.pattern().length() == 0 ? null : searchPattern.matcher(new String());
		
		fNumberOfScannedFiles= 0;
		fNumberOfFilesToScan= fileCount;
		
		fFileCharSequenceProvider= new FileCharSequenceProvider();
		fMatchAccess= new ReusableMatchAccess();
	}
		
	public void process() {
		fDocumentsInEditors= evalNonFileBufferDocuments();
		
		IResource[] roots= fScope.getRoots();
		for (int i= 0; i < roots.length; i++) {
			try {
				roots[i].accept(this, 0);
			} catch (CoreException ex) {
				fStatus.add(ex.getStatus());
			}
		}
		
		fDocumentsInEditors= null;
	}
	
	public IStatus getStatus() {
		return fStatus;
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
					// file buffer has precedence
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
		if (!fScope.contains(proxy)) {
			return false;
		}
		if (proxy.getType() != IResource.FILE) {
			return true;
		}
		try {
			boolean res= fCollector.acceptFile(proxy);
			if (!res || fMatcher == null) {
				return false;
			}
			
			IFile file= (IFile) proxy.requestResource();
			IDocument document= getOpenDocument(file);
			
			if (document != null) {
				locateMatches(file, new DocumentCharSequence(document));
			} else {
				CharSequence seq= null;
				try {
					seq= fFileCharSequenceProvider.newCharSequence(file);
					locateMatches(file, seq);
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
	
	private void locateMatches(IFile file, CharSequence searchInput) throws CoreException {
		fMatcher.reset(searchInput);
		int k= 0;
		while (fMatcher.find()) {
			int start= fMatcher.start();
			int end= fMatcher.end();
			if (end != start) { // don't report 0-length matches
				fMatchAccess.initialize(file, start, end - start, searchInput);
				boolean res= fCollector.acceptPatternMatch(fMatchAccess);
				if (!res) {
					return; // no further reporting requested
				}
			}
			if (k++ == 20) {
				if (fProgressMonitor.isCanceled()) {
					throw new OperationCanceledException(SearchMessages.TextSearchVisitor_canceled); 
				}
				k= 0;
			}
		}
		fMatchAccess.initialize(null, 0, 0, new String()); // clear references
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
	
	
	public static IStatus search(TextSearchScope scope, TextSearchRequestor collector, Pattern searchPattern, IProgressMonitor monitor) {
		if (monitor == null) {
			monitor= new NullProgressMonitor();
		}
		
		int amountOfWork= new AmountOfWorkCalculator(scope).process();
		try {
			monitor.beginTask("", amountOfWork); //$NON-NLS-1$
			if (amountOfWork > 0) {
				Integer[] args= new Integer[] {new Integer(1), new Integer(amountOfWork)};
				monitor.setTaskName(Messages.format(SearchMessages.TextSearchEngine_scanning, args)); 
			}				
			collector.beginReporting();
			TextSearchVisitor visitor= new TextSearchVisitor(scope, collector, searchPattern, amountOfWork, monitor);
			visitor.process();
			return visitor.getStatus();
		} finally {
			monitor.done();
			collector.endReporting();
		}
		
	}
	
}

