/**********************************************************************
Copyright (c) 2000, 2003 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.ui.editors.text;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.osgi.framework.Bundle;

import org.eclipse.core.internal.filebuffers.ContainerGenerator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.filebuffers.IFileBufferManager;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;

import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.editors.text.UISynchronizationContext;
import org.eclipse.ui.internal.editors.text.WorkspaceOperationRunner;
import org.eclipse.ui.internal.texteditor.TextEditorPlugin;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProviderExtension;
import org.eclipse.ui.texteditor.IDocumentProviderExtension2;
import org.eclipse.ui.texteditor.IDocumentProviderExtension3;
import org.eclipse.ui.texteditor.IElementStateListener;
import org.eclipse.ui.texteditor.IElementStateListenerExtension;
import org.eclipse.ui.texteditor.ISchedulingRuleProvider;

/**
 * @since 3.0
 */
public class TextFileDocumentProvider implements IDocumentProvider, IDocumentProviderExtension, IDocumentProviderExtension2, IDocumentProviderExtension3, IStorageDocumentProvider {
	
	/**
	 * Operation created by the document provider and to be executed by the providers runnable context.
	 */
	protected static abstract class DocumentProviderOperation implements IRunnableWithProgress, ISchedulingRuleProvider {
		
		/**
		 * The actual functionality of this operation.
		 * 
		 * @param monitor the progress monitor
		 * @throws CoreException
		 */
		protected abstract void execute(IProgressMonitor monitor) throws CoreException;
		
		/*
		 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			try {
				execute(monitor);
			} catch (CoreException x) {
				throw new InvocationTargetException(x);
			}
		}
		
		/*
		 * @see org.eclipse.ui.texteditor.ISchedulingRuleProvider#getSchedulingRule()
		 */
		public ISchedulingRule getSchedulingRule() {
			return ResourcesPlugin.getWorkspace().getRoot();
		}
	}
	
	static protected class NullProvider implements IDocumentProvider, IDocumentProviderExtension, IDocumentProviderExtension2, IDocumentProviderExtension3, IStorageDocumentProvider  {
		
		static final private IStatus STATUS_ERROR= new Status(IStatus.ERROR, EditorsUI.PLUGIN_ID, IStatus.INFO, TextEditorMessages.getString("NullProvider.error"), null); //$NON-NLS-1$
		
		public void connect(Object element) throws CoreException {}
		public void disconnect(Object element) {}
		public IDocument getDocument(Object element) { return null; }
		public void resetDocument(Object element) throws CoreException {}
		public void saveDocument(IProgressMonitor monitor, Object element, IDocument document, boolean overwrite) throws CoreException {}
		public long getModificationStamp(Object element) { return 0; }
		public long getSynchronizationStamp(Object element) { return 0; }
		public boolean isDeleted(Object element) { return true; }
		public boolean mustSaveDocument(Object element) { return false; }
		public boolean canSaveDocument(Object element) { return false; }
		public IAnnotationModel getAnnotationModel(Object element) { return null; }
		public void aboutToChange(Object element) {}
		public void changed(Object element) {}
		public void addElementStateListener(IElementStateListener listener) {}
		public void removeElementStateListener(IElementStateListener listener) {}
		public boolean isReadOnly(Object element) { return true; }
		public boolean isModifiable(Object element) { return false; }
		public void validateState(Object element, Object computationContext) throws CoreException {}
		public boolean isStateValidated(Object element) { return true; }
		public void updateStateCache(Object element) throws CoreException {}
		public void setCanSaveDocument(Object element) {}
		public IStatus getStatus(Object element) { return STATUS_ERROR; }
		public void synchronize(Object element) throws CoreException {}
		public void setProgressMonitor(IProgressMonitor progressMonitor) {}
		public IProgressMonitor getProgressMonitor() { return new NullProgressMonitor(); }
		public boolean isSynchronized(Object element) { return true; }
		public String getDefaultEncoding() { return null; }
		public String getEncoding(Object element) { return null; }
		public void setEncoding(Object element, String encoding) {}
	}
	
