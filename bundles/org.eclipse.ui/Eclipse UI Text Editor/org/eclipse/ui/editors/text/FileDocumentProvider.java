package org.eclipse.ui.editors.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerGenerator;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.dialogs.MessageDialog;



/**
 * Shareable document provider specialized for file resources (<code>IFile</code>).<p>
 * This class may be instantiated or be subclassed.
 */
public class FileDocumentProvider extends StorageDocumentProvider {
	
	
	/**
	 * Synchronizes the document with external resource changes.
	 */
	protected class FileSynchronizer implements IResourceChangeListener, IResourceDeltaVisitor {
		
		/** The file editor input */
		protected IFileEditorInput fFileEditorInput;
		
		/**
		 * Creates a new file synchronizer. Is not yet installed on a resource.
		 */
		public FileSynchronizer(IFileEditorInput fileEditorInput) {
			fFileEditorInput= fileEditorInput;
		};
		
		/**
		 * Creates a new file synchronizer. Is not yet installed on a resource.
		 * @deprecated since 0.042 use FileSynchronizer(IFileEditorInput)
		 */
		public FileSynchronizer(FileEditorInput fileEditorInput) {
			fFileEditorInput= fileEditorInput;
		};
		
		/**
		 * Returns the file wrapped by the file editor input.
		 */
		protected IFile getFile() {
			return fFileEditorInput.getFile();
		}
		
		/**
		 * Installs the synchronizer on the input's file.
		 */
		public void install() {
			getFile().getWorkspace().addResourceChangeListener(this);
		}
		
		/**
		 * Uninstalls the synchronizer from the input's file.
		 */
		public void uninstall() {
			getFile().getWorkspace().removeResourceChangeListener(this);
		}
		
		/*
		 * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
		 */
		public void resourceChanged(IResourceChangeEvent e) {
			IResourceDelta delta= e.getDelta();
			try {
				if (delta != null)
					delta.accept(this);
			} catch (CoreException x) {
				handleCoreException(x, TextEditorMessages.getString("FileDocumentProvider.resourceChanged")); //$NON-NLS-1$
			}
		}
		
		/*
		 * @see IResourceDeltaVisitor#visit(IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
						
			if (delta != null && getFile().equals(delta.getResource())) {
				
				Runnable runnable= null;
				
				switch (delta.getKind()) {
					case IResourceDelta.CHANGED:
						if ((IResourceDelta.CONTENT & delta.getFlags()) != 0) {
							FileInfo info= (FileInfo) getElementInfo(fFileEditorInput);
							if (!info.fCanBeSaved && computeModificationStamp(getFile()) != info.fModificationStamp) {
								runnable= new Runnable() {
									public void run() {
										if (getElementInfo(fFileEditorInput) != null)
											handleElementContentChanged(fFileEditorInput);
									}
								};
							}
						}
						break;
					case IResourceDelta.REMOVED:
						if ((IResourceDelta.MOVED_TO & delta.getFlags()) != 0) {
							final IPath path= delta.getMovedToPath();
							runnable= new Runnable() {
								public void run() {
									if (getElementInfo(fFileEditorInput) != null)
										handleElementMoved(fFileEditorInput, path);
								}
							};
						} else {
							FileInfo info= (FileInfo) getElementInfo(fFileEditorInput);
							if (!info.fCanBeSaved) {
								runnable= new Runnable() {
									public void run() {
										if (getElementInfo(fFileEditorInput) != null)
											handleElementDeleted(fFileEditorInput);
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
		
		/*
		 * Posts the update code "behind" the running operation.
		 *
		 * @param runnable the update code
		 */
		protected void update(Runnable runnable) {
			IWorkbench workbench= PlatformUI.getWorkbench();
			IWorkbenchWindow[] windows= workbench.getWorkbenchWindows();
			if (windows != null && windows.length > 0) {
				Display display= windows[0].getShell().getDisplay();
				display.asyncExec(runnable);
			} else
				runnable.run();
		}
	};
	
	
	
	/**
	 * Bundle of all required informations to allow files as 
	 * underlying document resources. 
	 */
	protected class FileInfo extends StorageInfo {
		
		/** The file synchronizer */
		public FileSynchronizer fFileSynchronizer;
		/** The time stamp at which this provider changed the file */
		public long fModificationStamp= IResource.NULL_STAMP;
		
		public FileInfo(IDocument document, IAnnotationModel model, FileSynchronizer fileSynchronizer) {
			super(document, model);
			fFileSynchronizer= fileSynchronizer;
		}
	};
	
	
	/**
	 * Creates a new document provider.
	 */
	public FileDocumentProvider() {
		super();
	}
	
