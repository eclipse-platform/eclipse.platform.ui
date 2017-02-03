/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Andrew Ferguson (Symbian) - [api] enable document setup participants to customize behavior based on resource being opened - https://bugs.eclipse.org/bugs/show_bug.cgi?id=208881
 *******************************************************************************/
package org.eclipse.core.internal.filebuffers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;

import org.eclipse.core.filebuffers.IAnnotationModelFactory;
import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.core.filebuffers.IDocumentSetupParticipantExtension;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.filebuffers.IFileBufferStatusCodes;
import org.eclipse.core.filebuffers.ISynchronizationContext;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.source.IAnnotationModel;


/**
 * @since 3.0
 */
public class TextFileBufferManager implements ITextFileBufferManager {

	private static abstract class SafeNotifier implements ISafeRunnable {
		@Override
		public void handleException(Throwable ex) {
			// NOTE: Logging is done by SafeRunner
		}
	}

	protected static final IContentType TEXT_CONTENT_TYPE= Platform.getContentTypeManager().getContentType(IContentTypeManager.CT_TEXT);

	private Map<IPath, AbstractFileBuffer> fFilesBuffers= new HashMap<>();
	private Map<IFileStore, FileStoreFileBuffer> fFileStoreFileBuffers= new HashMap<>();
	private List<IFileBufferListener> fFileBufferListeners= new ArrayList<>();
	protected ExtensionsRegistry fRegistry;
	private ISynchronizationContext fSynchronizationContext;