	static protected class FileInfo  {
		public Object fElement;
		public int fCount;
		public ITextFileBuffer fTextFileBuffer;
		public IAnnotationModel fModel;
		public boolean fCachedReadOnlyState;
	}
	
	static private class SingleElementIterator implements Iterator {

		private Object fElement;
		
		public SingleElementIterator(Object element) {
			fElement= element;
		}
		
		/*
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return fElement != null;
		}

		/*
		 * @see java.util.Iterator#next()
		 */
		public Object next() {
			if (fElement != null) {
				Object result= fElement;
				fElement= null;
				return result;
			}
			throw new NoSuchElementException();
		}

		/*
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	protected class FileBufferListener implements IFileBufferListener  {
		
		public FileBufferListener()  {
		}
		
		/*
		 * @see org.eclipse.core.buffer.text.IBufferedFileListener#bufferContentAboutToBeReplaced(org.eclipse.core.buffer.text.IBufferedFile)
		 */
		public void bufferContentAboutToBeReplaced(IFileBuffer file) {
			List list= new ArrayList(fElementStateListeners);
			Iterator e= list.iterator();
			while (e.hasNext()) {
				IElementStateListener l= (IElementStateListener) e.next();
				Iterator i= getElements(file);
				while (i.hasNext())
					l.elementContentAboutToBeReplaced(i.next());
			}
		}

		/*
		 * @see org.eclipse.core.buffer.text.IBufferedFileListener#bufferContentReplaced(org.eclipse.core.buffer.text.IBufferedFile)
		 */
		public void bufferContentReplaced(IFileBuffer file) {
			List list= new ArrayList(fElementStateListeners);
			Iterator e= list.iterator();
			while (e.hasNext()) {
				IElementStateListener l= (IElementStateListener) e.next();
				Iterator i= getElements(file);
				while (i.hasNext())
					l.elementContentReplaced(i.next());
			}
		}

		/*
		 * @see org.eclipse.core.buffer.text.IBufferedFileListener#stateChanging(org.eclipse.core.buffer.text.IBufferedFile)
		 */
		public void stateChanging(IFileBuffer file) {
			Iterator i= getElements(file);
			while (i.hasNext())
				fireElementStateChanging(i.next());
		}

		/*
		 * @see org.eclipse.core.buffer.text.IBufferedFileListener#dirtyStateChanged(org.eclipse.core.buffer.text.IBufferedFile, boolean)
		 */
		public void dirtyStateChanged(IFileBuffer file, boolean isDirty) {
			List list= new ArrayList(fElementStateListeners);
			Iterator e= list.iterator();
			while (e.hasNext()) {
				IElementStateListener l= (IElementStateListener) e.next();
				Iterator i= getElements(file);
				while (i.hasNext())
					l.elementDirtyStateChanged(i.next(), isDirty);
			}
		}

		/*
		 * @see org.eclipse.core.buffer.text.IBufferedFileListener#stateValidationChanged(org.eclipse.core.buffer.text.IBufferedFile, boolean)
		 */
		public void stateValidationChanged(IFileBuffer file, boolean isStateValidated) {
			List list= new ArrayList(fElementStateListeners);
			Iterator e= list.iterator();
			while (e.hasNext()) {
				Object l= e.next();
				if (l instanceof IElementStateListenerExtension) {
					IElementStateListenerExtension x= (IElementStateListenerExtension) l;
					Iterator i= getElements(file);
					while (i.hasNext())
						x.elementStateValidationChanged(i.next(), isStateValidated);
				}
			}
		}

		/*
		 * @see org.eclipse.core.buffer.text.IBufferedFileListener#underlyingFileMoved(org.eclipse.core.buffer.text.IBufferedFile, org.eclipse.core.runtime.IPath)
		 */
		public void underlyingFileMoved(IFileBuffer file, IPath newLocation) {
			IWorkspace workspace=ResourcesPlugin.getWorkspace();
			IFile newFile= workspace.getRoot().getFile(newLocation);
			IEditorInput input= newFile == null ? null : new FileEditorInput(newFile);
			List list= new ArrayList(fElementStateListeners);
			Iterator e= list.iterator();
			while (e.hasNext()) {
				IElementStateListener l= (IElementStateListener) e.next();
				Iterator i= getElements(file);
				while (i.hasNext())
					l.elementMoved(i.next(), input);
			}
		}

