/**********************************************************************
Copyright (c) 2000, 2003 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.core.internal.filebuffers;

import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;


public abstract class FileBuffer implements IFileBuffer {
	
		/**
		 * Runnable encapsulating an element state change. This runnable ensures 
		 * that a element change failed message is sent out to the element state
		 * listeners in case an exception occurred.
		 */
		private class SafeFileChange implements Runnable {
				
			/**
			 * Creates a new safe runnable for the given file.
			 */
			public SafeFileChange() {
			}
					
			/** 
			 * Execute the change.
			 * Subclass responsibility.
			 * 
			 * @exception an exception in case of error
			 */
			protected void execute() throws Exception {
			}
				
			/*
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
					
				if (isDisposed()) {
					fManager.fireStateChangeFailed(FileBuffer.this);
					return;
				}
					
				try {
					execute();
				} catch (Exception x) {
					FileBuffersPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IStatus.OK, "Exception when synchronizing", x)); //$NON-NLS-1$
					fManager.fireStateChangeFailed(FileBuffer.this);
				}
			}
		}
		
		/**
		 * Synchronizes the document with external resource changes.
		 */
		private class FileSynchronizer implements IResourceChangeListener, IResourceDeltaVisitor {
				
			/** A flag indicating whether this synchronizer is installed or not. */
			private boolean fIsInstalled= false;
				
			/**
			 * Creates a new file synchronizer. Is not yet installed on a file.
			 */
			public FileSynchronizer() {
			}
						
			/**
			 * Installs the synchronizer on the file.
			 */
			public void install() {
				fFile.getWorkspace().addResourceChangeListener(this);
				fIsInstalled= true;
			}
				
			/**
			 * Uninstalls the synchronizer from the file.
			 */
			public void uninstall() {
				fFile.getWorkspace().removeResourceChangeListener(this);
				fIsInstalled= false;
			}
				
			/*
			 * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
			 */
			public void resourceChanged(IResourceChangeEvent e) {
				IResourceDelta delta= e.getDelta();
				try {
					if (delta != null && fIsInstalled)
						delta.accept(this);
				} catch (CoreException x) {
					handleCoreException(x); 
				}
			}
				
			/*
			 * @see IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
			 */
			public boolean visit(IResourceDelta delta) throws CoreException {
								
				if (delta != null && fFile.equals(delta.getResource())) {
						
					Runnable runnable= null;
						
					switch (delta.getKind()) {
						case IResourceDelta.CHANGED:
							if ((IResourceDelta.CONTENT & delta.getFlags()) != 0) {
								if (!isDisposed() && !fCanBeSaved && fFile.isSynchronized(IFile.DEPTH_ZERO)) {
									runnable= new SafeFileChange() {
										protected void execute() throws Exception {
											if (fModificationStamp != fFile.getModificationStamp())
												handleFileContentChanged();
										}
									};
								}
							}
							break;
						case IResourceDelta.REMOVED:
							if ((IResourceDelta.MOVED_TO & delta.getFlags()) != 0) {
								final IPath path= delta.getMovedToPath();
								runnable= new SafeFileChange() {
									protected void execute() throws Exception {
										handleFileMoved(path);
									}
								};
							} else {
								if (!isDisposed() && !fCanBeSaved) {
									runnable= new SafeFileChange() {
										protected void execute() throws Exception {
											handleFileDeleted();
										}
									};
								}
							}
							break;
					}
						
					if (runnable != null)
						update(runnable);
				}
					
				return true; // because we are sitting on files anyway
			}
				
			/**
			 * Posts the update code "behind" the running operation.
			 *
			 * @param runnable the update code
			 */
			protected void update(Runnable runnable) {
				if (runnable instanceof SafeFileChange)
					fManager.fireStateChanging(FileBuffer.this);
				runnable.run();			
			}
		}
		
		
		
	/** The element for which the info is stored */
	protected IFile fFile;
	/** How often the element has been connected */
	protected int fReferenceCount;
	/** Can the element be saved */
	protected boolean fCanBeSaved= false;
	/** Has element state been validated */
	protected boolean fIsStateValidated= false;
	/** The status of this element */
	protected IStatus fStatus;
	/** The file synchronizer. */
	protected FileSynchronizer fFileSynchronizer;
	/** The time stamp at which this provider changed the file. */
	protected long fModificationStamp= IFile.NULL_STAMP;

	/** The text file buffer manager */
	protected TextFileBufferManager fManager;
	
	
	
	
	public FileBuffer(TextFileBufferManager manager) {
		super();
		fManager= manager;
	}

	abstract protected void handleFileContentChanged();
	
	abstract protected void addFileBufferContentListeners();
	
	abstract protected void removeFileBufferContentListeners();
	
	abstract protected void initializeFileBufferContent(IProgressMonitor monitor) throws CoreException;
	
	abstract protected void commitFileBufferContent(IProgressMonitor monitor, boolean overwrite) throws CoreException;
	
	
	public void create(IFile file, IProgressMonitor monitor) throws CoreException {
		fFile= file;
		fFileSynchronizer= new FileSynchronizer();
		refreshFile(monitor);
		
		initializeFileBufferContent(monitor);
		addFileBufferContentListeners();
	}
	
	public void connect() {
		++ fReferenceCount;
		if (fReferenceCount == 1)
			fFileSynchronizer.install();
	}
	
	public void disconnect() throws CoreException {
		-- fReferenceCount;
		if (fReferenceCount == 0) {
			if (fFileSynchronizer != null)
				fFileSynchronizer.uninstall();
			fFileSynchronizer= null;
		}
	}
	
	protected boolean isDisposed() {
		return fFileSynchronizer == null;
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBuffer#getUnderlyingFile()
	 */
	public IFile getUnderlyingFile() {
		return fFile;
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBuffer#commit(org.eclipse.core.runtime.IProgressMonitor, boolean)
	 */
	public void commit(IProgressMonitor monitor, boolean overwrite) throws CoreException {
		if (!isDisposed() && fCanBeSaved) {
			commitFileBufferContent(monitor, overwrite);
			fCanBeSaved= false;
			addFileBufferContentListeners();
			fManager.fireDirtyStateChanged(this, fCanBeSaved);
		}
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBuffer#isDirty()
	 */
	public boolean isDirty() {
		return fCanBeSaved;
	}
	
	/*
	 * @see org.eclipse.core.filebuffers.IFileBuffer#isShared()
	 */
	public boolean isShared() {
		return fReferenceCount > 1;
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBuffer#validateState(org.eclipse.core.runtime.IProgressMonitor, java.lang.Object)
	 */
	public void validateState(IProgressMonitor monitor, Object computationContext) throws CoreException {
		if (!isDisposed() && !fIsStateValidated)  {
			
			if (fFile.isReadOnly()) {
				IWorkspace workspace= fFile.getWorkspace();
				fStatus= workspace.validateEdit(new IFile[] { fFile }, computationContext);
			}
			fIsStateValidated= true;
			fManager.fireStateValidationChanged(this, fIsStateValidated);
		}
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBuffer#isStateValidated()
	 */
	public boolean isStateValidated() {
		return fIsStateValidated;
	}

	/**
	 * Sends out the notification that the file serving as document input has been moved.
	 * 
	 * @param newLocation the path of the new location of the file
	 */
	protected void handleFileMoved(IPath newLocation) {
		IWorkspace workspace=fFile.getWorkspace();
		IFile newFile= workspace.getRoot().getFile(newLocation);
		fManager.fireUnderlyingFileMoved(this, newFile);
	}
	
	/**
	 * Sends out the notification that the file serving as document input has been deleted.
	 */
	protected void handleFileDeleted() {
		fManager.fireUnderlyingFileDeleted(this);
	}
	
	/**
	 * Refreshes the given  file.
	 */
	protected void refreshFile(IProgressMonitor monitor) {
		try {
			fFile.refreshLocal(IFile.DEPTH_INFINITE, monitor);
		} catch (OperationCanceledException x) {
		} catch (CoreException x) {
			handleCoreException(x);
		}
	}

	/**
	 * Defines the standard procedure to handle <code>CoreExceptions</code>. Exceptions
	 * are written to the plug-in log.
	 *
	 * @param exception the exception to be logged
	 * @param message the message to be logged
	 */
	protected void handleCoreException(CoreException exception) {
		ILog log= Platform.getPlugin(FileBuffersPlugin.PLUGIN_ID).getLog();
		log.log(exception.getStatus());
	}
}
