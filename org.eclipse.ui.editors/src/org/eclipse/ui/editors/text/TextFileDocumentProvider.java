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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

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
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProviderExtension;
import org.eclipse.ui.texteditor.IDocumentProviderExtension2;
import org.eclipse.ui.texteditor.IDocumentProviderExtension3;
import org.eclipse.ui.texteditor.IElementStateListener;
import org.eclipse.ui.texteditor.IElementStateListenerExtension;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;

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
				l.elementContentAboutToBeReplaced(getElement(file));
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
				l.elementContentReplaced(getElement(file));
			}
		}

		/*
		 * @see org.eclipse.core.buffer.text.IBufferedFileListener#stateChanging(org.eclipse.core.buffer.text.IBufferedFile)
		 */
		public void stateChanging(IFileBuffer file) {
			Object element= getElement(file);
			fireElementStateChanging(element);
		}

		/*
		 * @see org.eclipse.core.buffer.text.IBufferedFileListener#dirtyStateChanged(org.eclipse.core.buffer.text.IBufferedFile, boolean)
		 */
		public void dirtyStateChanged(IFileBuffer file, boolean isDirty) {
			List list= new ArrayList(fElementStateListeners);
			Iterator e= list.iterator();
			while (e.hasNext()) {
				IElementStateListener l= (IElementStateListener) e.next();
				l.elementDirtyStateChanged(getElement(file), isDirty);
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
					x.elementStateValidationChanged(getElement(file), isStateValidated);
				}
			}
		}

		/*
		 * @see org.eclipse.core.buffer.text.IBufferedFileListener#underlyingFileMoved(org.eclipse.core.buffer.text.IBufferedFile, org.eclipse.core.resources.IFile)
		 */
		public void underlyingFileMoved(IFileBuffer file, IFile target) {
			IEditorInput input= target == null ? null : new FileEditorInput(target);
			List list= new ArrayList(fElementStateListeners);
			Iterator e= list.iterator();
			while (e.hasNext()) {
				IElementStateListener l= (IElementStateListener) e.next();
				l.elementMoved(getElement(file), input);
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
				l.elementDeleted(getElement(file));
			}
		}

		/*
		 * @see org.eclipse.core.buffer.text.IBufferedFileListener#stateChangeFailed(org.eclipse.core.buffer.text.IBufferedFile)
		 */
		public void stateChangeFailed(IFileBuffer file) {
			Object element= getElement(file);
			fireElementStateChangeFailed(element);
		}
	}


	
	/** The parent document provider */
	private IDocumentProvider fParentProvider;
	/** Element information of all connected elements */
	private Map fFileInfoMap= new HashMap();
	/** The list of element state listeners */
	private List fElementStateListeners= new ArrayList();
	/** The buffered file listener */
	private IFileBufferListener fBufferedFileListener= new FileBufferListener();
	/** The progress monitor */
	private IProgressMonitor fProgressMonitor;
	
	
	public TextFileDocumentProvider()  {
		IFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		manager.addFileBufferListener(fBufferedFileListener);
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
		}	
		++ info.fCount;
	}
	
	protected FileInfo createEmptyFileInfo()  {
		return new FileInfo();
	}
	
	protected FileInfo createFileInfo(Object element) throws CoreException {
		if (element instanceof IFileEditorInput) {
			IFileEditorInput input= (IFileEditorInput) element;
			
			ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
			manager.connect(input.getFile(), getProgressMonitor());
			ITextFileBuffer f= manager.getTextFileBuffer(input.getFile());
			
			FileInfo info= createEmptyFileInfo();
			info.fTextFileBuffer= f;
			info.fModel= createAnnotationModel(input.getFile());
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
			disposeFileInfo(element, info);
			
		} else
			-- info.fCount;
	}

	protected void disposeFileInfo(Object element, FileInfo info) {
		IFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		try {
			manager.disconnect(info.fTextFileBuffer.getUnderlyingFile(), getProgressMonitor());
		} catch (CoreException x) {
			handleCoreException(x, "FileDocumentProvider.disposeElementInfo"); //$NON-NLS-1$
		}
	}
	
	protected Object getElement(IFileBuffer file) {
		Iterator e= fFileInfoMap.keySet().iterator();
		while (e.hasNext())  {
			Object key= e.next();
			FileInfo info= (FileInfo) fFileInfoMap.get(key);
			if (info != null && file == info.fTextFileBuffer)
				return info.fElement;
		}
		return null;
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
		if (info != null)
			info.fTextFileBuffer.commit(monitor, overwrite);
		else
			getParentProvider().saveDocument(monitor, element, document, overwrite);
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#getModificationStamp(java.lang.Object)
	 */
	public long getModificationStamp(Object element) {
		FileInfo info= (FileInfo) fFileInfoMap.get(element);
		if (info != null)  {
			File file= getSystemFile(info);
			if (file != null)
				return file.lastModified();
			return info.fTextFileBuffer.getUnderlyingFile().getModificationStamp();
		}
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
		if (info == null)
			((IDocumentProviderExtension) getParentProvider()).updateStateCache(element);
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
			return info.fTextFileBuffer.getUnderlyingFile().isSynchronized(IResource.DEPTH_ZERO);
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
		IFile file= info.fTextFileBuffer.getUnderlyingFile();
		IPath path= file.getLocation();
		return path == null ? null : path.toFile();		
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
