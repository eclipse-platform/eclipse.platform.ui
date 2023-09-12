/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.editors.text;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.osgi.framework.Bundle;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.filebuffers.IFileBufferManager;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;

import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.source.IAnnotationModel;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.editors.text.NLSUtility;
import org.eclipse.ui.internal.editors.text.UISynchronizationContext;
import org.eclipse.ui.internal.editors.text.WorkspaceOperationRunner;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.part.FileEditorInput;

import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProviderExtension;
import org.eclipse.ui.texteditor.IDocumentProviderExtension2;
import org.eclipse.ui.texteditor.IDocumentProviderExtension3;
import org.eclipse.ui.texteditor.IDocumentProviderExtension4;
import org.eclipse.ui.texteditor.IDocumentProviderExtension5;
import org.eclipse.ui.texteditor.IElementStateListener;
import org.eclipse.ui.texteditor.IElementStateListenerExtension;
import org.eclipse.ui.texteditor.ISchedulingRuleProvider;


/**
 * Shared document provider specialized for {@link org.eclipse.core.resources.IFile} based domain elements.
 * A text file document provider can have a parent document provider to which
 * it may delegate calls i.e. instead of delegating work to a super class it
 * delegates to a parent document provider. The parent chain acts as chain
 * of command.
 * <p>
 * Text file document providers use {@linkplain org.eclipse.core.filebuffers.ITextFileBuffer text file buffers}
 * to access the file content. This allows to share it between various clients including
 * headless ones. Text file document providers should be preferred over file document
 * providers due to this advantage.
 * </p>
 * <p>
 * Use a {@linkplain org.eclipse.ui.editors.text.ForwardingDocumentProvider forwarding document provider}
 * if you need to ensure that all documents provided to clients are appropriately set up.
 * </p>
 * <p>
 * Clients can directly instantiate and configure this class with a suitable parent
 * document provider or provide their own subclass.
 * </p>
 *
 * @since 3.0
 */
public class TextFileDocumentProvider implements IDocumentProvider, IDocumentProviderExtension, IDocumentProviderExtension2, IDocumentProviderExtension3, IDocumentProviderExtension5, IStorageDocumentProvider, IDocumentProviderExtension4 {

	/**
	 * Operation created by the document provider and to be executed by the providers runnable context.
	 */
	protected static abstract class DocumentProviderOperation implements IRunnableWithProgress, ISchedulingRuleProvider {

		/**
		 * The actual functionality of this operation.
		 *
		 * @param monitor the progress monitor
		 * @throws CoreException if the execution fails
		 */
		protected abstract void execute(IProgressMonitor monitor) throws CoreException;

		@Override
		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			try {
				execute(monitor);
			} catch (CoreException x) {
				throw new InvocationTargetException(x);
			}
		}