		/*
		 * @see org.eclipse.core.buffer.text.IBufferedFileListener#underlyingFileDeleted(org.eclipse.core.buffer.text.IBufferedFile)
		 */
		public void underlyingFileDeleted(IFileBuffer file) {
			List list= new ArrayList(fElementStateListeners);
			Iterator e= list.iterator();
			while (e.hasNext()) {
				IElementStateListener l= (IElementStateListener) e.next();
				Iterator i= getElements(file);
				while (i.hasNext())
					l.elementDeleted(i.next());
			}
		}

		/*
		 * @see org.eclipse.core.buffer.text.IBufferedFileListener#stateChangeFailed(org.eclipse.core.buffer.text.IBufferedFile)
		 */
		public void stateChangeFailed(IFileBuffer file) {
			Iterator i= getElements(file);
			while (i.hasNext())
				fireElementStateChangeFailed(i.next());
		}

		/*
		 * @see org.eclipse.core.filebuffers.IFileBufferListener#bufferCreated(org.eclipse.core.filebuffers.IFileBuffer)
		 */
		public void bufferCreated(IFileBuffer buffer) {
			// ignore
		}

		/*
		 * @see org.eclipse.core.filebuffers.IFileBufferListener#bufferDisposed(org.eclipse.core.filebuffers.IFileBuffer)
		 */
		public void bufferDisposed(IFileBuffer buffer) {
			// ignore
		}
	}

	/** The parent document provider */
	private IDocumentProvider fParentProvider;
	/** Element information of all connected elements */
	private final Map fFileInfoMap= new HashMap();
	/** Map from file buffers to their connected elements */
	private final Map fFileBufferMap= new HashMap();
	/** The list of element state listeners */
	private List fElementStateListeners= new ArrayList();
	/** The file buffer listener */
	private final IFileBufferListener fFileBufferListener= new FileBufferListener();
	/** The progress monitor */
	private IProgressMonitor fProgressMonitor;
	/** The operation runner */
	private WorkspaceOperationRunner fOperationRunner;
	/** The rule factory */
	private IResourceRuleFactory fResourceRuleFactory;
	
	
	public TextFileDocumentProvider()  {
		this(null);
	}
	
	public TextFileDocumentProvider(IDocumentProvider parentProvider) {
		IFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		manager.setSynchronizationContext(new UISynchronizationContext());
		if (parentProvider != null)
			setParentDocumentProvider(parentProvider);
		
		fResourceRuleFactory= ResourcesPlugin.getWorkspace().getRuleFactory();
	}
	
