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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.eclipse.core.internal.filebuffers.ContainerGenerator;
import org.eclipse.core.resources.IFile;
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
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.filebuffers.IFileBufferManager;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProviderExtension;
import org.eclipse.ui.texteditor.IDocumentProviderExtension2;
import org.eclipse.ui.texteditor.IDocumentProviderExtension3;
import org.eclipse.ui.texteditor.IElementStateListener;
import org.eclipse.ui.texteditor.IElementStateListenerExtension;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;

import org.eclipse.ui.internal.editors.text.EditorsPlugin;

/**
 * @since 3.0
 */
public class TextFileDocumentProvider  implements IDocumentProvider, IDocumentProviderExtension, IDocumentProviderExtension2, IDocumentProviderExtension3, IStorageDocumentProvider {
	
		
	static protected class NullProvider implements IDocumentProvider, IDocumentProviderExtension, IDocumentProviderExtension2, IDocumentProviderExtension3, IStorageDocumentProvider  {
		
		static final private IStatus STATUS_ERROR= new Status(IStatus.ERROR, EditorsPlugin.getPluginId(), IStatus.INFO, TextEditorMessages.getString("NullProvider.error"), null); //$NON-NLS-1$
		
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
	
	
	public TextFileDocumentProvider()  {
		this(null);
	}
	
	public TextFileDocumentProvider(IDocumentProvider parentProvider) {
		IFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		manager.addFileBufferListener(fFileBufferListener);
		manager.setSynchronizationContext(new UISynchronizationContext());
		if (parentProvider != null)
			setParentDocumentProvider(parentProvider);
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
	 * Updates the file buffer map with a new releation between the file buffer
	 * of the given info and the given element;
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
		return new ResourceMarkerAnnotationModel(file);
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
		FileInfo info= (FileInfo) fFileInfoMap.get(element);
		if (info != null)
			info.fTextFileBuffer.revert(getProgressMonitor());
		else
			getParentProvider().resetDocument(element);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#saveDocument(org.eclipse.core.runtime.IProgressMonitor, java.lang.Object, org.eclipse.jface.text.IDocument, boolean)
	 */
	public void saveDocument(IProgressMonitor monitor, Object element, IDocument document, boolean overwrite) throws CoreException {
		FileInfo info= (FileInfo) fFileInfoMap.get(element);
		if (info != null) {
			info.fTextFileBuffer.commit(monitor, overwrite);
			if (info.fModel instanceof AbstractMarkerAnnotationModel) {
				AbstractMarkerAnnotationModel model= (AbstractMarkerAnnotationModel) info.fModel;
				model.updateMarkers(info.fTextFileBuffer.getDocument());
			}
		} else if (element instanceof IFileEditorInput) {
			try {
				monitor.beginTask("Saving", 2000);
				InputStream stream= new ByteArrayInputStream(document.get().getBytes(getDefaultEncoding()));
				IFile file= ((IFileEditorInput) element).getFile();
				ContainerGenerator generator = new ContainerGenerator(file.getWorkspace(), file.getParent().getFullPath());
				generator.generateContainer(new SubProgressMonitor(monitor, 1000));
				file.create(stream, false, new SubProgressMonitor(monitor, 1000));
			} catch (UnsupportedEncodingException x) {
				IStatus s= new Status(IStatus.ERROR, EditorsPlugin.getPluginId(), IStatus.OK, x.getMessage(), x);
				throw new CoreException(s);
			} finally {
				monitor.done();
			}
		} else
			getParentProvider().saveDocument(monitor, element, document, overwrite);
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#getModificationStamp(java.lang.Object)
	 */
	public long getModificationStamp(Object element) {
		FileInfo info= (FileInfo) fFileInfoMap.get(element);
		if (info != null)
			return info.fTextFileBuffer.getModifcationStamp();
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
		if (info != null)
			return info.fModel;
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
		if (!fElementStateListeners.contains(listener))
			fElementStateListeners.add(listener);
		getParentProvider().addElementStateListener(listener);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#removeElementStateListener(org.eclipse.ui.texteditor.IElementStateListener)
	 */
	public void removeElementStateListener(IElementStateListener listener) {
		Assert.isNotNull(listener);
		fElementStateListeners.remove(listener);
		getParentProvider().removeElementStateListener(listener);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension#isReadOnly(java.lang.Object)
	 */
	public boolean isReadOnly(Object element) {
		FileInfo info= (FileInfo) fFileInfoMap.get(element);
		if (info != null)
			return isSystemFileReadOnly(info);
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
	public void validateState(Object element, Object computationContext) throws CoreException {
		FileInfo info= (FileInfo) fFileInfoMap.get(element);
		if (info != null)
			info.fTextFileBuffer.validateState(getProgressMonitor(), computationContext);
		else
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
		FileInfo info= (FileInfo) fFileInfoMap.get(element);
		if (info != null)
			info.fTextFileBuffer.revert(getProgressMonitor());
		else
			((IDocumentProviderExtension) getParentProvider()).synchronize(element);
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
		ILog log= Platform.getPlugin(PlatformUI.PLUGIN_ID).getLog();
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
