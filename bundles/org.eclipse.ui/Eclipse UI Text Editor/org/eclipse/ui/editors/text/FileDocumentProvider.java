package org.eclipse.ui.editors.text;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */


import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.IAnnotationModel;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerGenerator;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;



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
				handleCoreException(x, "FileDocumentProvider.resourceChanged");
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
							if (info.fModificationStamp == IResource.NULL_STAMP || getFile().getModificationStamp() != info.fModificationStamp) {
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
							runnable= new Runnable() {
								public void run() {
									if (getElementInfo(fFileEditorInput) != null)
										handleElementDeleted(fFileEditorInput);
								}
							};
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
	protected class FileInfo extends ElementInfo {
		
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
	 * @see AbstractDocumentProvider#createAnnotationModel(Object)
	 */
	protected IAnnotationModel createAnnotationModel(Object element) throws CoreException {
		if (element instanceof IFileEditorInput) {
			IFileEditorInput input= (IFileEditorInput) element;
			return new ResourceMarkerAnnotationModel(input.getFile());
		}
		
		return super.createAnnotationModel(element);
	}
	/*
	 * @see AbstractDocumentProvider#createElementInfo(Object)
	 */
	protected ElementInfo createElementInfo(Object element) throws CoreException {
		if (element instanceof IFileEditorInput) {
			
			IFileEditorInput input= (IFileEditorInput) element;
			
			IDocument d= createDocument(element);
			IAnnotationModel m= createAnnotationModel(element);
			FileSynchronizer f= new FileSynchronizer(input);
			f.install();
			return new FileInfo(d, m, f);
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
	/*
	 * @see AbstractDocumentProvider#doSaveDocument(IProgressMonitor, Object, IDocument)
	 */
	protected void doSaveDocument(IProgressMonitor monitor, Object element, IDocument document) throws CoreException {
		if (element instanceof IFileEditorInput) {
			
			IFileEditorInput input= (IFileEditorInput) element;
			InputStream stream= new ByteArrayInputStream(document.get().getBytes());
			
			IFile file= input.getFile();
									
			if (file.exists()) {
				
				FileInfo info= (FileInfo) getElementInfo(element);
				file.setContents(stream, false, true, monitor);
				
				if (info != null) {
					
					info.fModificationStamp= file.getModificationStamp();
					
					ResourceMarkerAnnotationModel model= (ResourceMarkerAnnotationModel) info.fModel;
					model.updateMarkers(info.fDocument);
				}
				
			} else {
				try {
					monitor.beginTask("Saving", 2000);
					ContainerGenerator generator = new ContainerGenerator(file.getParent().getFullPath());
					generator.generateContainer(new SubProgressMonitor(monitor, 1000));
					file.create(stream, false, new SubProgressMonitor(monitor, 1000));
				}
				finally {
					monitor.done();
				}
			}
		} else {
			super.doSaveDocument(monitor, element, document);
		}
	}
	/*
	 * @see AbstractDocumentProvider#getElementInfo(Object)
	 * It's only here to circumvent visibility issues with 
	 * certain compilers.
	 */
	protected ElementInfo getElementInfo(Object element) {
		return super.getElementInfo(element);
	}
	/**
	 * Defines the standard procedure to handle CoreExceptions.
	 *
	 * @param exception the exception to be logged
	 * @param message the message to be logged
	 */
	protected void handleCoreException(CoreException exception, String message) {
		ILog log= Platform.getPlugin(PlatformUI.PLUGIN_ID).getLog();
		
		if (message != null)
			log.log(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, message, null));
		
		log.log(exception.getStatus());
	}
	/**
	 * Updates the element info to an change of the file content and sends out
	 * appropriate notifications.
	 *
	 * @param fileEditorInput the input of an text editor
	 */
	protected void handleElementContentChanged(IFileEditorInput fileEditorInput) {
		FileInfo info= (FileInfo) getElementInfo(fileEditorInput);
		try {
			
			IDocument document= new Document();
			setDocumentContent(document, fileEditorInput.getFile().getContents(false));
			String newContent= document.get();
			
			if ( !newContent.equals(info.fDocument.get())) {
				
				// set the new content and fire content related events
				fireElementContentAboutToBeReplaced(fileEditorInput);
				info.fDocument.removeDocumentListener(info);
				info.fDocument.set(newContent);
				info.fCanBeSaved= false;
				info.fModificationStamp= IResource.NULL_STAMP;
				fireElementContentReplaced(fileEditorInput);
				info.fDocument.addDocumentListener(info);
				
			} else {
				
				// fires only the dirty state related event
				info.fCanBeSaved= false;
				info.fModificationStamp= IResource.NULL_STAMP;
				fireElementDirtyStateChanged(fileEditorInput, false);
			}
			
		} catch (CoreException x) {
			handleCoreException(x, "FileDocumentProvider.updateContent");
		}
	}
	/**
	 * Sends out the notification that the file serving as document input has been deleted.
	 *
	 * @param fileEditorInput the input of an text editor
	 */
	protected void handleElementDeleted(IFileEditorInput fileEditorInput) {
		fireElementDeleted(fileEditorInput);
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
}