	final public void setParentDocumentProvider(IDocumentProvider parentProvider)  {
		
		Assert.isTrue(parentProvider instanceof IDocumentProviderExtension);
		Assert.isTrue(parentProvider instanceof IDocumentProviderExtension2);
		Assert.isTrue(parentProvider instanceof IDocumentProviderExtension3);
		Assert.isTrue(parentProvider instanceof IStorageDocumentProvider);
		
		fParentProvider= parentProvider;
		if (fParentProvider == null)
			fParentProvider= new NullProvider();
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
	 * @exception CoreException the operation's core exception
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
			throw new CoreException(new Status(IStatus.ERROR, EditorsUI.PLUGIN_ID, IStatus.ERROR, message, e));
		} catch (InterruptedException x) {
			String message= (x.getMessage() != null ? x.getMessage() : ""); //$NON-NLS-1$
			throw new CoreException(new Status(IStatus.CANCEL, EditorsUI.PLUGIN_ID, IStatus.OK, message, x));
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#connect(java.lang.Object)
	 */
	public void connect(Object element) throws CoreException {
		FileInfo info= (FileInfo) fFileInfoMap.get(element);
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
	 * @param info the element info
	 */
	private void storeFileBufferMapping(Object element, FileInfo info) {
		Object value= fFileBufferMap.get(info.fTextFileBuffer);
		
		if (value instanceof List) {
			List list= (List) value;
			list.add(element);
			return;
		}
		
		if (value == null) {
			value= element;
		} else {
			List list= new ArrayList(2);
			list.add(value);
			list.add(element);
			value= list;
		}
		fFileBufferMap.put(info.fTextFileBuffer, value);
	}

	protected FileInfo createEmptyFileInfo()  {
		return new FileInfo();
	}
	
	protected FileInfo createFileInfo(Object element) throws CoreException {
		
		IPath location= null;
		if (element instanceof IAdaptable) {
			IAdaptable adaptable= (IAdaptable) element;
			ILocationProvider provider= (ILocationProvider) adaptable.getAdapter(ILocationProvider.class);
			if (provider != null)
				location= provider.getPath(element);
		}
		
		if (location != null) {
			ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
			manager.connect(location, getProgressMonitor());
			manager.requestSynchronizationContext(location);
			ITextFileBuffer fileBuffer= manager.getTextFileBuffer(location);
			
			FileInfo info= createEmptyFileInfo();
			info.fTextFileBuffer= fileBuffer;
			info.fCachedReadOnlyState= isSystemFileReadOnly(info);
			
			IFile file= FileBuffers.getWorkspaceFileAtLocation(location);
			if (file != null && file.exists())
				info.fModel= createAnnotationModel(file);
			return info;
		}
		return null;
	}

	protected IAnnotationModel createAnnotationModel(IFile file) {
		return null;
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#disconnect(java.lang.Object)
	 */
	public void disconnect(Object element) {
		FileInfo info= (FileInfo) fFileInfoMap.get(element);
		
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
	 * @param info the given element info
	 */
	private void removeFileBufferMapping(Object element, FileInfo info) {
		Object value= fFileBufferMap.get(info.fTextFileBuffer);
		if (value == null)
			return;
		
		if (value instanceof List) {
			List list= (List) value;
			list.remove(element);
			if (list.size() == 1)
				fFileBufferMap.put(info.fTextFileBuffer, list.get(0));
		} else if (value == element) {
			fFileBufferMap.remove(info.fTextFileBuffer);
		}
	}

	protected void disposeFileInfo(Object element, FileInfo info) {
		IFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		try {
			IPath location= info.fTextFileBuffer.getLocation();
			manager.releaseSynchronizationContext(location);
			manager.disconnect(location, getProgressMonitor());
		} catch (CoreException x) {
			handleCoreException(x, "FileDocumentProvider.disposeElementInfo"); //$NON-NLS-1$
		}
	}
	
	/**
	 *Returns an iterator for all the elements that are connected to this file buffer.
	 * 
	 * @param file the file buffer
	 * @return an iterator for all elements connected with the given file buffer
	 */
	protected Iterator getElements(IFileBuffer file) {
		Object value= fFileBufferMap.get(file);
		if (value instanceof List)
			return new ArrayList((List) value).iterator();
		return new SingleElementIterator(value);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#getDocument(java.lang.Object)
	 */
	public IDocument getDocument(Object element) {
		FileInfo info= (FileInfo) fFileInfoMap.get(element);
		if (info != null)
			return info.fTextFileBuffer.getDocument();
		return getParentProvider().getDocument(element);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#resetDocument(java.lang.Object)
	 */
	public void resetDocument(Object element) throws CoreException {
		final FileInfo info= (FileInfo) fFileInfoMap.get(element);
		if (info != null) {
			DocumentProviderOperation operation= new DocumentProviderOperation() {
				/*
				 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider.DocumentProviderOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
				 */
				protected void execute(IProgressMonitor monitor) throws CoreException {
					info.fTextFileBuffer.revert(monitor);
					
					if (info.fModel instanceof AbstractMarkerAnnotationModel) {
						AbstractMarkerAnnotationModel markerModel= (AbstractMarkerAnnotationModel) info.fModel;
						markerModel.resetMarkers();
					}
				}
				/*
				 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider.DocumentProviderOperation#getSchedulingRule()
				 */
				public ISchedulingRule getSchedulingRule() {
					if (info.fElement instanceof IFileEditorInput) {
						IFileEditorInput input= (IFileEditorInput) info.fElement;
						return fResourceRuleFactory.modifyRule((input).getFile());
					} else
						return null;
				}
			};
			executeOperation(operation, getProgressMonitor());
		} else {
			getParentProvider().resetDocument(element);
		}
	}
	
	/*
	 * @see IDocumentProvider#saveDocument(IProgressMonitor, Object, IDocument, boolean)
	 */
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
		final FileInfo info= (FileInfo) fFileInfoMap.get(element);
		if (info != null) {
			
			if (info.fTextFileBuffer.getDocument() != document) {
				Status status= new Status(IStatus.WARNING, EditorsUI.PLUGIN_ID, IStatus.ERROR, "not the same document", null); //$NON-NLS-1$
				throw new CoreException(status);				
			}
			
			return new DocumentProviderOperation() {
				/*
				 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider.DocumentProviderOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
				 */
				public void execute(IProgressMonitor monitor) throws CoreException {
					commitFileBuffer(monitor, info, overwrite);					
				}
				/*
				 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider.DocumentProviderOperation#getSchedulingRule()
				 */
				public ISchedulingRule getSchedulingRule() {
					if (info.fElement instanceof IFileEditorInput) {
						IFileEditorInput input= (IFileEditorInput) info.fElement;
						return fResourceRuleFactory.modifyRule(input.getFile());
					} else
						return null;
				}
			};
			
		} else if (element instanceof IFileEditorInput) {
			
			final IFile file= ((IFileEditorInput) element).getFile();
			return new DocumentProviderOperation() {
				/*
				 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider.DocumentProviderOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
				 */
				public void execute(IProgressMonitor monitor) throws CoreException {
					createFileFromDocument(monitor, file, document);
				}
				/*
				 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider.DocumentProviderOperation#getSchedulingRule()
				 */
				public ISchedulingRule getSchedulingRule() {
					return fResourceRuleFactory.createRule(file);
				}
			};
		}
		
		return null;
	}
	
	protected void commitFileBuffer(IProgressMonitor monitor, FileInfo info, boolean overwrite) throws CoreException {
		Assert.isNotNull(info);
		info.fTextFileBuffer.commit(monitor, overwrite);
		if (info.fModel instanceof AbstractMarkerAnnotationModel) {
			AbstractMarkerAnnotationModel model= (AbstractMarkerAnnotationModel) info.fModel;
			model.updateMarkers(info.fTextFileBuffer.getDocument());
		}		
	}
	
	protected void createFileFromDocument(IProgressMonitor monitor, IFile file, IDocument document) throws CoreException {
		String encoding= getCharsetForNewFile(file, document);
		try {
			monitor.beginTask(TextEditorMessages.getString("TextFileDocumentProvider.beginTask.saving"), 2000); //$NON-NLS-1$
			InputStream stream= new ByteArrayInputStream(document.get().getBytes(encoding));
			ContainerGenerator generator = new ContainerGenerator(file.getWorkspace(), file.getParent().getFullPath());
			generator.generateContainer(new SubProgressMonitor(monitor, 1000));
			file.create(stream, false, new SubProgressMonitor(monitor, 1000));
		} catch (UnsupportedEncodingException x) {
			String message= TextEditorMessages.getFormattedString("Editor.error.unsupported_encoding.message_arg", encoding); //$NON-NLS-1$
			IStatus s= new Status(IStatus.ERROR, EditorsUI.PLUGIN_ID, IStatus.OK, message, x);
			throw new CoreException(s);
		} finally {
			monitor.done();
		}
	}
	
	private String getCharsetForNewFile(IFile targetFile, IDocument document) {
		// User-defined encoding has first priority
		String encoding;
		try {
			encoding= targetFile.getCharset(false);
		} catch (CoreException ex) {
			encoding= null;
		}
		if (encoding != null)
			return encoding;
		
		// Probe content
		Reader reader= new BufferedReader(new StringReader(document.get()));
		try {
			QualifiedName[] options= new QualifiedName[] { IContentDescription.CHARSET, IContentDescription.BYTE_ORDER_MARK };
			IContentDescription description= Platform.getContentTypeManager().getDescriptionFor(reader, targetFile.getName(), options);
			if (description != null) {
				encoding= description.getCharset();
				if (encoding != null)
					return encoding;
			}
		} catch (IOException ex) {
			// continue with next strategy
		} finally {
			try {
				reader.close();
			} catch (IOException ex) {
				TextEditorPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, TextEditorPlugin.PLUGIN_ID, IStatus.OK, "TextFileDocumentProvider.getCharsetForNewFile(...): Could not close reader", ex)); //$NON-NLS-1$
			}
		}
		
		// Use parent chain
		try {
			return targetFile.getParent().getDefaultCharset();
		} catch (CoreException ex) {
			// Use global default
			return ResourcesPlugin.getEncoding();
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#getModificationStamp(java.lang.Object)
	 */
	public long getModificationStamp(Object element) {
		FileInfo info= (FileInfo) fFileInfoMap.get(element);
		if (info != null)
			return info.fTextFileBuffer.getModificationStamp();
		return getParentProvider().getModificationStamp(element);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#getSynchronizationStamp(java.lang.Object)
	 */
	public long getSynchronizationStamp(Object element) {
		FileInfo info= (FileInfo) fFileInfoMap.get(element);
		if (info != null)
			return 0;
		return getParentProvider().getSynchronizationStamp(element);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#isDeleted(java.lang.Object)
	 */
	public boolean isDeleted(Object element) {
		FileInfo info= (FileInfo) fFileInfoMap.get(element);
		if (info != null)  {
			File file= getSystemFile(info);
			return file == null ? true : !file.exists();
		}
		return getParentProvider().isDeleted(element);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#mustSaveDocument(java.lang.Object)
	 */
	public boolean mustSaveDocument(Object element) {
		FileInfo info= (FileInfo) fFileInfoMap.get(element);
		if (info != null)
			return (info.fCount == 1) && info.fTextFileBuffer.isDirty();
		return getParentProvider().mustSaveDocument(element);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#canSaveDocument(java.lang.Object)
	 */
	public boolean canSaveDocument(Object element) {
		FileInfo info= (FileInfo) fFileInfoMap.get(element);
		if (info != null)
			return info.fTextFileBuffer.isDirty();
		return getParentProvider().canSaveDocument(element);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#getAnnotationModel(java.lang.Object)
	 */
	public IAnnotationModel getAnnotationModel(Object element) {
		FileInfo info= (FileInfo) fFileInfoMap.get(element);
		if (info != null) {
			if (info.fModel != null)
				return info.fModel;
			return info.fTextFileBuffer.getAnnotationModel();
		}
		return getParentProvider().getAnnotationModel(element);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#aboutToChange(java.lang.Object)
	 */
	public void aboutToChange(Object element) {
		FileInfo info= (FileInfo) fFileInfoMap.get(element);
		if (info == null)
			getParentProvider().aboutToChange(element);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#changed(java.lang.Object)
	 */
	public void changed(Object element) {
		FileInfo info= (FileInfo) fFileInfoMap.get(element);
		if (info == null)
			getParentProvider().changed(element);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#addElementStateListener(org.eclipse.ui.texteditor.IElementStateListener)
	 */
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

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#removeElementStateListener(org.eclipse.ui.texteditor.IElementStateListener)
	 */
	public void removeElementStateListener(IElementStateListener listener) {
		Assert.isNotNull(listener);
		fElementStateListeners.remove(listener);
		if (fElementStateListeners.size() == 0) {
			IFileBufferManager manager= FileBuffers.getTextFileBufferManager();
			manager.removeFileBufferListener(fFileBufferListener);
		}
		getParentProvider().removeElementStateListener(listener);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension#isReadOnly(java.lang.Object)
	 */
	public boolean isReadOnly(Object element) {
		FileInfo info= (FileInfo) fFileInfoMap.get(element);
		if (info != null)
			return info.fCachedReadOnlyState;
		return ((IDocumentProviderExtension) getParentProvider()).isReadOnly(element);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension#isModifiable(java.lang.Object)
	 */
	public boolean isModifiable(Object element) {
		FileInfo info= (FileInfo) fFileInfoMap.get(element);
		if (info != null)
			return info.fTextFileBuffer.isStateValidated() ? !isSystemFileReadOnly(info) : true;
		return ((IDocumentProviderExtension) getParentProvider()).isModifiable(element);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension#validateState(java.lang.Object, java.lang.Object)
	 */
	public void validateState(Object element, final Object computationContext) throws CoreException {
		final FileInfo info= (FileInfo) fFileInfoMap.get(element);
		if (info != null) {
			DocumentProviderOperation operation= new DocumentProviderOperation() {
				/*
				 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider.DocumentProviderOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
				 */
				protected void execute(IProgressMonitor monitor) throws CoreException {
					info.fTextFileBuffer.validateState(monitor, computationContext);
				}
				/*
				 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider.DocumentProviderOperation#getSchedulingRule()
				 */
				public ISchedulingRule getSchedulingRule() {
					if (info.fElement instanceof IFileEditorInput) {
						IFileEditorInput input= (IFileEditorInput) info.fElement;
						return fResourceRuleFactory.validateEditRule(new IResource[] { input.getFile() });
					} else
						return null;
				}
			};
			executeOperation(operation, getProgressMonitor());
		} else
			((IDocumentProviderExtension) getParentProvider()).validateState(element, computationContext);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension#isStateValidated(java.lang.Object)
	 */
	public boolean isStateValidated(Object element) {
		FileInfo info= (FileInfo) fFileInfoMap.get(element);
		if (info != null)
			return info.fTextFileBuffer.isStateValidated();
		return ((IDocumentProviderExtension) getParentProvider()).isStateValidated(element);
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension#updateStateCache(java.lang.Object)
	 */
	public void updateStateCache(Object element) throws CoreException {
		FileInfo info= (FileInfo) fFileInfoMap.get(element);
		if (info != null) {
			boolean isReadOnly= isSystemFileReadOnly(info);
			// See http://bugs.eclipse.org/bugs/show_bug.cgi?id=14469 for the dirty bit check
			if (!info.fCachedReadOnlyState && isReadOnly && info.fTextFileBuffer.isDirty())
				info.fTextFileBuffer.resetStateValidation();
			info.fCachedReadOnlyState= isReadOnly;
		} else {
			((IDocumentProviderExtension) getParentProvider()).updateStateCache(element);
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension#setCanSaveDocument(java.lang.Object)
	 */
	public void setCanSaveDocument(Object element) {
		FileInfo info= (FileInfo) fFileInfoMap.get(element);
		if (info == null)
			((IDocumentProviderExtension) getParentProvider()).setCanSaveDocument(element);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension#getStatus(java.lang.Object)
	 */
	public IStatus getStatus(Object element) {
		FileInfo info= (FileInfo) fFileInfoMap.get(element);
		if (info != null)
			return info.fTextFileBuffer.getStatus();
		return ((IDocumentProviderExtension) getParentProvider()).getStatus(element);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension#synchronize(java.lang.Object)
	 */
	public void synchronize(Object element) throws CoreException {
		final FileInfo info= (FileInfo) fFileInfoMap.get(element);
		if (info != null) {
			DocumentProviderOperation operation= new DocumentProviderOperation() {
				/*
				 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider.DocumentProviderOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
				 */
				protected void execute(IProgressMonitor monitor) throws CoreException {
					info.fTextFileBuffer.revert(monitor);
				}
				/*
				 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider.DocumentProviderOperation#getSchedulingRule()
				 */
				public ISchedulingRule getSchedulingRule() {
					if (info.fElement instanceof IFileEditorInput) {
						IFileEditorInput input= (IFileEditorInput) info.fElement;
						return fResourceRuleFactory.refreshRule(input.getFile());
					} else
						return null;
				}
			};
			executeOperation(operation, getProgressMonitor());
		} else {
			((IDocumentProviderExtension) getParentProvider()).synchronize(element);
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension2#setProgressMonitor(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void setProgressMonitor(IProgressMonitor progressMonitor) {
		fProgressMonitor= progressMonitor;
		((IDocumentProviderExtension2) getParentProvider()).setProgressMonitor(progressMonitor);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension2#getProgressMonitor()
	 */
	public IProgressMonitor getProgressMonitor() {
		return fProgressMonitor;
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension3#isSynchronized(java.lang.Object)
	 */
	public boolean isSynchronized(Object element) {
		FileInfo info= (FileInfo) fFileInfoMap.get(element);
		if (info != null)
			return info.fTextFileBuffer.isSynchronized();
		return ((IDocumentProviderExtension3) getParentProvider()).isSynchronized(element);
	}

	/*
	 * @see org.eclipse.ui.editors.text.IStorageDocumentProvider#getDefaultEncoding()
	 */
	public String getDefaultEncoding() {
		return FileBuffers.getTextFileBufferManager().getDefaultEncoding();
	}

	/*
	 * @see org.eclipse.ui.editors.text.IStorageDocumentProvider#getEncoding(java.lang.Object)
	 */
	public String getEncoding(Object element) {
		FileInfo info= (FileInfo) fFileInfoMap.get(element);
		if (info != null)
			return info.fTextFileBuffer.getEncoding();
		return ((IStorageDocumentProvider) getParentProvider()).getEncoding(element);
	}

	/*
	 * @see org.eclipse.ui.editors.text.IStorageDocumentProvider#setEncoding(java.lang.Object, java.lang.String)
	 */
	public void setEncoding(Object element, String encoding) {
		FileInfo info= (FileInfo) fFileInfoMap.get(element);
		if (info != null)
			info.fTextFileBuffer.setEncoding(encoding);
		else
			((IStorageDocumentProvider) getParentProvider()).setEncoding(element, encoding);
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
		ILog log= Platform.getLog(bundle);		
		IStatus status= message != null ? new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, message, exception) : exception.getStatus();
		log.log(status);
	}
	
	protected File getSystemFile(FileInfo info)  {
		IPath path= info.fTextFileBuffer.getLocation();
		return FileBuffers.getSystemFileAtLocation(path);
	}
	
	protected boolean isSystemFileReadOnly(FileInfo info)  {
		File file= getSystemFile(info);
		return file == null ? true : !file.canWrite();
	}
	
	protected FileInfo getFileInfo(Object element)  {
		return (FileInfo) fFileInfoMap.get(element);
	}
	
	protected Iterator getConnectedElementsIterator()  {
		return new HashSet(fFileInfoMap.keySet()).iterator();
	}
	
	protected Iterator getFileInfosIterator()  {
		return new ArrayList(fFileInfoMap.values()).iterator();
	}
	
	protected void fireElementStateChanging(Object element) {
		List list= new ArrayList(fElementStateListeners);
		Iterator e= list.iterator();
		while (e.hasNext()) {
			Object l= e.next();
			if (l instanceof IElementStateListenerExtension) {
				IElementStateListenerExtension x= (IElementStateListenerExtension) l;
				x.elementStateChanging(element);
			}
		}
	}
	
	protected void fireElementStateChangeFailed(Object element) {
		List list= new ArrayList(fElementStateListeners);
		Iterator e= list.iterator();
		while (e.hasNext()) {
			Object l= e.next();
			if (l instanceof IElementStateListenerExtension) {
				IElementStateListenerExtension x= (IElementStateListenerExtension) l;
				x.elementStateChangeFailed(element);
			}
		}
	}
}
