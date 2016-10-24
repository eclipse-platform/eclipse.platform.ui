/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.filebuffers;

import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.filebuffers.IFileBufferStatusCodes;

import org.eclipse.jface.text.IDocumentExtension4;


public abstract class ResourceFileBuffer extends AbstractFileBuffer {

		/**
		 * Runnable encapsulating an element state change. This runnable ensures
		 * that a element change failed message is sent out to the element state
		 * listeners in case an exception occurred.
		 */
		private abstract class SafeFileChange implements Runnable {

			/**
			 * Creates a new safe runnable for the given file.
			 */
			public SafeFileChange() {
			}

			/**
			 * Execute the change.
			 * Subclass responsibility.
			 *
			 * @exception Exception in case of error
			 */
			protected abstract void execute() throws Exception;

			/**
			 * Does everything necessary prior to execution.
			 */
			public void preRun() {
				fManager.fireStateChanging(ResourceFileBuffer.this);
			}

			@Override
			public void run() {

				if (isDisconnected()) {
					fManager.fireStateChangeFailed(ResourceFileBuffer.this);
					return;
				}

				try {
					execute();
				} catch (Exception x) {
					FileBuffersPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IStatus.OK, "Exception when synchronizing", x)); //$NON-NLS-1$
					fManager.fireStateChangeFailed(ResourceFileBuffer.this);
				}
			}
		}

		/**
		 * Synchronizes the document with external resource changes.
		 */
		private class FileSynchronizer implements IResourceChangeListener {

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

			@Override
			public void resourceChanged(IResourceChangeEvent e) {
				IResourceDelta delta= e.getDelta();
				if (delta != null)
					delta= delta.findMember(fFile.getFullPath());

				if (delta != null && fIsInstalled) {
					SafeFileChange fileChange= null;

					final int flags= delta.getFlags();
					switch (delta.getKind()) {
						case IResourceDelta.CHANGED:
							if ((IResourceDelta.ENCODING & flags) != 0) {
								if (!isDisconnected() && !fCanBeSaved && isSynchronized()) {
									fileChange= new SafeFileChange() {
										@Override
										protected void execute() throws Exception {
											handleFileContentChanged(false, false);
										}
									};
								}
							}
							if (fileChange == null && (IResourceDelta.CONTENT & flags) != 0) {
								if (!isDisconnected() && !fCanBeSaved && (!isSynchronized() || (IResourceDelta.REPLACED & flags) != 0)) {
									fileChange= new SafeFileChange() {
										@Override
										protected void execute() throws Exception {
											handleFileContentChanged(false, true);
										}
									};
								}
							}
							break;
						case IResourceDelta.REMOVED:
							if ((IResourceDelta.MOVED_TO & flags) != 0) {
								final IPath path= delta.getMovedToPath();
								fileChange= new SafeFileChange() {
									@Override
									protected void execute() throws Exception {
										handleFileMoved(path);
									}
								};
							} else {
								if (!isDisconnected() && !fCanBeSaved) {
									fileChange= new SafeFileChange() {
										@Override
										protected void execute() throws Exception {
											handleFileDeleted();
										}
									};
								}
							}
							break;
						default:
							break;
					}

					if (fileChange != null) {
						fileChange.preRun();
						if (isSynchronizationContextRequested()) {
							fManager.execute(fileChange);
						} else {
							fileChange.run();
						}
					}
				}
			}
		}



	/** The location */
	protected IPath fLocation;
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
	/** The modification stamp at which this buffer synchronized with the underlying file. */
	protected long fSynchronizationStamp= IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;
	/** How often the synchronization context has been requested */
	protected int fSynchronizationContextCount;


	public ResourceFileBuffer(TextFileBufferManager manager) {
		super(manager);
	}

	abstract protected void addFileBufferContentListeners();

	abstract protected void removeFileBufferContentListeners();

	abstract protected void initializeFileBufferContent(IProgressMonitor monitor) throws CoreException;

	abstract protected void commitFileBufferContent(IProgressMonitor monitor, boolean overwrite) throws CoreException;

	abstract protected void handleFileContentChanged(boolean revert, boolean updateModificationStamp) throws CoreException;


	@Override
	public void create(IPath location, IProgressMonitor monitor) throws CoreException {
		SubMonitor subMonitor= SubMonitor.convert(monitor, FileBuffersMessages.ResourceFileBuffer_task_creatingFileBuffer, 2);

		IWorkspaceRoot workspaceRoot= ResourcesPlugin.getWorkspace().getRoot();
		IFile file= workspaceRoot.getFile(location);
		URI uri= file.getLocationURI();
		if (uri == null) {
			String message= NLSUtility.format(FileBuffersMessages.ResourceFileBuffer_error_cannot_determine_URI, location);
			throw new CoreException(new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IStatus.OK, message, null));
		}

		fLocation= location;
		fFile= file;
		fFileStore= EFS.getStore(uri);
		fFileSynchronizer= new FileSynchronizer();

		initializeFileBufferContent(subMonitor.split(1));

		fSynchronizationStamp= fFile.getModificationStamp();

		addFileBufferContentListeners();
		subMonitor.split(1);

	}

	@Override
	public void connect() {
		++fReferenceCount;
		if (fReferenceCount == 1)
			connected();
	}

	/**
	 * Called when this file buffer has been connected. This is the case when
	 * there is exactly one connection.
	 * <p>
	 * Clients may extend this method.
	 */
	protected void connected() {
		fFileSynchronizer.install();
	}

	@Override
	public void disconnect() throws CoreException {
		--fReferenceCount;
		if (fReferenceCount <= 0)
			disconnected();
	}

	/**
	 * Called when this file buffer has been disconnected. This is the case when
	 * the number of connections drops below <code>1</code>.
	 * <p>
	 * Clients may extend this method.
	 */
	protected void disconnected() {
		if (fFileSynchronizer != null) {
			fFileSynchronizer.uninstall();
			fFileSynchronizer= null;
		}
		removeFileBufferContentListeners();
	}

	@Override
	public boolean isDisconnected() {
		return fFileSynchronizer == null;
	}

	@Override
	public IPath getLocation() {
		return fLocation;
	}

	@Override
	public ISchedulingRule computeCommitRule() {
		IResourceRuleFactory factory= ResourcesPlugin.getWorkspace().getRuleFactory();
		return factory.modifyRule(fFile);
	}

	@Override
	public void commit(IProgressMonitor monitor, boolean overwrite) throws CoreException {
		if (!isDisconnected() && fCanBeSaved) {

			fManager.fireStateChanging(this);

			try {
				commitFileBufferContent(monitor, overwrite);
			} catch (CoreException x) {
				fManager.fireStateChangeFailed(this);
				throw x;
			} catch (RuntimeException x) {
				fManager.fireStateChangeFailed(this);
				throw x;
			}

			fCanBeSaved= false;
			fManager.fireDirtyStateChanged(this, fCanBeSaved);
		}
	}

	@Override
	public void revert(IProgressMonitor monitor) throws CoreException {
		if (isDisconnected())
			return;

		if (!fFile.isSynchronized(IResource.DEPTH_INFINITE)) {
			fCanBeSaved= false;
			refreshFile(monitor);
			return;
		}

		try {
			fManager.fireStateChanging(this);
			handleFileContentChanged(true, false);
		} catch (RuntimeException x) {
			fManager.fireStateChangeFailed(this);
			throw x;
		}
	}

	@Override
	public boolean isDirty() {
		return fCanBeSaved;
	}

	@Override
	public void setDirty(boolean isDirty) {
		fCanBeSaved= isDirty;
	}

	@Override
	public boolean isShared() {
		return fReferenceCount > 1;
	}

	@Override
	public ISchedulingRule computeValidateStateRule() {
		IResourceRuleFactory factory= ResourcesPlugin.getWorkspace().getRuleFactory();
		return factory.validateEditRule(new IResource[] { fFile });
	}

	@Override
	public void validateState(IProgressMonitor monitor, Object computationContext) throws CoreException {
		if (!isDisconnected() && !fIsStateValidated)  {

			fStatus= null;

			fManager.fireStateChanging(this);

			try {
				if (fFile.isReadOnly()) {
					IWorkspace workspace= fFile.getWorkspace();
					fStatus= workspace.validateEdit(new IFile[] { fFile }, computationContext);
					if (fStatus.isOK())
						handleFileContentChanged(false, false);
				}

				if (fFile.isDerived(IResource.CHECK_ANCESTORS)) {
					IStatus status= new Status(IStatus.WARNING, FileBuffersPlugin.PLUGIN_ID, IFileBufferStatusCodes.DERIVED_FILE, FileBuffersMessages.ResourceFileBuffer_warning_fileIsDerived, null);
					if (fStatus == null || fStatus.isOK())
						fStatus= status;
					else
						fStatus= new MultiStatus(FileBuffersPlugin.PLUGIN_ID, IFileBufferStatusCodes.STATE_VALIDATION_FAILED, new IStatus[] {fStatus, status}, FileBuffersMessages.ResourceFileBuffer_stateValidationFailed, null);
				}

			} catch (RuntimeException x) {
				fManager.fireStateChangeFailed(this);
				throw x;
			}

			fIsStateValidated= fStatus == null || fStatus.getSeverity() != IStatus.CANCEL;
			fManager.fireStateValidationChanged(this, fIsStateValidated);
		}
	}

	@Override
	public boolean isStateValidated() {
		return fIsStateValidated;
	}

	@Override
	public void resetStateValidation() {
		if (fIsStateValidated) {
			fIsStateValidated= false;
			fManager.fireStateValidationChanged(this, fIsStateValidated);
		}
	}

	/**
	 * Sends out the notification that the file serving as document input has been moved.
	 *
	 * @param newLocation the path of the new location of the file
	 */
	protected void handleFileMoved(IPath newLocation) {
		fManager.fireUnderlyingFileMoved(this, newLocation);
	}

	/**
	 * Sends out the notification that the file serving as document input has been deleted.
	 */
	protected void handleFileDeleted() {
		fManager.fireUnderlyingFileDeleted(this);
	}

	/**
	 * Refreshes the given  file.
	 *
	 * @param monitor the progress monitor
	 */
	protected void refreshFile(IProgressMonitor monitor) {
		try {
			fFile.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		} catch (OperationCanceledException x) {
			// Ignore
		} catch (CoreException x) {
			handleCoreException(x);
		}
	}

	/**
	 * Defines the standard procedure to handle <code>CoreExceptions</code>. Exceptions
	 * are written to the plug-in log.
	 *
	 * @param exception the exception to be logged
	 */
	protected void handleCoreException(CoreException exception) {
		ILog log= FileBuffersPlugin.getDefault().getLog();
		log.log(exception.getStatus());
	}

	@Override
	public boolean isSynchronized() {
		if (fSynchronizationStamp == fFile.getModificationStamp() && fFile.isSynchronized(IResource.DEPTH_ZERO))
			return true;

		fSynchronizationStamp= IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;
		return false;
	}

	@Override
	public void requestSynchronizationContext() {
		++ fSynchronizationContextCount;
	}

	@Override
	public void releaseSynchronizationContext() {
		-- fSynchronizationContextCount;
	}

	@Override
	public boolean isSynchronizationContextRequested() {
		return fSynchronizationContextCount > 0;
	}

	@Override
	public boolean isCommitable() {
		IFileInfo info= fFileStore.fetchInfo();
		return info.exists() && !info.getAttribute(EFS.ATTRIBUTE_READ_ONLY);
	}

	@Override
	public void validationStateChanged(boolean validationState, IStatus status) {
		fIsStateValidated= validationState;
		fStatus= status;
	}
}
