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
package org.eclipse.team.internal.ccvs.ui;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.team.core.subscribers.ITeamResourceChangeListener;
import org.eclipse.team.core.subscribers.TeamDelta;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.IStorageDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IElementStateListener;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.quickdiff.IQuickDiffProviderImplementation;

/**
 * A QuickDiff provider that provides a reference to the latest revision of a file
 * in the CVS repository. The provider notifies when the file's sync state changes
 * and the diff should be recalculated (e.g. commit, update...) or when the file
 * is changed (e.g. replace with).  
 * 
 * @since 3.0
 */
public class RemoteRevisionQuickDiffProvider implements IQuickDiffProviderImplementation {

	private boolean fDocumentRead = false;
	private ITextEditor fEditor = null;
	private IDocument fReference = null;
	private IDocumentProvider fDocumentProvider = null;
	private IEditorInput fInput = null;
	private String fId;
	private ICVSFile fCvsFile;

	private Job fUpdateJob;
	
	/**
	 * Updates the document if a sync changes occurs to the associated CVS file.
	 */
	private ITeamResourceChangeListener teamChangeListener = new ITeamResourceChangeListener() {
		public void teamResourceChanged(TeamDelta[] deltas) {
			if(fCvsFile != null) {
				for (int i = 0; i < deltas.length; i++) {
					TeamDelta delta = deltas[i];
					try {
						if(delta.getResource().equals(fCvsFile.getIResource())) {
							if(delta.getFlags() == TeamDelta.SYNC_CHANGED) {
								fetchContentsInJob();
							}
						}
					} catch (CVSException e) {
						e.printStackTrace();
					} 
				}
			}
		}
	};

	/**
	 * Updates the document if the document is changed (e.g. replace with)
	 */
	private IElementStateListener documentListener = new IElementStateListener() {
		public void elementDirtyStateChanged(Object element, boolean isDirty) {
		}

		public void elementContentAboutToBeReplaced(Object element) {
		}

		public void elementContentReplaced(Object element) {
			if(element == fInput) {
				fetchContentsInJob();
			}
		}

		public void elementDeleted(Object element) {
		}

		public void elementMoved(Object originalElement, Object movedElement) {
		}
	};
	
