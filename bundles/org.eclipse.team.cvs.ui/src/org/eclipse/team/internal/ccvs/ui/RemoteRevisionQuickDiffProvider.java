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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.ITeamResourceChangeListener;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.core.subscribers.TeamDelta;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
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
 * unmanaged file : reference == null
 * new file transitions to managed (add then commit) : reference updated with remote revision 
 * managed file has new remote (commit, refresh remote) : reference updated with new remote revision
 * managed file cleaned, remote is the same (replace with, update) : refresh diff bar with existing reference 
 * managed file is unmanaged (unmanage operation) :   
 * 
 * @since 3.0
 */
public class RemoteRevisionQuickDiffProvider implements IQuickDiffProviderImplementation {
	// The editor showing this quickdiff and provides access to the editor input and
	// ultimatly the IFile.
	private ITextEditor fEditor = null;	
	
	// The document containing the remote file. Can be null if the assigned editor doesn't have
	// a CVS remote resource associated with it.
	private IDocument fReference = null;
	
	// Will be true when the document has been read and initialized.
	private boolean fReferenceInitialized = false;
	
	// Document provider allows us to register/deregister the element state change listener.
	private IDocumentProvider fDocumentProvider = null;

	// Unique id for this reference provider as set via setId(). 
	private String fId;
	
	// A handle to the remote CVS file for this provider. 
	private SyncInfo fLastSyncState;

	// Job that re-creates the reference document.
	private Job fUpdateJob;
	
	private boolean DEBUG = false;
	
	/**
	 * Updates the document if a sync changes occurs to the associated CVS file.
	 */
	private ITeamResourceChangeListener teamChangeListener = new ITeamResourceChangeListener() {
		public void teamResourceChanged(TeamDelta[] deltas) {
			if(initialized()) {
				for (int i = 0; i < deltas.length; i++) {
					TeamDelta delta = deltas[i];
					IResource resource = delta.getResource();
					if(resource.getType() == IResource.FILE && 
					   fLastSyncState != null && resource.equals(fLastSyncState.getLocal())) {
						if(delta.getFlags() == TeamDelta.SYNC_CHANGED) {
							fetchContentsInJob();
						}
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
			if(fEditor != null && fEditor.getEditorInput() == element) {
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
			if (! fReferenceInitialized) {
				readDocument(monitor);
			}
			return fReference;
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
		return getManagedCVSFile() != null;
	}

	/*
	 * @see org.eclipse.jface.text.source.diff.DocumentLineDiffer.IQuickDiffReferenceProvider#dispose()
	 */
	public void dispose() {
		fReferenceInitialized = false;
		// stop update job
		if(fUpdateJob != null && fUpdateJob.getState() != Job.NONE) {
			fUpdateJob.cancel();
			try {
				fUpdateJob.join();
			} catch (InterruptedException e) {		
			}
		}
		synchronized(this) {
			// remove listeners
			if(fDocumentProvider != null) {
				fDocumentProvider.removeElementStateListener(documentListener);
			}
			CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber().removeListener(teamChangeListener);			
			
			fReferenceInitialized = false;
			fReference= null;
			fUpdateJob = null;
			fLastSyncState = null;
		}
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
	
	/**
	 * Determine if the file represented by this quickdiff provider has changed with
	 * respect to it's remote state. Return true if the remote contents should be
	 * refreshed, and false if not.
	 */
	private boolean computeChange(IProgressMonitor monitor) throws TeamException {
		boolean needToUpdateReferenceDocument = false;
		if(initialized()) {
			SyncInfo info = getSyncState(getFileFromEditor(), monitor);
			// check if 
			if(info == null && fLastSyncState != null) {
				return true;
			}
			int kind = info.getKind();			
			if(fLastSyncState == null) {
				needToUpdateReferenceDocument = true;
			} else if(! fLastSyncState.equals(info)) {
				needToUpdateReferenceDocument = true; 
			}
			if(DEBUG) debug(fLastSyncState, info);
			fLastSyncState = info;
		}		
		return needToUpdateReferenceDocument;		
	}
	
	private void debug(SyncInfo lastSyncState, SyncInfo info) {
		String last = "[none]";
		if(lastSyncState != null) {
			last = lastSyncState.toString();
		}
		System.out.println("+ CVSQuickDiff: was " + last + " is " + info.toString());
	}

	private SyncInfo getSyncState(IResource resource, IProgressMonitor monitor) throws TeamException {
		ICVSFile cvsFile = getManagedCVSFile();
		return CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber().getSyncInfo(resource, monitor);
	}
	
	/**
	 * This provider is initialized if an editor has been assigned and the input for
	 * the editor is a FileEditorInput.
	 * @return true if initialized and false otherwise.
	 */
	private boolean initialized() {
		return fEditor != null && fEditor.getEditorInput() instanceof IFileEditorInput;
	}
	
	/**
	 * Creates a document and initializes it with the contents of a CVS remote
	 * resource.
	 * @param monitor the progress monitor
	 * @throws CoreException
	 */
	private synchronized void readDocument(IProgressMonitor monitor) throws CoreException {
		if (!initialized())
			return;

		fReferenceInitialized= false;
		fDocumentProvider= fEditor.getDocumentProvider();
		
		if (fReference == null) {
			fReference= new Document();
		}

		if(computeChange(monitor)) {
			ICVSRemoteFile remoteFile = (ICVSRemoteFile)fLastSyncState.getRemote(); 
			if (fLastSyncState.getRemote() != null && fDocumentProvider instanceof IStorageDocumentProvider) {
				IStorageDocumentProvider provider= (IStorageDocumentProvider) fDocumentProvider;			
				String encoding= provider.getEncoding(fEditor.getEditorInput());
				if (encoding == null) {
					encoding= provider.getDefaultEncoding();
				}
				InputStream stream= remoteFile.getContents(monitor);
				if (stream == null) {
					return;
				}
				if(! monitor.isCanceled()) {
					setDocumentContent(fReference, stream, encoding);
				}
			} else {
				// the remote is null, so juts ensure that the document is null
				fReference = new Document();
				fReference.set("");
			}
		}
		
		if(fDocumentProvider != null) {
			CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber().addListener(teamChangeListener);
			((IDocumentProvider)fDocumentProvider).addElementStateListener(documentListener);
		}
		
		fReferenceInitialized= true;
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
	private ICVSFile getManagedCVSFile() {
		if(fEditor != null) {
			IFile file = getFileFromEditor();
			try {
				if(file != null && CVSWorkspaceRoot.isSharedWithCVS(file)) {
					return CVSWorkspaceRoot.getCVSFileFor(file);
				}
			} catch (CVSException e) {
				CVSUIPlugin.log(e);
			}
		}
		return null;
	}

	private IFile getFileFromEditor() {
		if(fEditor != null) {
			IEditorInput input= fEditor.getEditorInput();
			if (input instanceof IFileEditorInput) {
				return ((IFileEditorInput)input).getFile();
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