		@Override
		public ISchedulingRule getSchedulingRule() {
			return ResourcesPlugin.getWorkspace().getRoot();
		}
	}

	/**
	 * @deprecated As of 3.3 - do not use
	 */
	@Deprecated
	static protected class NullProvider implements IDocumentProvider, IDocumentProviderExtension, IDocumentProviderExtension2, IDocumentProviderExtension3, IDocumentProviderExtension4, IDocumentProviderExtension5, IStorageDocumentProvider  {

		static final private IStatus STATUS_ERROR= new Status(IStatus.ERROR, EditorsUI.PLUGIN_ID, IStatus.OK, TextEditorMessages.NullProvider_error, null);

		@Override
		public void connect(Object element) throws CoreException {}
		@Override
		public void disconnect(Object element) {}
		@Override
		public IDocument getDocument(Object element) { return null; }
		@Override
		public void resetDocument(Object element) throws CoreException {}
		@Override
		public void saveDocument(IProgressMonitor monitor, Object element, IDocument document, boolean overwrite) throws CoreException {}
		@Override
		public long getModificationStamp(Object element) { return 0; }
		@Override
		public long getSynchronizationStamp(Object element) { return 0; }
		@Override
		public boolean isDeleted(Object element) { return true; }
		@Override
		public boolean mustSaveDocument(Object element) { return false; }
		@Override
		public boolean canSaveDocument(Object element) { return false; }
		@Override
		public IAnnotationModel getAnnotationModel(Object element) { return null; }
		@Override
		public void aboutToChange(Object element) {}
		@Override
		public void changed(Object element) {}
		@Override
		public void addElementStateListener(IElementStateListener listener) {}
		@Override
		public void removeElementStateListener(IElementStateListener listener) {}
		@Override
		public boolean isReadOnly(Object element) { return true; }
		@Override
		public boolean isModifiable(Object element) { return false; }
		@Override
		public void validateState(Object element, Object computationContext) throws CoreException {}
		@Override
		public boolean isStateValidated(Object element) { return true; }
		@Override
		public void updateStateCache(Object element) throws CoreException {}
		@Override
		public void setCanSaveDocument(Object element) {}
		@Override
		public IStatus getStatus(Object element) { return STATUS_ERROR; }
		@Override
		public void synchronize(Object element) throws CoreException {}
		@Override
		public void setProgressMonitor(IProgressMonitor progressMonitor) {}
		@Override
		public IProgressMonitor getProgressMonitor() { return new NullProgressMonitor(); }
		@Override
		public boolean isSynchronized(Object element) { return true; }
		@Override
		public boolean isNotSynchronizedException(Object element, CoreException ex) { return false; }
		@Override
		public String getDefaultEncoding() { return null; }
		@Override
		public String getEncoding(Object element) { return null; }
		@Override
		public void setEncoding(Object element, String encoding) {}
		@Override
		public IContentType getContentType(Object element) throws CoreException { return null; }
	}

	static protected class FileInfo  {
		public Object fElement;
		public int fCount;
		public ITextFileBuffer fTextFileBuffer;

		/**
		 * The file buffer location kind.
		 * @since 3.4
		 */
		public LocationKind fTextFileBufferLocationKind;

		public IAnnotationModel fModel;
		public boolean fCachedReadOnlyState;
	}

	static private class SingleElementIterator<E> implements Iterator<E> {

		private E fElement;

		public SingleElementIterator(E element) {
			fElement= element;
		}

		@Override
		public boolean hasNext() {
			return fElement != null;
		}

		@Override
		public E next() {
			if (fElement != null) {
				E result= fElement;
				fElement= null;
				return result;
			}
			throw new NoSuchElementException();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	protected class FileBufferListener implements IFileBufferListener  {

		public FileBufferListener()  {
		}

		@Override
		public void bufferContentAboutToBeReplaced(IFileBuffer file) {
			List<IElementStateListener> list= new ArrayList<>(fElementStateListeners);
			Iterator<IElementStateListener> e= list.iterator();
			while (e.hasNext()) {
				IElementStateListener l= e.next();
				Iterator<Object> i= getElements(file);
				while (i.hasNext())
					l.elementContentAboutToBeReplaced(i.next());
			}
		}

		@Override
		public void bufferContentReplaced(IFileBuffer file) {
			List<IElementStateListener> list= new ArrayList<>(fElementStateListeners);
			Iterator<IElementStateListener> e= list.iterator();
			while (e.hasNext()) {
				IElementStateListener l= e.next();
				Iterator<Object> i= getElements(file);
				while (i.hasNext())
					l.elementContentReplaced(i.next());
			}
		}

		@Override
		public void stateChanging(IFileBuffer file) {
			Iterator<Object> i= getElements(file);
			while (i.hasNext())
				fireElementStateChanging(i.next());
		}

		@Override
		public void dirtyStateChanged(IFileBuffer file, boolean isDirty) {
			List<IElementStateListener> list= new ArrayList<>(fElementStateListeners);
			Iterator<IElementStateListener> e= list.iterator();
			while (e.hasNext()) {
				IElementStateListener l= e.next();
				Iterator<Object> i= getElements(file);
				while (i.hasNext())
					l.elementDirtyStateChanged(i.next(), isDirty);
			}
		}

		@Override
		public void stateValidationChanged(IFileBuffer file, boolean isStateValidated) {
			List<IElementStateListener> list= new ArrayList<>(fElementStateListeners);
			Iterator<IElementStateListener> e= list.iterator();
			while (e.hasNext()) {
				Object l= e.next();
				if (l instanceof IElementStateListenerExtension) {
					IElementStateListenerExtension x= (IElementStateListenerExtension) l;
					Iterator<Object> i= getElements(file);
					while (i.hasNext())
						x.elementStateValidationChanged(i.next(), isStateValidated);
				}
			}
		}

		@Override
		public void underlyingFileMoved(IFileBuffer file, IPath newLocation) {
			IWorkspace workspace=ResourcesPlugin.getWorkspace();
			IFile newFile= workspace.getRoot().getFile(newLocation);
			IEditorInput input= new FileEditorInput(newFile);
			List<IElementStateListener> list= new ArrayList<>(fElementStateListeners);
			Iterator<IElementStateListener> e= list.iterator();
			while (e.hasNext()) {
				IElementStateListener l= e.next();
				Iterator<Object> i= getElements(file);
				while (i.hasNext())
					l.elementMoved(i.next(), input);
			}
		}

		@Override
		public void underlyingFileDeleted(IFileBuffer file) {
			List<IElementStateListener> list= new ArrayList<>(fElementStateListeners);
			Iterator<IElementStateListener> e= list.iterator();
			while (e.hasNext()) {
				IElementStateListener l= e.next();
				Iterator<Object> i= getElements(file);
				while (i.hasNext())
					l.elementDeleted(i.next());
			}
		}

		@Override
		public void stateChangeFailed(IFileBuffer file) {
			Iterator<Object> i= getElements(file);
			while (i.hasNext())
				fireElementStateChangeFailed(i.next());
		}

		@Override
		public void bufferCreated(IFileBuffer buffer) {
			// ignore
		}

		@Override
		public void bufferDisposed(IFileBuffer buffer) {
			// ignore
		}
	}

	/** The parent document provider. */
	private IDocumentProvider fParentProvider;
	/** Element information of all connected elements. */
	private final Map<Object, FileInfo> fFileInfoMap= new HashMap<>();
	/** Map from file buffers to their connected elements. Value is an Object or a {@code List<Object>}. */
	private final Map<ITextFileBuffer, Object> fFileBufferMap= new HashMap<>();
	/** The list of element state listeners. */
	private List<IElementStateListener> fElementStateListeners= new ArrayList<>();
	/** The file buffer listener. */
	private final IFileBufferListener fFileBufferListener= new FileBufferListener();
	/** The progress monitor. */
	private IProgressMonitor fProgressMonitor;
	/** The operation runner. */
	private WorkspaceOperationRunner fOperationRunner;
	/** The rule factory. */
	private IResourceRuleFactory fResourceRuleFactory;


	/**
	 * Creates a new text file document provider
	 * with no parent.
	 */
	public TextFileDocumentProvider()  {
		this(null);
	}

	/**
	 * Creates a new text file document provider
	 * which has the given parent provider.
	 *
	 * @param parentProvider the parent document provider
	 */
	public TextFileDocumentProvider(IDocumentProvider parentProvider) {
		IFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		manager.setSynchronizationContext(new UISynchronizationContext());
		if (parentProvider != null)
			setParentDocumentProvider(parentProvider);

		fResourceRuleFactory= ResourcesPlugin.getWorkspace().getRuleFactory();
	}

	/**
	 * Sets the given parent provider as this document
	 * provider's parent document provider.
	 *
	 * @param parentProvider the parent document provider
	 */
	public final void setParentDocumentProvider(IDocumentProvider parentProvider) {

		Assert.isTrue(parentProvider instanceof IDocumentProviderExtension);
		Assert.isTrue(parentProvider instanceof IDocumentProviderExtension2);
		Assert.isTrue(parentProvider instanceof IDocumentProviderExtension3);
		Assert.isTrue(parentProvider instanceof IStorageDocumentProvider);

		fParentProvider= parentProvider;
	}

	/**
	 * Returns the parent document provider.
	 *
	 * @return the parent document provider
	 */
	final protected  IDocumentProvider getParentProvider() {
		if (fParentProvider == null)
			fParentProvider= new StorageDocumentProvider();
		return fParentProvider;
	}

	/**
	 * Returns the runnable context for this document provider.
	 *
	 * @param monitor the progress monitor
	 * @return the runnable context for this document provider
	 */
	protected IRunnableContext getOperationRunner(IProgressMonitor monitor) {
		if (fOperationRunner == null)
			fOperationRunner = new WorkspaceOperationRunner();
		fOperationRunner.setProgressMonitor(monitor);
		return fOperationRunner;
	}

	/**
	 * Executes the given operation in the providers runnable context.
	 *
	 * @param operation the operation to be executes
	 * @param monitor the progress monitor
	 * @throws CoreException the operation's core exception
	 */
	protected void executeOperation(DocumentProviderOperation operation, IProgressMonitor monitor) throws CoreException {
		try {
			IRunnableContext runner= getOperationRunner(monitor);
			if (runner != null)
				runner.run(false, false, operation);
			else
				operation.run(monitor);
		} catch (InvocationTargetException x) {
			Throwable e= x.getTargetException();
			if (e instanceof CoreException)
				throw (CoreException) e;
			String message= (e.getMessage() != null ? e.getMessage() : ""); //$NON-NLS-1$
			throw new CoreException(new Status(IStatus.ERROR, EditorsUI.PLUGIN_ID, IStatus.OK, message, e));
		} catch (InterruptedException x) {
			String message= (x.getMessage() != null ? x.getMessage() : ""); //$NON-NLS-1$
			throw new CoreException(new Status(IStatus.CANCEL, EditorsUI.PLUGIN_ID, IStatus.OK, message, x));
		}
	}

	@Override
	public void connect(Object element) throws CoreException {
		FileInfo info= fFileInfoMap.get(element);
		if (info == null) {

			info= createFileInfo(element);
			if (info == null)  {
				getParentProvider().connect(element);
				return;
			}

			info.fElement= element;
			fFileInfoMap.put(element, info);
			storeFileBufferMapping(element, info);
		}
		++ info.fCount;
	}

	/**
	 * Updates the file buffer map with a new relation between the file buffer
	 * of the given info and the given element.
	 *
	 * @param element the element
	 * @param info the element's file info object
	 */
	private void storeFileBufferMapping(Object element, FileInfo info) {
		Object value= fFileBufferMap.get(info.fTextFileBuffer);

		if (value instanceof List) {
			@SuppressWarnings("unchecked")
			List<Object> list= (List<Object>) value;
			list.add(element);
			return;
		}

		if (value == null) {
			value= element;
		} else {
			List<Object> list= new ArrayList<>(2);
			list.add(value);
			list.add(element);

			value= list;
		}
		fFileBufferMap.put(info.fTextFileBuffer, value);
	}

	/**
	 * Creates and returns a new and empty file info object.
	 * <p>
	 * Subclasses which extend {@link org.eclipse.ui.editors.text.TextFileDocumentProvider.FileInfo}
	 * should override this method.
	 * </p>
	 *
	 * @return a new and empty object of type <code>FileInfo</code>
	 */
	protected FileInfo createEmptyFileInfo()  {
		return new FileInfo();
	}

	/**
	 * Creates and returns the file info object
	 * for the given element.
	 * <p>
	 * Subclasses which extend {@link org.eclipse.ui.editors.text.TextFileDocumentProvider.FileInfo}
	 * will probably have to extend this method as well.
	 * </p>
	 *
	 * @param element the element
	 * @return a file info object of type <code>FileInfo</code>
	 * 			 or <code>null</code> if none can be created
	 * @throws CoreException if the file info object could not successfully be created
	 */
	protected FileInfo createFileInfo(Object element) throws CoreException {
		if (!(element instanceof IAdaptable))
			return null;
		IAdaptable adaptable= (IAdaptable) element;

		IFile file= null;
		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		ITextFileBuffer fileBuffer= null;
		LocationKind locationKind= null;

		file= adaptable.getAdapter(IFile.class);
		IProgressMonitor progressMonitor= getProgressMonitor();
		try {
			if (file != null) {
				IPath location= file.getFullPath();
				locationKind= LocationKind.IFILE;
				manager.connect(location, locationKind, progressMonitor);
				fileBuffer= manager.getTextFileBuffer(location, locationKind);
			} else {
				ILocationProvider provider= adaptable.getAdapter(ILocationProvider.class);
				if (provider instanceof ILocationProviderExtension) {
					URI uri= ((ILocationProviderExtension) provider).getURI(element);
					if (ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(uri).length == 0) {
						IFileStore fileStore= EFS.getStore(uri);
						manager.connectFileStore(fileStore, progressMonitor);
						fileBuffer= manager.getFileStoreTextFileBuffer(fileStore);
					}
				}
				if (fileBuffer == null && provider != null) {
					IPath location= provider.getPath(element);
					if (location == null)
						return null;
					locationKind= LocationKind.NORMALIZE;
					manager.connect(location, locationKind, progressMonitor);
					fileBuffer= manager.getTextFileBuffer(location, locationKind);
					file= FileBuffers.getWorkspaceFileAtLocation(location);
				}
			}
		} finally {
			if (progressMonitor != null) {
				progressMonitor.done();
			}
		}

		if (fileBuffer != null) {
			fileBuffer.requestSynchronizationContext();

			FileInfo info= createEmptyFileInfo();
			info.fTextFileBuffer= fileBuffer;
			info.fTextFileBufferLocationKind= locationKind;
			info.fCachedReadOnlyState= isSystemFileReadOnly(info);

			if (file != null)
				info.fModel= createAnnotationModel(file);

			if (info.fModel == null)
				info.fModel= info.fTextFileBuffer.getAnnotationModel();

			setUpSynchronization(info);

			return info;
		}
		return null;
	}

	/**
	 * Sets up the synchronization for the document
	 * and the annotation mode.
	 *
	 * @param info the file info
	 * @since 3.2
	 */
	protected void setUpSynchronization(FileInfo info) {
		if (info == null || info.fTextFileBuffer == null)
			return;

		IDocument document= info.fTextFileBuffer.getDocument();
		IAnnotationModel model= info.fModel;

		if (document instanceof ISynchronizable) {
			Object lock= ((ISynchronizable)document).getLockObject();
			if (lock == null) {
				lock= new Object();
				((ISynchronizable)document).setLockObject(lock);
			}
			if (model instanceof ISynchronizable)
				((ISynchronizable) model).setLockObject(lock);
		}
	}

	/**
	 * Creates and returns the annotation model for the given file.
	 *
	 * @param file the file
	 * @return the file's annotation model or <code>null</code> if none
	 */
	protected IAnnotationModel createAnnotationModel(IFile file) {
		return null;
	}

	@Override
	public void disconnect(Object element) {
		FileInfo info= fFileInfoMap.get(element);

		if (info == null)  {
			getParentProvider().disconnect(element);
			return;
		}

		if (info.fCount == 1) {

			fFileInfoMap.remove(element);
			removeFileBufferMapping(element, info);
			disposeFileInfo(element, info);

		} else
			-- info.fCount;
	}

	/**
	 * Removes the relation between the file buffer of the given info and the
	 * given element from the file buffer mapping.
	 *
	 * @param element the element
	 * @param info the element's file info object
	 */
	private void removeFileBufferMapping(Object element, FileInfo info) {
		Object value= fFileBufferMap.get(info.fTextFileBuffer);
		if (value == null)
			return;

		if (value instanceof List) {
			List<?> list= (List<?>) value;
			list.remove(element);
			if (list.size() == 1)
				fFileBufferMap.put(info.fTextFileBuffer, list.get(0));
		} else if (value == element) {
			fFileBufferMap.remove(info.fTextFileBuffer);
		}
	}

	/**
	 * Releases all resources described by given element's info object.
	 * <p>
	 * Subclasses which extend {@link org.eclipse.ui.editors.text.TextFileDocumentProvider.FileInfo}
	 * will probably have to extend this method as well.
	 * </p>
	 *
	 * @param element the element
	 * @param info the element's file info object
	 */
	protected void disposeFileInfo(Object element, FileInfo info) {
		IFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		IProgressMonitor progressMonitor= getProgressMonitor();
		try {
			info.fTextFileBuffer.releaseSynchronizationContext();
			if (info.fTextFileBufferLocationKind != null)
				manager.disconnect(info.fTextFileBuffer.getLocation(), info.fTextFileBufferLocationKind, progressMonitor);
			else
				manager.disconnectFileStore(info.fTextFileBuffer.getFileStore(), progressMonitor);
		} catch (CoreException x) {
			handleCoreException(x, "FileDocumentProvider.disposeElementInfo"); //$NON-NLS-1$
		} finally {
			if (progressMonitor != null) {
				progressMonitor.done();
			}
		}
	}

	/**
	 * Returns an iterator for all the elements that are connected to this file buffer.
	 *
	 * @param file the file buffer
	 * @return an iterator for all elements connected with the given file buffer
	 */
	protected Iterator<Object> getElements(IFileBuffer file) {
		Object value= fFileBufferMap.get(file);
		if (value instanceof List)
			return new ArrayList<Object>((List<?>) value).iterator();
		return new SingleElementIterator<>(value);
	}

	@Override
	public IDocument getDocument(Object element) {
		FileInfo info= fFileInfoMap.get(element);
		if (info != null)
			return info.fTextFileBuffer.getDocument();
		return getParentProvider().getDocument(element);
	}

	@Override
	public void resetDocument(Object element) throws CoreException {
		final FileInfo info= fFileInfoMap.get(element);
		if (info != null) {
			DocumentProviderOperation operation= new DocumentProviderOperation() {
				@Override
				protected void execute(IProgressMonitor monitor) throws CoreException {
					info.fTextFileBuffer.revert(monitor);

					if (info.fModel instanceof AbstractMarkerAnnotationModel) {
						AbstractMarkerAnnotationModel markerModel= (AbstractMarkerAnnotationModel) info.fModel;
						markerModel.resetMarkers();
					}
				}
				@Override
				public ISchedulingRule getSchedulingRule() {
					if (info.fElement instanceof IFileEditorInput) {
						IFileEditorInput input= (IFileEditorInput) info.fElement;
						return fResourceRuleFactory.refreshRule((input).getFile());
					}
					return null;
				}
			};
			IProgressMonitor progressMonitor= getProgressMonitor();
			try {
				executeOperation(operation, progressMonitor);
			} finally {
				if (progressMonitor != null) {
					progressMonitor.done();
				}
			}
		} else {
			getParentProvider().resetDocument(element);
		}
	}

	@Override
	public final void saveDocument(IProgressMonitor monitor, Object element, IDocument document, boolean overwrite) throws CoreException {

		if (element == null)
			return;

		DocumentProviderOperation operation= createSaveOperation(element, document, overwrite);
		if (operation != null)
			executeOperation(operation, monitor);
		else
			getParentProvider().saveDocument(monitor, element, document, overwrite);
	}

	protected DocumentProviderOperation createSaveOperation(final Object element, final IDocument document, final boolean overwrite) throws CoreException {
		final FileInfo info= fFileInfoMap.get(element);
		if (info != null) {

			if (info.fTextFileBuffer.getDocument() != document) {
				// the info exists, but not for the given document
				// -> saveAs was executed with a target that is already open
				// in another editor
				// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=85519
				Status status= new Status(IStatus.WARNING, EditorsUI.PLUGIN_ID, IStatus.OK, TextEditorMessages.TextFileDocumentProvider_saveAsTargetOpenInEditor, null);
				throw new CoreException(status);
			}

			return new DocumentProviderOperation() {
				@Override
				public void execute(IProgressMonitor monitor) throws CoreException {
					commitFileBuffer(monitor, info, overwrite);
				}
				@Override
				public ISchedulingRule getSchedulingRule() {
					if (info.fElement instanceof IFileEditorInput) {
						IFileEditorInput input= (IFileEditorInput) info.fElement;
						return computeSchedulingRule(input.getFile());
					}
					return null;
				}
			};

		} else if (element instanceof IFileEditorInput) {

			final IFile file= ((IFileEditorInput) element).getFile();
			return new DocumentProviderOperation() {
				@Override
				public void execute(IProgressMonitor monitor) throws CoreException {
					createFileFromDocument(monitor, file, document);
				}
				@Override
				public ISchedulingRule getSchedulingRule() {
					return computeSchedulingRule(file);
				}
			};
		} else if (element instanceof IURIEditorInput) {
			final URI uri= ((IURIEditorInput)element).getURI();
			return new DocumentProviderOperation() {
				@Override
				public void execute(IProgressMonitor monitor) throws CoreException {
					createFileStoreFromDocument(monitor, uri, document);
				}
				@Override
				public ISchedulingRule getSchedulingRule() {
					return null;
				}
			};
		}

		return null;
	}

	/**
	 * Commits the given file info's file buffer by changing the contents
	 * of the underlying file to the contents of this file buffer. After that
	 * call, <code>isDirty</code> returns <code>false</code> and <code>isSynchronized</code>
	 * returns <code>true</code>.
	 *
	 * @param monitor the progress monitor
	 * @param info the element's file info object
	 * @param overwrite indicates whether the underlying file should be overwritten if it is not synchronized with the file system
	 * @throws CoreException if writing or accessing the underlying file fails
	 */
	protected void commitFileBuffer(IProgressMonitor monitor, FileInfo info, boolean overwrite) throws CoreException {
		Assert.isNotNull(info);

		/* https://bugs.eclipse.org/bugs/show_bug.cgi?id=98327
		 * Make sure file gets saved in commit() if the underlying file has been deleted */
		if (info.fElement instanceof IFileEditorInput) {
			IFileEditorInput input= (IFileEditorInput) info.fElement;
			IResource resource= input.getFile();
			if (!resource.isSynchronized(IResource.DEPTH_ZERO) && isDeleted(input))
					info.fTextFileBuffer.setDirty(true);
		}


		info.fTextFileBuffer.commit(monitor, overwrite);
		if (info.fModel instanceof AbstractMarkerAnnotationModel) {
			AbstractMarkerAnnotationModel model= (AbstractMarkerAnnotationModel) info.fModel;
			model.updateMarkers(info.fTextFileBuffer.getDocument());
		}
	}

	/**
	 * Creates the given file with the given document content.
	 *
	 * @param monitor the progress monitor
	 * @param file the file to be created
	 * @param document the document to be written to the file
	 * @throws CoreException if the creation of the file fails
	 */
	protected void createFileFromDocument(IProgressMonitor monitor, IFile file, IDocument document) throws CoreException {
		try {
			monitor.beginTask(TextEditorMessages.TextFileDocumentProvider_beginTask_saving, 2000);
			ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
			manager.connect(file.getFullPath(), LocationKind.IFILE, monitor);
			ITextFileBuffer buffer= ITextFileBufferManager.DEFAULT.getTextFileBuffer(file.getFullPath(), LocationKind.IFILE);
			buffer.getDocument().set(document.get());
			buffer.commit(monitor, true);
			manager.disconnect(file.getFullPath(), LocationKind.IFILE, monitor);
		} finally {
			monitor.done();
		}
	}

	/**
	 * Creates the given file store with the given document content.
	 *
	 * @param monitor the progress monitor
	 * @param uri the location where the file store should be created
	 * @param document the document to be written to the file store
	 * @throws CoreException if the creation of the file store fails
	 * @since 3.3
	 */
	private void createFileStoreFromDocument(IProgressMonitor monitor, URI uri, IDocument document) throws CoreException {
		try {
			monitor.beginTask(TextEditorMessages.TextFileDocumentProvider_beginTask_saving, 2000);
			IFileStore fileStore= EFS.getStore(uri);
			FileBuffers.getTextFileBufferManager().connectFileStore(fileStore, monitor);
			ITextFileBuffer buffer= FileBuffers.getTextFileBufferManager().getFileStoreTextFileBuffer(fileStore);
			buffer.getDocument().set(document.get());
			buffer.commit(monitor, true);
			FileBuffers.getTextFileBufferManager().disconnectFileStore(fileStore, monitor);
		} finally {
			monitor.done();
		}
	}

	@Override
	public long getModificationStamp(Object element) {
		FileInfo info= fFileInfoMap.get(element);
		if (info != null)
			return info.fTextFileBuffer.getModificationStamp();
		return getParentProvider().getModificationStamp(element);
	}

	@Override
	public long getSynchronizationStamp(Object element) {
		FileInfo info= fFileInfoMap.get(element);
		if (info != null)
			return 0;
		return getParentProvider().getSynchronizationStamp(element);
	}

	@Override
	public boolean isDeleted(Object element) {
		FileInfo info= fFileInfoMap.get(element);
		if (info != null)  {
			IFileStore fileStore= getFileStore(info);
			return fileStore == null ? true : !fileStore.fetchInfo().exists();
		}
		return getParentProvider().isDeleted(element);
	}

	@Override
	public boolean mustSaveDocument(Object element) {
		FileInfo info= fFileInfoMap.get(element);
		if (info != null)
			return (info.fCount == 1) && info.fTextFileBuffer.isDirty();
		return getParentProvider().mustSaveDocument(element);
	}

	@Override
	public boolean canSaveDocument(Object element) {
		FileInfo info= fFileInfoMap.get(element);
		if (info != null)
			return info.fTextFileBuffer.isDirty();
		return getParentProvider().canSaveDocument(element);
	}

	@Override
	public IAnnotationModel getAnnotationModel(Object element) {
		FileInfo info= fFileInfoMap.get(element);
		if (info != null)
			return info.fModel;
		return getParentProvider().getAnnotationModel(element);
	}

	@Override
	public void aboutToChange(Object element) {
		FileInfo info= fFileInfoMap.get(element);
		if (info == null)
			getParentProvider().aboutToChange(element);
	}

	@Override
	public void changed(Object element) {
		FileInfo info= fFileInfoMap.get(element);
		if (info == null)
			getParentProvider().changed(element);
	}

	@Override
	public void addElementStateListener(IElementStateListener listener) {
		Assert.isNotNull(listener);
		if (!fElementStateListeners.contains(listener)) {
			fElementStateListeners.add(listener);
			if (fElementStateListeners.size() == 1) {
				IFileBufferManager manager= FileBuffers.getTextFileBufferManager();
				manager.addFileBufferListener(fFileBufferListener);
			}
		}
		getParentProvider().addElementStateListener(listener);
	}

	@Override
	public void removeElementStateListener(IElementStateListener listener) {
		Assert.isNotNull(listener);
		fElementStateListeners.remove(listener);
		if (fElementStateListeners.isEmpty()) {
			IFileBufferManager manager= FileBuffers.getTextFileBufferManager();
			manager.removeFileBufferListener(fFileBufferListener);
		}
		getParentProvider().removeElementStateListener(listener);
	}

	@Override
	public boolean isReadOnly(Object element) {
		FileInfo info= fFileInfoMap.get(element);
		if (info != null)
			return info.fCachedReadOnlyState;
		return ((IDocumentProviderExtension) getParentProvider()).isReadOnly(element);
	}

	@Override
	public boolean isModifiable(Object element) {
		FileInfo info= fFileInfoMap.get(element);
		if (info != null)
			return info.fTextFileBuffer.isStateValidated() ? !isReadOnly(element) : true;
		return ((IDocumentProviderExtension) getParentProvider()).isModifiable(element);
	}

	@Override
	public void validateState(Object element, final Object computationContext) throws CoreException {
		final FileInfo info= fFileInfoMap.get(element);
		if (info != null) {
			DocumentProviderOperation operation= new DocumentProviderOperation() {
				@Override
				protected void execute(IProgressMonitor monitor) throws CoreException {
					info.fTextFileBuffer.validateState(monitor, computationContext);
				}
				@Override
				public ISchedulingRule getSchedulingRule() {
					if (info.fElement instanceof IFileEditorInput) {
						IFileEditorInput input= (IFileEditorInput) info.fElement;
						IFile file= input.getFile();
						ISchedulingRule validateEditRule= fResourceRuleFactory.validateEditRule(new IResource[] { file });
						if (validateEditRule == null) {
							// Note that factory decides to provide a null rule for modifiable files (not read-only).
							// Null rule means, that org.eclipse.core.internal.resources.WorkManager.checkIn(ISchedulingRule, IProgressMonitor)
							// will run jobManager.beginRule(null, monitor); which will NOT show any progress dialog
							// and will *immediately* lock UI thread via lock.acquire(); while the workspace is locked
							// Providing here a file we enforce the progress dialog, where this operation can be cancelled by user,
							// so that an occasional "Modify" or "Save" of the editor will NOT block UI forever.
							return file;
						} else {
							return validateEditRule;
						}
					}
					return null;
				}
			};
			IProgressMonitor progressMonitor= getProgressMonitor();
			try {
				executeOperation(operation, progressMonitor);
			} finally {
				if (progressMonitor != null) {
					progressMonitor.done();
				}
			}
		} else
			((IDocumentProviderExtension) getParentProvider()).validateState(element, computationContext);
	}

	@Override
	public boolean isStateValidated(Object element) {
		FileInfo info= fFileInfoMap.get(element);
		if (info != null)
			return info.fTextFileBuffer.isStateValidated();
		return ((IDocumentProviderExtension) getParentProvider()).isStateValidated(element);
	}

	@Override
	public void updateStateCache(Object element) throws CoreException {
		FileInfo info= fFileInfoMap.get(element);
		if (info != null) {
			boolean isReadOnly= isSystemFileReadOnly(info);
			// See http://bugs.eclipse.org/bugs/show_bug.cgi?id=14469 for the dirty bit check
			// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=50699 for commenting that out
			if (!info.fCachedReadOnlyState && isReadOnly /*&& !info.fTextFileBuffer.isDirty()*/)
				info.fTextFileBuffer.resetStateValidation();
			info.fCachedReadOnlyState= isReadOnly;
		} else {
			((IDocumentProviderExtension) getParentProvider()).updateStateCache(element);
		}
	}

	@Override
	public void setCanSaveDocument(Object element) {
		FileInfo info= fFileInfoMap.get(element);
		if (info == null)
			((IDocumentProviderExtension) getParentProvider()).setCanSaveDocument(element);
	}

	@Override
	public IStatus getStatus(Object element) {
		FileInfo info= fFileInfoMap.get(element);
		if (info == null)
			return ((IDocumentProviderExtension) getParentProvider()).getStatus(element);

		IStatus status= info.fTextFileBuffer.getStatus();

		if (status.getCode() == IResourceStatus.OUT_OF_SYNC_LOCAL) {
			String message= status.getMessage();
			IBindingService bindingService= PlatformUI.getWorkbench().getService(IBindingService.class);
			String keySequence= bindingService.getBestActiveBindingFormattedFor(IWorkbenchCommandConstants.FILE_REFRESH);
			if (keySequence != null)
				message= message + NLSUtility.format(TextEditorMessages.TextFileDocumentProvider_error_outOfSyncHintWithKeyBinding, keySequence);
			else
				message= message + TextEditorMessages.TextFileDocumentProvider_error_outOfSyncHint;
			return new Status(status.getSeverity(), status.getPlugin(), status.getCode(), message, status.getException());
		}

		// Ensure that we don't open an empty document for an non-existent IFile
		if (status.getSeverity() != IStatus.ERROR && element instanceof IFileEditorInput) {
			IFile file= FileBuffers.getWorkspaceFileAtLocation(info.fTextFileBuffer.getLocation());
			if (file == null || !file.exists()) {
				String message= NLSUtility.format(TextEditorMessages.TextFileDocumentProvider_error_doesNotExist, ((IFileEditorInput)element).getFile().getFullPath());
				return new Status(IStatus.ERROR, EditorsUI.PLUGIN_ID, IResourceStatus.RESOURCE_NOT_FOUND, message, null);
			}
		}

		return status;
	}

	@Override
	public void synchronize(Object element) throws CoreException {
		final FileInfo info= fFileInfoMap.get(element);
		if (info != null) {
			DocumentProviderOperation operation= new DocumentProviderOperation() {
				@Override
				protected void execute(IProgressMonitor monitor) throws CoreException {
					info.fTextFileBuffer.revert(monitor);
				}
				@Override
				public ISchedulingRule getSchedulingRule() {
					if (info.fElement instanceof IFileEditorInput) {
						IFileEditorInput input= (IFileEditorInput) info.fElement;
						return fResourceRuleFactory.refreshRule(input.getFile());
					}
					return null;
				}
			};
			IProgressMonitor progressMonitor= getProgressMonitor();
			try {
				executeOperation(operation, progressMonitor);
			} finally {
				if (progressMonitor != null) {
					progressMonitor.done();
				}
			}
		} else {
			((IDocumentProviderExtension) getParentProvider()).synchronize(element);
		}
	}

	@Override
	public void setProgressMonitor(IProgressMonitor progressMonitor) {
		fProgressMonitor= progressMonitor;
		((IDocumentProviderExtension2) getParentProvider()).setProgressMonitor(progressMonitor);
	}

	@Override
	public IProgressMonitor getProgressMonitor() {
		return fProgressMonitor;
	}

	@Override
	public boolean isSynchronized(Object element) {
		FileInfo info= fFileInfoMap.get(element);
		if (info != null)
			return info.fTextFileBuffer.isSynchronized();
		return ((IDocumentProviderExtension3) getParentProvider()).isSynchronized(element);
	}

	@Override
	public boolean isNotSynchronizedException(Object element, CoreException ex) {
		IStatus status= ex.getStatus();
		if (status == null || status instanceof MultiStatus)
			return false;

		if (status.getException() != null)
			return false;

		return status.getCode() == IResourceStatus.OUT_OF_SYNC_LOCAL;
	}

	@Override
	public String getDefaultEncoding() {
		return FileBuffers.getTextFileBufferManager().getDefaultEncoding();
	}

	@Override
	public String getEncoding(Object element) {
		FileInfo info= fFileInfoMap.get(element);
		if (info != null)
			return info.fTextFileBuffer.getEncoding();
		return ((IStorageDocumentProvider) getParentProvider()).getEncoding(element);
	}

	@Override
	public void setEncoding(Object element, String encoding) {
		FileInfo info= fFileInfoMap.get(element);
		if (info != null)
			info.fTextFileBuffer.setEncoding(encoding);
		else
			((IStorageDocumentProvider) getParentProvider()).setEncoding(element, encoding);
	}

	@Override
	public IContentType getContentType(Object element) throws CoreException {
		FileInfo info= fFileInfoMap.get(element);
		if (info != null)
			return info.fTextFileBuffer.getContentType();
		IDocumentProvider parent= getParentProvider();
		if (parent instanceof IDocumentProviderExtension4)
			return ((IDocumentProviderExtension4) parent).getContentType(element);
		return null;
	}

	/**
	 * Defines the standard procedure to handle <code>CoreExceptions</code>. Exceptions
	 * are written to the plug-in log.
	 *
	 * @param exception the exception to be logged
	 * @param message the message to be logged
	 */
	protected void handleCoreException(CoreException exception, String message) {
		Bundle bundle = Platform.getBundle(PlatformUI.PLUGIN_ID);
		ILog log= ILog.of(bundle);
		IStatus status= message != null ? new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.OK, message, exception) : exception.getStatus();
		log.log(status);
	}

	/**
	 * Returns the file store denoted by the given info.
	 *
	 * @param info the element's file info object
	 * @return the {@link IFileStore} for the given file info
	 * @since 3.2
	 */
	protected IFileStore getFileStore(FileInfo info)  {
		return info.fTextFileBuffer.getFileStore();
	}

	/**
	 * Returns the system file denoted by the given info.
	 *
	 * @param info the element's file info object
	 * @return the system file for the given file info
	 * @deprecated As of 3.2, replaced by {@link #getFileStore(org.eclipse.ui.editors.text.TextFileDocumentProvider.FileInfo)}
	 */
	@Deprecated
	protected File getSystemFile(FileInfo info)  {
		IPath path= info.fTextFileBuffer.getLocation();
		return FileBuffers.getSystemFileAtLocation(path);
	}

	/**
	 * Returns whether the system file denoted by
	 * the given info is read-only.
	 *
	 * @param info the element's file info object
	 * @return <code>true</code> iff read-only
	 */
	protected boolean isSystemFileReadOnly(FileInfo info)  {
		IFileStore fileStore= getFileStore(info);
		if (fileStore == null)
			return false;
		IFileInfo fileInfo= fileStore.fetchInfo();
		return fileInfo.exists() && fileInfo.getAttribute(EFS.ATTRIBUTE_READ_ONLY);
	}

	/**
	 * Returns the file info object for the given element.
	 *
	 * @param element the element
	 * @return the file info object, or <code>null</code> if none
	 */
	protected FileInfo getFileInfo(Object element)  {
		return fFileInfoMap.get(element);
	}

	/**
	 * Returns an iterator over the elements connected via this document provider.
	 *
	 * @return an iterator over the list of elements
	 */
	protected Iterator<Object> getConnectedElementsIterator()  {
		return new HashSet<>(fFileInfoMap.keySet()).iterator();
	}

	/**
	 * Returns an iterator over this document provider's file info objects.
	 *
	 * @return the iterator over list of file info objects
	 */
	protected Iterator<FileInfo> getFileInfosIterator()  {
		return new ArrayList<>(fFileInfoMap.values()).iterator();
	}

	/**
	 * Informs all registered element state listeners
	 * about the current state change of the element.
	 *
	 * @param element the element
	 * @see IElementStateListenerExtension#elementStateChanging(Object)
	 */
	protected void fireElementStateChanging(Object element) {
		List<IElementStateListener> list= new ArrayList<>(fElementStateListeners);
		Iterator<IElementStateListener> e= list.iterator();
		while (e.hasNext()) {
			Object l= e.next();
			if (l instanceof IElementStateListenerExtension) {
				IElementStateListenerExtension x= (IElementStateListenerExtension) l;
				x.elementStateChanging(element);
			}
		}
	}

	/**
	 * Informs all registered element state listeners
	 * about the failed state change of the element.
	 *
	 * @param element the element
	 * @see IElementStateListenerExtension#elementStateChangeFailed(Object)
	 */
	protected void fireElementStateChangeFailed(Object element) {
		List<IElementStateListener> list= new ArrayList<>(fElementStateListeners);
		Iterator<IElementStateListener> e= list.iterator();
		while (e.hasNext()) {
			Object l= e.next();
			if (l instanceof IElementStateListenerExtension) {
				IElementStateListenerExtension x= (IElementStateListenerExtension) l;
				x.elementStateChangeFailed(element);
			}
		}
	}

	/**
	 * Computes the scheduling rule needed to create or modify a resource. If
	 * the resource exists, its modify rule is returned. If it does not, the
	 * resource hierarchy is iterated towards the workspace root to find the
	 * first parent of <code>toCreateOrModify</code> that exists. Then the
	 * 'create' rule for the last non-existing resource is returned.
	 *
	 * @param toCreateOrModify the resource to create or modify
	 * @return the minimal scheduling rule needed to modify or create a resource
	 * @since 3.1
	 */
	protected ISchedulingRule computeSchedulingRule(IResource toCreateOrModify) {
		if (toCreateOrModify.exists())
			return fResourceRuleFactory.modifyRule(toCreateOrModify);

		IResource parent= toCreateOrModify;
		do {
			toCreateOrModify= parent;
			parent= toCreateOrModify.getParent();
		} while (parent != null && !parent.exists());

		return fResourceRuleFactory.createRule(toCreateOrModify);
	}
}