	/*
	 * @see org.eclipse.test.quickdiff.DocumentLineDiffer.IQuickDiffReferenceProvider#getReference()
	 */
	public IDocument getReference(IProgressMonitor monitor) {
		try {
			if (!fDocumentRead)
				readDocument(monitor);
			if (fDocumentRead)
				return fReference;
			else
				return null;
		} catch(CoreException e) {
			CVSUIPlugin.log(e);
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.quickdiff.IQuickDiffProviderImplementation#setActiveEditor(org.eclipse.ui.texteditor.ITextEditor)
	 */
	public void setActiveEditor(ITextEditor targetEditor) {
		if (targetEditor != fEditor) {
			dispose();
			fEditor= targetEditor;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.quickdiff.IQuickDiffProviderImplementation#isEnabled()
	 */
	public boolean isEnabled() {
		if (!initialized())
			return false;
		return getCVSFile() != null;
	}

	/*
	 * @see org.eclipse.jface.text.source.diff.DocumentLineDiffer.IQuickDiffReferenceProvider#dispose()
	 */
	public void dispose() {
		// stop update job
		if(fUpdateJob != null && fUpdateJob.getState() != Job.NONE) {
			fUpdateJob.cancel();
			try {
				fUpdateJob.join();
			} catch (InterruptedException e) {		
			}
		}
		
		fReference= null;
		fCvsFile = null;
		fUpdateJob = null;
		fInput = null;
		
		// remove listeners
		if(fDocumentProvider != null) {
			fDocumentProvider.removeElementStateListener(documentListener);
		}
		CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber().removeListener(teamChangeListener);
	}

	/*
	 * @see org.eclipse.quickdiff.QuickDiffTestPlugin.IQuickDiffProviderImplementation#setId(java.lang.String)
	 */
	public void setId(String id) {
		fId= id;
	}

	/*
	 * @see org.eclipse.jface.text.source.diff.DocumentLineDiffer.IQuickDiffReferenceProvider#getId()
	 */
	public String getId() {
		return fId;
	}
	
	private boolean initialized() {
		return fEditor != null;
	}
	
	/**
	 * Creates a document and initializes it with the contents of a CVS remote
	 * resource.
	 * @param monitor the progress monitor
	 * @throws CoreException
	 */
	private void readDocument(IProgressMonitor monitor) throws CoreException {
		if (!initialized())
			return;

		fDocumentRead= false;
	
		if (fReference == null) {
			fReference= new Document();
		}
	
		fCvsFile = getCVSFile();
		if(fCvsFile != null) {
			ICVSRemoteResource remote = CVSWorkspaceRoot.getRemoteTree(fCvsFile.getIResource(), fCvsFile.getSyncInfo().getTag(), monitor);
			IDocumentProvider docProvider= fEditor.getDocumentProvider();
			if (docProvider instanceof IStorageDocumentProvider) {
				fDocumentProvider = docProvider;
				IStorageDocumentProvider provider= (IStorageDocumentProvider) fDocumentProvider;			
				String encoding= provider.getEncoding(fEditor.getEditorInput());
				if (encoding == null) {
					encoding= provider.getDefaultEncoding();
				}
				InputStream stream= remote.getContents(monitor);
				if (stream == null) {
					return;
				}
				setDocumentContent(fReference, stream, encoding);
				
				CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber().addListener(teamChangeListener);
				((IDocumentProvider)provider).addElementStateListener(documentListener);
			}
		}
		fDocumentRead= true;
	}
	
	/**
	 * Intitializes the given document with the given stream using the given encoding.
	 *
	 * @param document the document to be initialized
	 * @param contentStream the stream which delivers the document content
	 * @param encoding the character encoding for reading the given stream
	 * @exception CoreException if the given stream can not be read
	 */
	private static void setDocumentContent(IDocument document, InputStream contentStream, String encoding) throws CoreException {
		Reader in= null;
		try {
			final int DEFAULT_FILE_SIZE= 15 * 1024;

			in= new BufferedReader(new InputStreamReader(contentStream, encoding), DEFAULT_FILE_SIZE);
			CharArrayWriter caw= new CharArrayWriter(DEFAULT_FILE_SIZE);
			char[] readBuffer= new char[2048];
			int n= in.read(readBuffer);
			while (n > 0) {
				caw.write(readBuffer, 0, n);
				n= in.read(readBuffer);
			}
			document.set(caw.toString());
		} catch (IOException x) {
			throw new CVSException(Policy.bind("RemoteRevisionQuickDiffProvider.readingFile"), x);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException x) {
					throw new CVSException(Policy.bind("RemoteRevisionQuickDiffProvider.closingFile"), x);
				}
			}
		}
	}
	
	/**
	 * Returns the ICVSFile associated with he active editor or <code>null</code> 
	 * if the provider doesn't not have access to a CVS managed file.
	 * @return the handle to a CVS file
	 */
	private ICVSFile getCVSFile() {
		if(fEditor != null) {
			IEditorInput input= fEditor.getEditorInput();
			if (input instanceof IFileEditorInput) {
				IFile file= ((IFileEditorInput)input).getFile();
				try {
					if(CVSWorkspaceRoot.isSharedWithCVS(file)) {
						return CVSWorkspaceRoot.getCVSFileFor(file);
					}
				} catch (CVSException e) {
					CVSUIPlugin.log(e);
				}
			}
		}
		return null;
	}

	/**
	 * Runs a job that updates the document. If a previous job is already running it
	 * is stopped before the new job can start.
	 */
	private void fetchContentsInJob() {
		if(fUpdateJob != null && fUpdateJob.getState() != Job.NONE) {
			fUpdateJob.cancel();
			try {
				fUpdateJob.join();
			} catch (InterruptedException e) {				
			}
		}
		Job updateJob = new Job(Policy.bind("RemoteRevisionQuickDiffProvider.fetchingFile")) {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					readDocument(monitor);
				} catch (CoreException e) {
					return e.getStatus();
				}
				return Status.OK_STATUS;
			}
		};
		updateJob.schedule();
	}
}