	/*
	 * @see StorageDocumentProvider#setDocumentContent(IDocument, IEditorInput)
	 */
	protected boolean setDocumentContent(IDocument document, IEditorInput editorInput) throws CoreException {
		if (editorInput instanceof IFileEditorInput) {
			IFile file= ((IFileEditorInput) editorInput).getFile();
			setDocumentContent(document, file.getContents(false));
			return true;
		}
		
		return super.setDocumentContent(document, editorInput);
	}
	
	/*
	 * @see AbstractDocumentProvider#createAnnotationModel(Object)
	 */
	protected IAnnotationModel createAnnotationModel(Object element) throws CoreException {
		if (element instanceof IFileEditorInput) {
			IFileEditorInput input= (IFileEditorInput) element;
			return new ResourceMarkerAnnotationModel(input.getFile());
		}
		
		return super.createAnnotationModel(element);
	}
	
	/**
	 * Checks whether the given resource has been changed on the 
	 * local file system by comparing the actual time stamp with the 
	 * cached one. If the resource has been changed, a <code>CoreException</code>
	 * is thrown.
	 * 
	 * @param cachedModificationStamp the chached modification stamp
	 * @param resource the resource to check
	 * @exception CoreException if resource has been changed on the file system
	 */
	protected void checkSynchronizationState(long cachedModificationStamp, IResource resource) throws CoreException {
		if (cachedModificationStamp != computeModificationStamp(resource)) {
			Status status= new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IResourceStatus.OUT_OF_SYNC_LOCAL, TextEditorMessages.getString("FileDocumentProvider.error.out_of_sync"), null); //$NON-NLS-1$
			throw new CoreException(status);
		}
	}
	
	/**
	 * Computes the initial modification stamp for the given resource.
	 * 
	 * @param resource the resource
	 * @return the modification stamp
	 */
	protected long computeModificationStamp(IResource resource) {
		long modificationStamp= resource.getModificationStamp();
		
		IPath path= resource.getLocation();
		if (path == null)
			return modificationStamp;
			
		modificationStamp= path.toFile().lastModified();
		return modificationStamp;
	}
	
	/*
	 * @see IDocumentProvider#getModificationStamp(Object)
	 */
	public long getModificationStamp(Object element) {
		
		if (element instanceof IFileEditorInput) {
			IFileEditorInput input= (IFileEditorInput) element;
			return computeModificationStamp(input.getFile());
		}
		
		return super.getModificationStamp(element);
	}
	
	/*
	 * @see IDocumentProvider#getSynchronizationStamp(Object)
	 */
	public long getSynchronizationStamp(Object element) {
		
		if (element instanceof IFileEditorInput) {
			FileInfo info= (FileInfo) getElementInfo(element);
			return info.fModificationStamp;
		}
		
		return super.getSynchronizationStamp(element);
	}
	
	/*
	 * @see IDocumentProvider#isDeleted(Object)
	 */
	public boolean isDeleted(Object element) {
		
		if (element instanceof IFileEditorInput) {
			IFileEditorInput input= (IFileEditorInput) element;
			
			IPath path= input.getFile().getLocation();
			if (path == null)
				return true;
				
			return !path.toFile().exists();
		}
		
		return super.isDeleted(element);
	}
	
	/*
	 * @see AbstractDocumentProvider#doSaveDocument(IProgressMonitor, Object, IDocument, boolean)
	 */
	protected void doSaveDocument(IProgressMonitor monitor, Object element, IDocument document, boolean overwrite) throws CoreException {
		if (element instanceof IFileEditorInput) {
			
			IFileEditorInput input= (IFileEditorInput) element;
			InputStream stream= new ByteArrayInputStream(document.get().getBytes());
			
			IFile file= input.getFile();
									
			if (file.exists()) {				
				
				FileInfo info= (FileInfo) getElementInfo(element);
				
				if (info != null && !overwrite)
					checkSynchronizationState(info.fModificationStamp, file);
				
				file.setContents(stream, overwrite, true, monitor);
				
				if (info != null) {
										
					ResourceMarkerAnnotationModel model= (ResourceMarkerAnnotationModel) info.fModel;
					model.updateMarkers(info.fDocument);
					
					info.fModificationStamp= computeModificationStamp(file);
				}
				
			} else {
				try {
					monitor.beginTask(TextEditorMessages.getString("FileDocumentProvider.task.saving"), 2000); //$NON-NLS-1$
					ContainerGenerator generator = new ContainerGenerator(file.getParent().getFullPath());
					generator.generateContainer(new SubProgressMonitor(monitor, 1000));
					file.create(stream, false, new SubProgressMonitor(monitor, 1000));
				}
				finally {
					monitor.done();
				}
			}
		} else {
			super.doSaveDocument(monitor, element, document, overwrite);
		}
	}
	
	/*
	 * @see AbstractDocumentProvider#createElementInfo(Object)
	 */
	protected ElementInfo createElementInfo(Object element) throws CoreException {
		if (element instanceof IFileEditorInput) {
			
			IFileEditorInput input= (IFileEditorInput) element;
			
			try {
				input.getFile().refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (CoreException x) {
				handleCoreException(x,TextEditorMessages.getString("FileDocumentProvider.createElementInfo")); //$NON-NLS-1$
			}
			
			IDocument d= createDocument(element);
			IAnnotationModel m= createAnnotationModel(element);
			FileSynchronizer f= new FileSynchronizer(input);
			f.install();
			
			FileInfo info= new FileInfo(d, m, f);
			info.fModificationStamp= computeModificationStamp(input.getFile());
			
			return info;
		}
		
		return super.createElementInfo(element);
	}
	
	/*
	 * @see AbstractDocumentProvider#disposeElementInfo(Object, ElementInfo)
	 */
	protected void disposeElementInfo(Object element, ElementInfo info) {
		if (info instanceof FileInfo) {
			FileInfo fileInfo= (FileInfo) info;
			if (fileInfo.fFileSynchronizer != null)
				fileInfo.fFileSynchronizer.uninstall();
		}
		
		super.disposeElementInfo(element, info);
	}	
	
	/**
	 * Updates the element info to a change of the file content and sends out
	 * appropriate notifications.
	 *
	 * @param fileEditorInput the input of an text editor
	 */
	protected void handleElementContentChanged(IFileEditorInput fileEditorInput) {
		FileInfo info= (FileInfo) getElementInfo(fileEditorInput);
		try {
			
			IDocument document= new Document();
			setDocumentContent(document, fileEditorInput);
			String newContent= document.get();
			
			if ( !newContent.equals(info.fDocument.get())) {
				
				// set the new content and fire content related events
				fireElementContentAboutToBeReplaced(fileEditorInput);
				
				removeUnchangedElementListeners(fileEditorInput, info);
				
				info.fDocument.removeDocumentListener(info);
				info.fDocument.set(newContent);
				info.fCanBeSaved= false;
				info.fModificationStamp= computeModificationStamp(fileEditorInput.getFile());
				
				addUnchangedElementListeners(fileEditorInput, info);
				
				fireElementContentReplaced(fileEditorInput);
				
			} else {
				
				removeUnchangedElementListeners(fileEditorInput, info);
				
				// fires only the dirty state related event
				info.fCanBeSaved= false;
				info.fModificationStamp= computeModificationStamp(fileEditorInput.getFile());
				
				addUnchangedElementListeners(fileEditorInput, info);
				
				fireElementDirtyStateChanged(fileEditorInput, false);
			}
			
		} catch (CoreException x) {
			handleCoreException(x, TextEditorMessages.getString("FileDocumentProvider.updateContent")); //$NON-NLS-1$
		}
	}
	
	/**
	 * Sends out the notification that the file serving as document input has been moved.
	 * 
	 * @param fileEditorInput the input of an text editor
	 * @param path the path of the new location of the file
	 */
	protected void handleElementMoved(IFileEditorInput fileEditorInput, IPath path) {
		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		IFile newFile= workspace.getRoot().getFile(path);
		fireElementMoved(fileEditorInput, newFile == null ? null : new FileEditorInput(newFile));
	}
	
	/**
	 * Sends out the notification that the file serving as document input has been deleted.
	 *
	 * @param fileEditorInput the input of an text editor
	 */
	protected void handleElementDeleted(IFileEditorInput fileEditorInput) {
		fireElementDeleted(fileEditorInput);
	}
	
	/*
	 * @see AbstractDocumentProvider#getElementInfo(Object)
	 * It's only here to circumvent visibility issues with certain compilers.
	 */
	protected ElementInfo getElementInfo(Object element) {
		return super.getElementInfo(element);
	}
	
	/*
	 * @see AbstractDocumentProvider#doValidateState(Object, Object)
	 */
	protected void doValidateState(Object element, Object computationContext) throws CoreException {
		
		if (element instanceof IFileEditorInput) {
			IFileEditorInput input= (IFileEditorInput) element;
			FileInfo info= (FileInfo) getElementInfo(input);
			if (info != null) {
				IFile file= input.getFile();
				if (file.isReadOnly()) { // do not use cached state here
					IWorkspace workspace= file.getWorkspace();
					workspace.validateEdit(new IFile[] { file }, computationContext);
				}
			}
		}
		
		super.doValidateState(element, computationContext);
	}
	
	/*
	 * @see IDocumentProviderExtension#isModifiable(Object)
	 */
	public boolean isModifiable(Object element) {
		if (!isStateValidated(element)) {
			if (element instanceof IFileEditorInput)
				return true;
		}
		return super.isModifiable(element);
	}
}