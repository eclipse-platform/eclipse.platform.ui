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
package org.eclipse.core.internal.filebuffers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IAnnotationModelFactory;
import org.eclipse.core.filebuffers.IDocumentFactory;
import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.filebuffers.IFileBufferStatusCodes;
import org.eclipse.core.filebuffers.IStateValidationSupport;
import org.eclipse.core.filebuffers.ISynchronizationContext;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.source.IAnnotationModel;


/**
 * @since 3.0
 */
public class TextFileBufferManager implements ITextFileBufferManager {

	private static abstract class SafeNotifier implements ISafeRunnable {
		public void handleException(Throwable ex) {
			IStatus status= new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IStatus.OK, "TextFileBufferManager failed to notify an ITextFileBufferListener", ex);  //$NON-NLS-1$
			FileBuffersPlugin.getDefault().getLog().log(status);
		}
	}

	private Map fFilesBuffers= new HashMap();
	private List fFileBufferListeners= new ArrayList();
	private ExtensionsRegistry fRegistry;
	private ISynchronizationContext fSynchronizationContext;


	public TextFileBufferManager()  {
		fRegistry= new ExtensionsRegistry();
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBufferManager#connect(org.eclipse.core.runtime.IPath, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void connect(IPath location, IProgressMonitor monitor) throws CoreException {
		Assert.isNotNull(location);
		location= FileBuffers.normalizeLocation(location);

		AbstractFileBuffer fileBuffer= (AbstractFileBuffer) fFilesBuffers.get(location);
		if (fileBuffer == null)  {

			fileBuffer= createFileBuffer(location);
			if (fileBuffer == null)
				throw new CoreException(new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IFileBufferStatusCodes.CREATION_FAILED, FileBuffersMessages.FileBufferManager_error_canNotCreateFilebuffer, null));

			fileBuffer.create(location, monitor);
			fileBuffer.connect();
			fFilesBuffers.put(location, fileBuffer);
			fireBufferCreated(fileBuffer);

		} else {
			fileBuffer.connect();
		}
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBufferManager#disconnect(org.eclipse.core.runtime.IPath, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void disconnect(IPath location, IProgressMonitor monitor) throws CoreException {
		Assert.isNotNull(location);
		location= FileBuffers.normalizeLocation(location);

		AbstractFileBuffer fileBuffer= (AbstractFileBuffer) fFilesBuffers.get(location);
		if (fileBuffer != null) {
			fileBuffer.disconnect();
			if (fileBuffer.isDisconnected()) {
				fFilesBuffers.remove(location);
				fireBufferDisposed(fileBuffer);
				fileBuffer.dispose();
			}
		}
	}

	/*
	 * @see org.eclipse.core.filebuffers.ITextFileBufferManager#isTextFileLocation(org.eclipse.core.runtime.IPath)
	 */
	public boolean isTextFileLocation(IPath location) {
		return isTextFileLocation(location, false);
	}

	/*
	 * @see org.eclipse.core.filebuffers.ITextFileBufferManager#isTextFileLocation(org.eclipse.core.runtime.IPath, boolean)
	 * @since 3.2
	 */
	public boolean isTextFileLocation(IPath location, boolean strict) {
		Assert.isNotNull(location);
		location= FileBuffers.normalizeLocation(location);

		IContentTypeManager manager= Platform.getContentTypeManager();
		IContentType text= manager.getContentType(IContentTypeManager.CT_TEXT);

		IFile file= FileBuffers.getWorkspaceFileAtLocation(location);
		if (file != null) {
			if (file.exists()) {
				try {
					IContentDescription description= file.getContentDescription();
					if (description != null) {
						IContentType type= description.getContentType();
						if (type != null)
							return type.isKindOf(text);
					}
				} catch (CoreException x) {
					// ignore: API specification tells return true if content type can't be determined
				}
			} else {
				IContentType[] contentTypes= manager.findContentTypesFor(file.getName());
				if (contentTypes != null && contentTypes.length > 0) {
					for (int i= 0; i < contentTypes.length; i++)
						if (contentTypes[i].isKindOf(text))
							return true;
					return false;
				}
			}
			return !strict;
		}

		File externalFile= FileBuffers.getSystemFileAtLocation(location);
		if (externalFile != null) {
			if (externalFile.exists()) {
				FileInputStream is= null;
				try {
					is= new FileInputStream(externalFile);
					IContentDescription description= manager.getDescriptionFor(is, externalFile.getName(), IContentDescription.ALL);
					if (description != null) {
						IContentType type= description.getContentType();
						if (type != null)
							return type.isKindOf(text);
					}
				} catch (IOException ex) {
					// ignore: API specification tells return true if content type can't be determined
				} finally {
					if (is != null ) {
						try {
							is.close();
						} catch (IOException e) {
							// ignore: API specification tells to return true if content type can't be determined
						}
					}
				}

				return !strict;

			}

			IContentType[] contentTypes= manager.findContentTypesFor(externalFile.getName());
			if (contentTypes != null && contentTypes.length > 0) {
				for (int i= 0; i < contentTypes.length; i++)
					if (contentTypes[i].isKindOf(text))
						return true;
				return false;
			}
			return !strict;
		}

		return false;
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBufferManager#getFileBuffer(org.eclipse.core.runtime.IPath)
	 */
	public IFileBuffer getFileBuffer(IPath location) {
		location= FileBuffers.normalizeLocation(location);
		return (IFileBuffer) fFilesBuffers.get(location);
	}

	/*
	 * @see org.eclipse.core.filebuffers.ITextFileBufferManager#getTextFileBuffer(org.eclipse.core.runtime.IPath)
	 */
	public ITextFileBuffer getTextFileBuffer(IPath location) {
		location= FileBuffers.normalizeLocation(location);
		return (ITextFileBuffer) fFilesBuffers.get(location);
	}

	/*
	 * @see org.eclipse.core.buffer.text.IBufferedFileManager#getDefaultEncoding()
	 */
	public String getDefaultEncoding() {
		return ResourcesPlugin.getEncoding();
	}

	/*
	 * @see org.eclipse.core.filebuffers.ITextFileBufferManager#createEmptyDocument(org.eclipse.core.runtime.IPath)
	 */
	public IDocument createEmptyDocument(IPath location) {
		Assert.isNotNull(location);
		location= FileBuffers.normalizeLocation(location);

		final IDocument[] runnableResult= new IDocument[1];
		final IDocumentFactory factory= fRegistry.getDocumentFactory(location);
		if (factory != null) {
			ISafeRunnable runnable= new ISafeRunnable() {
				public void run() throws Exception {
					runnableResult[0]= factory.createDocument();
				}
				public void handleException(Throwable t) {
					IStatus status= new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IStatus.OK, FileBuffersMessages.TextFileBufferManager_error_documentFactoryFailed, t);
					FileBuffersPlugin.getDefault().getLog().log(status);
					if (t instanceof VirtualMachineError)
						throw (VirtualMachineError)t;
				}
			};
			Platform.run(runnable);
		}
		final IDocument document;
		if (runnableResult[0] != null)
			document= runnableResult[0];
		else
			document= new Document();
		
		// Set the initial line delimiter
		if (document instanceof IDocumentExtension4) {
			String initalLineDelimiter= getLineDelimiterPreference(location); 
			if (initalLineDelimiter != null)
				((IDocumentExtension4)document).setInitialLineDelimiter(initalLineDelimiter);
		}

		final IDocumentSetupParticipant[] participants= fRegistry.getDocumentSetupParticipants(location);
		if (participants != null) {
			for (int i= 0; i < participants.length; i++) {
				final IDocumentSetupParticipant participant= participants[i];
				ISafeRunnable runnable= new ISafeRunnable() {
					public void run() throws Exception {
						participant.setup(document);
					}
					public void handleException(Throwable t) {
						IStatus status= new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IStatus.OK, FileBuffersMessages.TextFileBufferManager_error_documentSetupFailed, t);
						FileBuffersPlugin.getDefault().getLog().log(status);
						if (t instanceof VirtualMachineError)
							throw (VirtualMachineError)t;
					}
				};
				Platform.run(runnable);
			}
		}

		return document;
	}

	/*
	 * @see org.eclipse.core.filebuffers.ITextFileBufferManager#createAnnotationModel(org.eclipse.core.runtime.IPath)
	 */
	public IAnnotationModel createAnnotationModel(IPath location) {
		Assert.isNotNull(location);
		location= FileBuffers.normalizeLocation(location);
		IAnnotationModelFactory factory= fRegistry.getAnnotationModelFactory(location);
		if (factory != null)
			return factory.createAnnotationModel(location);
		return null;
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBufferManager#addFileBufferListener(org.eclipse.core.filebuffers.IFileBufferListener)
	 */
	public void addFileBufferListener(IFileBufferListener listener) {
		Assert.isNotNull(listener);
		if (!fFileBufferListeners.contains(listener))
			fFileBufferListeners.add(listener);
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBufferManager#removeFileBufferListener(org.eclipse.core.filebuffers.IFileBufferListener)
	 */
	public void removeFileBufferListener(IFileBufferListener listener) {
		Assert.isNotNull(listener);
		fFileBufferListeners.remove(listener);
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBufferManager#setSynchronizationContext(org.eclipse.core.filebuffers.ISynchronizationContext)
	 */
	public void setSynchronizationContext(ISynchronizationContext context) {
		fSynchronizationContext= context;
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBufferManager#requestSynchronizationContext(org.eclipse.core.runtime.IPath)
	 */
	public void requestSynchronizationContext(IPath location) {
		Assert.isNotNull(location);
		location= FileBuffers.normalizeLocation(location);

		AbstractFileBuffer fileBuffer= (AbstractFileBuffer) fFilesBuffers.get(location);
		if (fileBuffer != null)
			fileBuffer.requestSynchronizationContext();
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBufferManager#releaseSynchronizationContext(org.eclipse.core.runtime.IPath)
	 */
	public void releaseSynchronizationContext(IPath location) {
		Assert.isNotNull(location);
		location= FileBuffers.normalizeLocation(location);

		AbstractFileBuffer fileBuffer= (AbstractFileBuffer) fFilesBuffers.get(location);
		if (fileBuffer != null)
			fileBuffer.releaseSynchronizationContext();
	}

	/**
	 * Executes the given runnable in the synchronization context of this file buffer manager.
	 * If there is no synchronization context connected with this manager, the runnable is
	 * directly executed.
	 *
	 * @param runnable the runnable to be executed
	 * @param requestSynchronizationContext <code>true</code> if the synchronization context is requested for the execution
	 */
	public void execute(Runnable runnable, boolean requestSynchronizationContext) {
		if (requestSynchronizationContext && fSynchronizationContext != null)
			fSynchronizationContext.run(runnable);
		else
			runnable.run();
	}

	private AbstractFileBuffer createFileBuffer(IPath location) {
		if (isTextFileLocation(location, false)) {
			if (FileBuffers.getWorkspaceFileAtLocation(location) != null)
				return new ResourceTextFileBuffer(this);
			return new JavaTextFileBuffer(this);
		}
		return null;
	}

	protected void fireDirtyStateChanged(final IFileBuffer buffer, final boolean isDirty) {
		Iterator e= new ArrayList(fFileBufferListeners).iterator();
		while (e.hasNext()) {
			final IFileBufferListener l= (IFileBufferListener) e.next();
			Platform.run(new SafeNotifier() {
				public void run() {
					l.dirtyStateChanged(buffer, isDirty);
				}
			});
		}
	}

	protected void fireBufferContentAboutToBeReplaced(final IFileBuffer buffer) {
		Iterator e= new ArrayList(fFileBufferListeners).iterator();
		while (e.hasNext()) {
			final IFileBufferListener l= (IFileBufferListener) e.next();
			Platform.run(new SafeNotifier() {
				public void run() {
					l.bufferContentAboutToBeReplaced(buffer);
				}
			});
		}
	}

	protected void fireBufferContentReplaced(final IFileBuffer buffer) {
		Iterator e= new ArrayList(fFileBufferListeners).iterator();
		while (e.hasNext()) {
			final IFileBufferListener l= (IFileBufferListener) e.next();
			Platform.run(new SafeNotifier() {
				public void run() {
					l.bufferContentReplaced(buffer);
				}
			});
		}
	}

	protected void fireUnderlyingFileMoved(final IFileBuffer buffer, final IPath target) {
		Iterator e= new ArrayList(fFileBufferListeners).iterator();
		while (e.hasNext()) {
			final IFileBufferListener l= (IFileBufferListener) e.next();
			Platform.run(new SafeNotifier() {
				public void run() {
					l.underlyingFileMoved(buffer, target);
				}
			});
		}
	}

	protected void fireUnderlyingFileDeleted(final IFileBuffer buffer) {
		Iterator e= new ArrayList(fFileBufferListeners).iterator();
		while (e.hasNext()) {
			final IFileBufferListener l= (IFileBufferListener) e.next();
			Platform.run(new SafeNotifier() {
				public void run() {
					l.underlyingFileDeleted(buffer);
				}
			});
		}
	}

	protected void fireStateValidationChanged(final IFileBuffer buffer, final boolean isStateValidated) {
		Iterator e= new ArrayList(fFileBufferListeners).iterator();
		while (e.hasNext()) {
			final IFileBufferListener l= (IFileBufferListener) e.next();
			Platform.run(new SafeNotifier() {
				public void run() {
					l.stateValidationChanged(buffer, isStateValidated);
				}
			});
		}
	}

	protected void fireStateChanging(final IFileBuffer buffer) {
		Iterator e= new ArrayList(fFileBufferListeners).iterator();
		while (e.hasNext()) {
			final IFileBufferListener l= (IFileBufferListener) e.next();
			Platform.run(new SafeNotifier() {
				public void run() {
					l.stateChanging(buffer);
				}
			});
		}
	}

	protected void fireStateChangeFailed(final IFileBuffer buffer) {
		Iterator e= new ArrayList(fFileBufferListeners).iterator();
		while (e.hasNext()) {
			final IFileBufferListener l= (IFileBufferListener) e.next();
			Platform.run(new SafeNotifier() {
				public void run() {
					l.stateChangeFailed(buffer);
				}
			});
		}
	}

	protected void fireBufferCreated(final IFileBuffer buffer) {
		Iterator e= new ArrayList(fFileBufferListeners).iterator();
		while (e.hasNext()) {
			final IFileBufferListener l= (IFileBufferListener) e.next();
			Platform.run(new SafeNotifier() {
				public void run() {
					l.bufferCreated(buffer);
				}
			});
		}
	}

	protected void fireBufferDisposed(final IFileBuffer buffer) {
		Iterator e= new ArrayList(fFileBufferListeners).iterator();
		while (e.hasNext()) {
			final IFileBufferListener l= (IFileBufferListener) e.next();
			Platform.run(new SafeNotifier() {
				public void run() {
					l.bufferDisposed(buffer);
				}
			});
		}
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBufferManager#validateState(org.eclipse.core.filebuffers.IFileBuffer[], org.eclipse.core.runtime.IProgressMonitor, java.lang.Object)
	 * @since 3.1
	 */
	public void validateState(final IFileBuffer[] fileBuffers, IProgressMonitor monitor, final Object computationContext) throws CoreException {
		IWorkspaceRunnable runnable= new IWorkspaceRunnable() {
			public void run(IProgressMonitor progressMonitor) throws CoreException {
				IFileBuffer[] toValidate= findFileBuffersToValidate(fileBuffers);
				validationStateAboutToBeChanged(toValidate);
				try {
					IStatus status= validateEdit(toValidate, computationContext);
					validationStateChanged(toValidate, true, status);
				} catch (RuntimeException x) {
					validationStateChangedFailed(toValidate);
				}
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, computeValidateStateRule(fileBuffers), IWorkspace.AVOID_UPDATE, monitor);
	}

	private IFileBuffer[] findFileBuffersToValidate(IFileBuffer[] fileBuffers) {
		ArrayList list= new ArrayList();
		for (int i= 0; i < fileBuffers.length; i++) {
			if (!fileBuffers[i].isStateValidated())
				list.add(fileBuffers[i]);
		}
		return (IFileBuffer[]) list.toArray(new IFileBuffer[list.size()]);
	}

	private void validationStateAboutToBeChanged(IFileBuffer[] fileBuffers) {
		for (int i= 0; i < fileBuffers.length; i++) {
			if (fileBuffers[i] instanceof IStateValidationSupport) {
				IStateValidationSupport support= (IStateValidationSupport) fileBuffers[i];
				support.validationStateAboutToBeChanged();
			}
		}
	}

	private void validationStateChanged(IFileBuffer[] fileBuffers, boolean validationState, IStatus status) {
		for (int i= 0; i < fileBuffers.length; i++) {
			if (fileBuffers[i] instanceof IStateValidationSupport) {
				IStateValidationSupport support= (IStateValidationSupport) fileBuffers[i];
				support.validationStateChanged(validationState, status);
			}
		}
	}

	private void validationStateChangedFailed(IFileBuffer[] fileBuffers) {
		for (int i= 0; i < fileBuffers.length; i++) {
			if (fileBuffers[i] instanceof IStateValidationSupport) {
				IStateValidationSupport support= (IStateValidationSupport) fileBuffers[i];
				support.validationStateChangeFailed();
			}
		}
	}

	private IStatus validateEdit(IFileBuffer[] fileBuffers, Object computationContext) {
		ArrayList list= new ArrayList();
		for (int i= 0; i < fileBuffers.length; i++) {
			IFile file= getWorkspaceFile(fileBuffers[i]);
			if (file != null)
				list.add(file);
		}
		IFile[] files= new IFile[list.size()];
		list.toArray(files);
		return ResourcesPlugin.getWorkspace().validateEdit(files, computationContext);
	}

	private ISchedulingRule computeValidateStateRule(IFileBuffer[] fileBuffers) {
		ArrayList list= new ArrayList();
		for (int i= 0; i < fileBuffers.length; i++) {
			IResource resource= getWorkspaceFile(fileBuffers[i]);
			if (resource != null)
				list.add(resource);
		}
		IResource[] resources= new IResource[list.size()];
		list.toArray(resources);
		IResourceRuleFactory factory= ResourcesPlugin.getWorkspace().getRuleFactory();
		return factory.validateEditRule(resources);
	}

	private IFile getWorkspaceFile(IFileBuffer fileBuffer) {
		return FileBuffers.getWorkspaceFileAtLocation(fileBuffer.getLocation());
	}
	
	private String getLineDelimiterPreference(IPath location) {
		IFile file= FileBuffers.getWorkspaceFileAtLocation(location);
		IScopeContext[] scopeContext;
		if (file != null && file.getProject() != null) {
			// project preference
			scopeContext= new IScopeContext[] { new ProjectScope(file.getProject()) };
			String lineDelimiter= Platform.getPreferencesService().getString(Platform.PI_RUNTIME, Platform.PREF_LINE_SEPARATOR, null, scopeContext);
			if (lineDelimiter != null)
				return lineDelimiter;
		}
		// workspace preference
		scopeContext= new IScopeContext[] { new InstanceScope() };
		return Platform.getPreferencesService().getString(Platform.PI_RUNTIME, Platform.PREF_LINE_SEPARATOR, null, scopeContext);
	}
}