	public TextFileBufferManager()  {
		fRegistry= new ExtensionsRegistry();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated As of 3.3, replaced by {@link #connect(IPath, LocationKind, IProgressMonitor)}
	 */
	@Deprecated
	@Override
	public void connect(IPath location, IProgressMonitor monitor) throws CoreException {
		connect(location, LocationKind.NORMALIZE, monitor);
	}

	@Override
	public void connect(IPath location, LocationKind locationKind, IProgressMonitor monitor) throws CoreException {
		Assert.isNotNull(location);
		if (locationKind == LocationKind.NORMALIZE)
			location= normalizeLocation(location);

		AbstractFileBuffer fileBuffer= null;
		synchronized (fFilesBuffers) {
			fileBuffer= internalGetFileBuffer(location);
			if (fileBuffer != null)  {
				fileBuffer.connect();
				return;
			}
		}

		fileBuffer= createFileBuffer(location, locationKind);
		if (fileBuffer == null)
			throw new CoreException(new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IFileBufferStatusCodes.CREATION_FAILED, FileBuffersMessages.FileBufferManager_error_canNotCreateFilebuffer, null));

		fileBuffer.create(location, monitor);

		synchronized (fFilesBuffers) {
			AbstractFileBuffer oldFileBuffer= internalGetFileBuffer(location);
			if (oldFileBuffer != null) {
				fileBuffer.disconnect();
				fileBuffer.dispose();
				oldFileBuffer.connect();
				return;
			}
			fileBuffer.connect();
			fFilesBuffers.put(location, fileBuffer);
		}

		// Do notification outside synchronized block
		fireBufferCreated(fileBuffer);
	}

	@Override
	public void connectFileStore(IFileStore fileStore, IProgressMonitor monitor) throws CoreException {
		Assert.isLegal(fileStore != null);

		FileStoreFileBuffer fileBuffer= null;
		synchronized (fFileStoreFileBuffers) {
			fileBuffer= internalGetFileBuffer(fileStore);
			if (fileBuffer != null)  {
				fileBuffer.connect();
				return;
			}
		}

		fileBuffer= createFileBuffer(fileStore);
		if (fileBuffer == null)
			throw new CoreException(new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IFileBufferStatusCodes.CREATION_FAILED, FileBuffersMessages.FileBufferManager_error_canNotCreateFilebuffer, null));

		fileBuffer.create(fileStore, monitor);

		synchronized (fFileStoreFileBuffers) {
			AbstractFileBuffer oldFileBuffer= internalGetFileBuffer(fileStore);
			if (oldFileBuffer != null) {
				fileBuffer.disconnect();
				fileBuffer.dispose();
				oldFileBuffer.connect();
				return;
			}
			fileBuffer.connect();
			fFileStoreFileBuffers.put(fileStore, fileBuffer);
		}

		// Do notification outside synchronized block
		fireBufferCreated(fileBuffer);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated As of 3.3, replaced by {@link #disconnect(IPath, LocationKind, IProgressMonitor)}
	 */
	@Deprecated
	@Override
	public void disconnect(IPath location, IProgressMonitor monitor) throws CoreException {
		disconnect(location, LocationKind.NORMALIZE, monitor);
	}

	/*
	 * @since 3.3
	 */
	protected IPath normalizeLocation(IPath location) {
		return location;
	}

	@Override
	public void disconnect(IPath location, LocationKind locationKind, IProgressMonitor monitor) throws CoreException {
		Assert.isNotNull(location);
		if (locationKind == LocationKind.NORMALIZE)
			location= normalizeLocation(location);

		AbstractFileBuffer fileBuffer;
		synchronized (fFilesBuffers) {
			fileBuffer= internalGetFileBuffer(location);
			if (fileBuffer == null)
				return;

			fileBuffer.disconnect();
			if (!fileBuffer.isDisconnected())
				return;

			fFilesBuffers.remove(location);
		}

		// Do notification outside synchronized block
		fireBufferDisposed(fileBuffer);
		fileBuffer.dispose();
	}

	@Override
	public void disconnectFileStore(IFileStore fileStore, IProgressMonitor monitor) throws CoreException {
		Assert.isLegal(fileStore != null);

		AbstractFileBuffer fileBuffer;
		synchronized (fFileStoreFileBuffers) {
			fileBuffer= internalGetFileBuffer(fileStore);
			if (fileBuffer == null)
				return;

			fileBuffer.disconnect();
			if (!fileBuffer.isDisconnected())
				return;

			fFileStoreFileBuffers.remove(fileStore);
		}

		// Do notification outside synchronized block
		fireBufferDisposed(fileBuffer);
		fileBuffer.dispose();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated As of 3.2, replaced by {@link #isTextFileLocation(IPath, boolean)}
	 */
	@Deprecated
	@Override
	public boolean isTextFileLocation(IPath location) {
		return isTextFileLocation(location, false);
	}

	/**
	 * Returns whether a file store at the given location is or can be considered a
	 * text file. If the file store exists, the concrete content type of the file store is
	 * checked. If the concrete content type for the existing file store can not be
	 * determined, this method returns <code>!strict</code>. If the file store does
	 * not exist, it is checked whether a text content type is associated with
	 * the given location. If no content type is associated with the location,
	 * this method returns <code>!strict</code>.
	 * <p>
	 * The provided location is either a full path of a workspace resource or an
	 * absolute path in the local file system. The file buffer manager does not
	 * resolve the location of workspace resources in the case of linked
	 * resources.
	 * </p>
	 *
	 * @param fileStore	file store to check
	 * @param strict	<code>true</code> if a file with unknown content type
	 * 					is not treated as text file, <code>false</code> otherwise
	 * @return <code>true</code> if the location is a text file location
	 * @since 3.3
	 */
	protected boolean isTextFileLocation(IFileStore fileStore, boolean strict) {
		if (fileStore == null)
			return false;

		IContentTypeManager manager= Platform.getContentTypeManager();
		IFileInfo fileInfo= fileStore.fetchInfo();
		if (fileInfo.exists()) {

			try(InputStream is= fileStore.openInputStream(EFS.NONE, null)) {
				IContentDescription description= manager.getDescriptionFor(is, fileStore.getName(), IContentDescription.ALL);
				if (description != null) {
					IContentType type= description.getContentType();
					if (type != null)
						return type.isKindOf(TEXT_CONTENT_TYPE);
				}
			} catch (CoreException ex) {
				// ignore: API specification tells return true if content type can't be determined
			} catch (IOException ex) {
				// ignore: API specification tells return true if content type can't be determined
			}

			return !strict;

		}

		IContentType[] contentTypes= manager.findContentTypesFor(fileStore.getName());
		if (contentTypes != null && contentTypes.length > 0) {
			for (IContentType contentType : contentTypes)
				if (contentType.isKindOf(TEXT_CONTENT_TYPE))
					return true;
			return false;
		}
		return !strict;
	}

	@Override
	public boolean isTextFileLocation(IPath location, boolean strict) {
		Assert.isNotNull(location);
		location= normalizeLocation(location);
		try {
			return isTextFileLocation(EFS.getStore(URIUtil.toURI(location)), strict);
		} catch (CoreException ex) {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated As of 3.3, replaced by {@link #getFileBuffer(IPath, LocationKind)}
	 */
	@Deprecated
	@Override
	public IFileBuffer getFileBuffer(IPath location) {
		return getFileBuffer(location, LocationKind.NORMALIZE);
	}

	@Override
	public IFileBuffer getFileBuffer(IPath location, LocationKind locationKind) {
		if (locationKind == LocationKind.NORMALIZE)
			location= normalizeLocation(location);
		return internalGetFileBuffer(location);
	}

	@Override
	public IFileBuffer getFileStoreFileBuffer(IFileStore fileStore) {
		Assert.isLegal(fileStore != null);
		return internalGetFileBuffer(fileStore);
	}

	private AbstractFileBuffer internalGetFileBuffer(IPath location) {
		synchronized (fFilesBuffers) {
			return fFilesBuffers.get(location);
		}
	}

	private FileStoreFileBuffer internalGetFileBuffer(IFileStore fileStore) {
		synchronized (fFileStoreFileBuffers) {
			return fFileStoreFileBuffers.get(fileStore);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated As of 3.3, replaced by {@link #getTextFileBuffer(IPath, LocationKind)}
	 */
	@Deprecated
	@Override
	public ITextFileBuffer getTextFileBuffer(IPath location) {
		return getTextFileBuffer(location, LocationKind.NORMALIZE);
	}

	@Override
	public ITextFileBuffer getTextFileBuffer(IPath location, LocationKind locationKind) {
		return (ITextFileBuffer)getFileBuffer(location, locationKind);
	}

	@Override
	public ITextFileBuffer getFileStoreTextFileBuffer(IFileStore fileStore) {
		Assert.isLegal(fileStore != null);
		return (ITextFileBuffer)getFileStoreFileBuffer(fileStore);
	}

	@Override
	public ITextFileBuffer getTextFileBuffer(IDocument document) {
		Assert.isLegal(document != null);
		Iterator<AbstractFileBuffer> iter;
		synchronized (fFilesBuffers) {
			iter= new ArrayList<>(fFilesBuffers.values()).iterator();
		}

		while (iter.hasNext()) {
			Object buffer= iter.next();
			if (buffer instanceof ITextFileBuffer) {
				ITextFileBuffer textFileBuffer= (ITextFileBuffer)buffer;
				if (textFileBuffer.getDocument() == document) {
					if (!((AbstractFileBuffer)textFileBuffer).isDisconnected())
						return textFileBuffer;
					return null;
				}
			}
		}
		synchronized (fFileStoreFileBuffers) {
			iter= new ArrayList<AbstractFileBuffer>(fFileStoreFileBuffers.values()).iterator();
		}
		while (iter.hasNext()) {
			Object buffer= iter.next();
			if (buffer instanceof ITextFileBuffer) {
				ITextFileBuffer textFileBuffer= (ITextFileBuffer)buffer;
				if (textFileBuffer.getDocument() == document) {
					if (!((AbstractFileBuffer)textFileBuffer).isDisconnected())
						return textFileBuffer;
				}
			}
		}
		return null;
	}

	@Override
	public IFileBuffer[] getFileBuffers() {
		synchronized (fFilesBuffers) {
			Collection<AbstractFileBuffer> values= fFilesBuffers.values();
			return values.toArray(new IFileBuffer[values.size()]);
		}
	}

	@Override
	public IFileBuffer[] getFileStoreFileBuffers() {
		synchronized (fFileStoreFileBuffers) {
			Collection<FileStoreFileBuffer> values= fFileStoreFileBuffers.values();
			return values.toArray(new IFileBuffer[values.size()]);
		}
	}

	@Override
	public String getDefaultEncoding() {
		return System.getProperty("file.encoding"); //$NON-NLS-1$;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated As of 3.3, replaced by {@link #createEmptyDocument(IPath, LocationKind)}
	 */
	@Deprecated
	@Override
	public IDocument createEmptyDocument(IPath location) {
		return createEmptyDocument(location, LocationKind.NORMALIZE);
	}

	@Override
	public IDocument createEmptyDocument(final IPath location, final LocationKind locationKind) {
		IDocument documentFromFactory= createDocumentFromFactory(location, locationKind);
		final IDocument document;
		if (documentFromFactory != null)
			document= documentFromFactory;
		else
			document= new SynchronizableDocument();

		if (location == null)
			return document;

		// Set the initial line delimiter
		if (document instanceof IDocumentExtension4) {
			String initalLineDelimiter= getLineDelimiterPreference(location, locationKind);
			if (initalLineDelimiter != null)
				((IDocumentExtension4)document).setInitialLineDelimiter(initalLineDelimiter);
		}

		final IDocumentSetupParticipant[] participants= fRegistry.getDocumentSetupParticipants(location, locationKind);
		if (participants != null) {
			for (final IDocumentSetupParticipant participant : participants) {
				ISafeRunnable runnable= new ISafeRunnable() {
					@Override
					public void run() throws Exception {
						if (participant instanceof IDocumentSetupParticipantExtension)
							((IDocumentSetupParticipantExtension)participant).setup(document, location, locationKind);
						else
							participant.setup(document);

						if (document.getDocumentPartitioner() != null) {
							String message= NLSUtility.format(FileBuffersMessages.TextFileBufferManager_warning_documentSetupInstallsDefaultPartitioner, participant.getClass());
							IStatus status= new Status(IStatus.WARNING, FileBuffersPlugin.PLUGIN_ID, IStatus.OK, message, null);
							FileBuffersPlugin.getDefault().getLog().log(status);
						}
					}
					@Override
					public void handleException(Throwable t) {
						IStatus status= new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IStatus.OK, FileBuffersMessages.TextFileBufferManager_error_documentSetupFailed, t);
						FileBuffersPlugin.getDefault().getLog().log(status);
					}
				};
				SafeRunner.run(runnable);
			}
		}

		return document;
	}

	/**
	 * Helper to get rid of deprecation warnings.
	 *
	 * @param location the location of the file to be connected
	 * @param locationKind the kind of the given location
	 * @return the created empty document or <code>null</code> if none got created
	 * @since 3.5
	 * @deprecated As of 3.5
	 */
	@Deprecated
	private IDocument createDocumentFromFactory(final IPath location, final LocationKind locationKind) {
		final IDocument[] runnableResult= new IDocument[1];
		if (location != null) {
			final org.eclipse.core.filebuffers.IDocumentFactory factory= fRegistry.getDocumentFactory(location, locationKind);
			if (factory != null) {
				ISafeRunnable runnable= new ISafeRunnable() {
					@Override
					public void run() throws Exception {
						runnableResult[0]= factory.createDocument();
					}
					@Override
					public void handleException(Throwable t) {
						IStatus status= new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IStatus.OK, FileBuffersMessages.TextFileBufferManager_error_documentFactoryFailed, t);
						FileBuffersPlugin.getDefault().getLog().log(status);
					}
				};
				SafeRunner.run(runnable);
			}
		}
		return runnableResult[0];
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated As of 3.3, replaced by {@link #createAnnotationModel(IPath, LocationKind)}
	 */
	@Deprecated
	@Override
	public IAnnotationModel createAnnotationModel(IPath location) {
		return createAnnotationModel(location, LocationKind.NORMALIZE);
	}

	@Override
	public IAnnotationModel createAnnotationModel(IPath location, LocationKind locationKind) {
		Assert.isNotNull(location);
		IAnnotationModelFactory factory= fRegistry.getAnnotationModelFactory(location, locationKind);
		if (factory != null)
			return factory.createAnnotationModel(location);
		return null;
	}

	@Override
	public void addFileBufferListener(IFileBufferListener listener) {
		Assert.isNotNull(listener);
		synchronized (fFileBufferListeners) {
			if (!fFileBufferListeners.contains(listener))
				fFileBufferListeners.add(listener);
		}
	}

	@Override
	public void removeFileBufferListener(IFileBufferListener listener) {
		Assert.isNotNull(listener);
		synchronized (fFileBufferListeners) {
			fFileBufferListeners.remove(listener);
		}
	}

	@Override
	public void setSynchronizationContext(ISynchronizationContext context) {
		fSynchronizationContext= context;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated As of 3.1, replaced by
	 *             {@link org.eclipse.core.filebuffers.IFileBuffer#requestSynchronizationContext()}
	 */
	@Deprecated
	@Override
	public void requestSynchronizationContext(IPath location) {
		Assert.isNotNull(location);
		location= normalizeLocation(location);

		AbstractFileBuffer fileBuffer= internalGetFileBuffer(location);
		if (fileBuffer != null)
			fileBuffer.requestSynchronizationContext();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated As of 3.1, replaced by {@link IFileBuffer#releaseSynchronizationContext()}
	 */
	@Deprecated
	@Override
	public void releaseSynchronizationContext(IPath location) {
		Assert.isNotNull(location);
		location= normalizeLocation(location);

		AbstractFileBuffer fileBuffer= internalGetFileBuffer(location);
		if (fileBuffer != null)
			fileBuffer.releaseSynchronizationContext();
	}

	@Override
	public void execute(Runnable runnable) {
		if (fSynchronizationContext != null)
			fSynchronizationContext.run(runnable);
		else
			runnable.run();
	}


	private AbstractFileBuffer createFileBuffer(IPath location, LocationKind locationKind) {
		/*
		 * XXX: the following code is commented out for performance
		 * reasons and because we do not yet create a special binary
		 * file buffer.
		 */
//		if (isTextFileLocation(location, false))
//			return createTextFileBuffer(location);
//		return createBinaryFileBuffer(location, locationKind);
		return createTextFileBuffer(location, locationKind);
	}

	/**
	 * Creates a text file buffer for the given path.
	 *
	 * @param location the location of the file to be connected
	 * @param locationKind the kind of the given location
	 * @return the text file buffer
	 * @since 3.3
	 */
	protected AbstractFileBuffer createTextFileBuffer(IPath location, LocationKind locationKind) {
		Assert.isLegal(locationKind != LocationKind.IFILE);
		return new FileStoreTextFileBuffer(this);
	}

//	private AbstractFileBuffer createBinaryFileBuffer(IPath location, LocationKind locationKind) {
//		// XXX: should return a binary file buffer - using text file buffer for now
//		return createTextFileBuffer(location, locationKind);
//	}

	private FileStoreFileBuffer createFileBuffer(IFileStore location) {
		/*
		 * XXX: the following code is commented out for performance
		 * reasons and because we do not yet create a special binary
		 * file buffer.
		 */
//		if (isTextFileLocation(location, false))
//			return createTextFileBuffer(location);
//		return createBinaryFileBuffer(location);
		return createTextFileBuffer(location);

	}

	/**
	 * Creates a text file buffer for the given file store.
	 *
	 * @param location the file store
	 * @return the text file buffer
	 * @since 3.3
	 */
	protected FileStoreFileBuffer createTextFileBuffer(IFileStore location) {
		return new FileStoreTextFileBuffer(this);
	}

//	private FileStoreFileBuffer createBinaryFileBuffer(FileStore location) {
//		// XXX: should return a binary file buffer - using text file buffer for now
//		return createTextFileBuffer(location);
//	}

	private Iterator<IFileBufferListener> getFileBufferListenerIterator() {
		synchronized (fFileBufferListeners) {
			return new ArrayList<>(fFileBufferListeners).iterator();
		}
	}

	protected void fireDirtyStateChanged(final IFileBuffer buffer, final boolean isDirty) {
		Iterator<IFileBufferListener> e= getFileBufferListenerIterator();
		while (e.hasNext()) {
			final IFileBufferListener l= e.next();
			SafeRunner.run(new SafeNotifier() {
				@Override
				public void run() {
					l.dirtyStateChanged(buffer, isDirty);
				}
			});
		}
	}

	protected void fireBufferContentAboutToBeReplaced(final IFileBuffer buffer) {
		Iterator<IFileBufferListener> e= getFileBufferListenerIterator();
		while (e.hasNext()) {
			final IFileBufferListener l= e.next();
			SafeRunner.run(new SafeNotifier() {
				@Override
				public void run() {
					l.bufferContentAboutToBeReplaced(buffer);
				}
			});
		}
	}

	protected void fireBufferContentReplaced(final IFileBuffer buffer) {
		Iterator<IFileBufferListener> e= getFileBufferListenerIterator();
		while (e.hasNext()) {
			final IFileBufferListener l= e.next();
			SafeRunner.run(new SafeNotifier() {
				@Override
				public void run() {
					l.bufferContentReplaced(buffer);
				}
			});
		}
	}

	protected void fireUnderlyingFileMoved(final IFileBuffer buffer, final IPath target) {
		Iterator<IFileBufferListener> e= getFileBufferListenerIterator();
		while (e.hasNext()) {
			final IFileBufferListener l= e.next();
			SafeRunner.run(new SafeNotifier() {
				@Override
				public void run() {
					l.underlyingFileMoved(buffer, target);
				}
			});
		}
	}

	protected void fireUnderlyingFileDeleted(final IFileBuffer buffer) {
		Iterator<IFileBufferListener> e= getFileBufferListenerIterator();
		while (e.hasNext()) {
			final IFileBufferListener l= e.next();
			SafeRunner.run(new SafeNotifier() {
				@Override
				public void run() {
					l.underlyingFileDeleted(buffer);
				}
			});
		}
	}

	protected void fireStateValidationChanged(final IFileBuffer buffer, final boolean isStateValidated) {
		Iterator<IFileBufferListener> e= getFileBufferListenerIterator();
		while (e.hasNext()) {
			final IFileBufferListener l= e.next();
			SafeRunner.run(new SafeNotifier() {
				@Override
				public void run() {
					l.stateValidationChanged(buffer, isStateValidated);
				}
			});
		}
	}

	protected void fireStateChanging(final IFileBuffer buffer) {
		Iterator<IFileBufferListener> e= getFileBufferListenerIterator();
		while (e.hasNext()) {
			final IFileBufferListener l= e.next();
			SafeRunner.run(new SafeNotifier() {
				@Override
				public void run() {
					l.stateChanging(buffer);
				}
			});
		}
	}

	protected void fireStateChangeFailed(final IFileBuffer buffer) {
		Iterator<IFileBufferListener> e= getFileBufferListenerIterator();
		while (e.hasNext()) {
			final IFileBufferListener l= e.next();
			SafeRunner.run(new SafeNotifier() {
				@Override
				public void run() {
					l.stateChangeFailed(buffer);
				}
			});
		}
	}

	protected void fireBufferCreated(final IFileBuffer buffer) {
		Iterator<IFileBufferListener> e= getFileBufferListenerIterator();
		while (e.hasNext()) {
			final IFileBufferListener l= e.next();
			SafeRunner.run(new SafeNotifier() {
				@Override
				public void run() {
					l.bufferCreated(buffer);
				}
			});
		}
	}

	protected void fireBufferDisposed(final IFileBuffer buffer) {
		Iterator<IFileBufferListener> e= getFileBufferListenerIterator();
		while (e.hasNext()) {
			final IFileBufferListener l= e.next();
			SafeRunner.run(new SafeNotifier() {
				@Override
				public void run() {
					l.bufferDisposed(buffer);
				}
			});
		}
	}

	@Override
	public void validateState(final IFileBuffer[] fileBuffers, IProgressMonitor monitor, final Object computationContext) throws CoreException {
	}

	/**
	 * Returns the line delimiter to be used by the given location.
	 *
	 * @param location the location of the file to be connected
	 * @param locationKind the kind of the given location
	 * @return the line delimiter
	 * @since 3.3
	 */
	protected String getLineDelimiterPreference(IPath location, LocationKind locationKind) {
		return System.getProperty("line.separator"); //$NON-NLS-1$
	}

}
