/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.internal.filebuffers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.filebuffers.IDocumentFactory;
import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
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
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.jface.text.IDocument;


/**
 * Shareable document provider specialized for file resources (<code>IFile</code>).
 * <p>
 * This class may be instantiated or be subclassed.</p>
 */
public class FileDocumentProvider2 extends StorageDocumentProvider2 {

	/**
	 * Qualified name for the encoding key.
	 */
	private static final QualifiedName ENCODING_KEY = new QualifiedName(FileBuffersPlugin.PLUGIN_ID, "encoding"); //$NON-NLS-1$ //$NON-NLS-2$

	
	/**
	 * Runnable encapsulating an element state change. This runnable ensures 
	 * that a element change failed message is sent out to the element state
	 * listeners in case an exception occurred.
	 */
	protected class SafeResourceChange implements Runnable {
		
		/** The resource that changes. */
		private IResource fChangedResource;
		
		/**
		 * Creates a new safe runnable for the given resource.
		 * 
		 * @param resource the resource that changes
		 */
		public SafeResourceChange(IResource resource) {
			fChangedResource= resource;
		}
		
		/**
		 * @return the resource
		 */
		public IResource getChangedResource() {
			return fChangedResource;
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
			
			if (getElementInfo(fChangedResource) == null) {
				fireElementStateChangeFailed(fChangedResource);
				return;
			}
			
			try {
				execute();
			} catch (Exception x) {
				fireElementStateChangeFailed(fChangedResource);
			}
		}
	};
	
	
	/**
	 * Synchronizes the document with external resource changes.
	 */
	protected class ResourceSynchronizer implements IResourceChangeListener, IResourceDeltaVisitor {
		
		/** The resource. */
		protected IResource fSynchronizedResource;
		/** A flag indicating whether this synchronizer is installed or not. */
		protected boolean fIsInstalled= false;
		
		/**
		 * Creates a new resource synchronizer. Is not yet installed on a resource.
		 * 
		 * @param resource the resource with which the document is to be synchronized
		 */
		public ResourceSynchronizer(IResource resource) {
			fSynchronizedResource= resource;
		};
				
		/**
		 * Returns the resource associated with this synchronizer.
		 * 
		 * @return the resource associated with this synchronizer
		 */
		protected IResource getSynchronizedResource() {
			return fSynchronizedResource;
		}
		
		/**
		 * Installs the synchronizer on the resource.
		 */
		public void install() {
			getSynchronizedResource().getWorkspace().addResourceChangeListener(this);
			fIsInstalled= true;
		}
		
		/**
		 * Uninstalls the synchronizer from the resource.
		 */
		public void uninstall() {
			getSynchronizedResource().getWorkspace().removeResourceChangeListener(this);
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
				handleCoreException(x, "FileDocumentProvider.resourceChanged"); 
			}
		}
		
		/*
		 * @see IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
						
			if (delta != null && getSynchronizedResource().equals(delta.getResource())) {
				
				Runnable runnable= null;
				
				switch (delta.getKind()) {
					case IResourceDelta.CHANGED:
						if ((IResourceDelta.CONTENT & delta.getFlags()) != 0) {
							ResourceInfo info= (ResourceInfo) getElementInfo(fSynchronizedResource);
							if (info != null && !info.fCanBeSaved && fSynchronizedResource.isSynchronized(IResource.DEPTH_ZERO)) {
								runnable= new SafeResourceChange(fSynchronizedResource) {
									protected void execute() throws Exception {
										IResource r= getChangedResource();
										ResourceInfo i= (ResourceInfo) getElementInfo(r);
										if (i.fModificationStamp != r.getModificationStamp())
											handleElementContentChanged(getChangedResource());
									}
								};
							}
						}
						break;
					case IResourceDelta.REMOVED:
						if ((IResourceDelta.MOVED_TO & delta.getFlags()) != 0) {
							final IPath path= delta.getMovedToPath();
							runnable= new SafeResourceChange(fSynchronizedResource) {
								protected void execute() throws Exception {
									handleElementMoved(getChangedResource(), path);
								}
							};
						} else {
							ResourceInfo info= (ResourceInfo) getElementInfo(fSynchronizedResource);
							if (info != null && !info.fCanBeSaved) {
								runnable= new SafeResourceChange(fSynchronizedResource) {
									protected void execute() throws Exception {
										handleElementDeleted(getChangedResource());
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
			if (runnable instanceof SafeResourceChange)
				fireElementStateChanging(fSynchronizedResource);
			// TODO post behind operation
			runnable.run();			
		}
	};
	
	
	
	/**
	 * Bundle of all required information to allow resources as underlying document content providers. 
	 */
	protected class ResourceInfo extends StorageInfo {
		
		/** The resource synchronizer. */
		public ResourceSynchronizer fResourceSynchronizer;
		/** The time stamp at which this provider changed the file. */
		public long fModificationStamp= IResource.NULL_STAMP;
		
		/**
		 * Creates and returns a new resource info.
		 * 
		 * @param document the document
		 * @param resourceSynchronizer the file synchronizer
		 */
		public ResourceInfo(IDocument document, ResourceSynchronizer resourceSynchronizer) {
			super(document);
			fResourceSynchronizer= resourceSynchronizer;
		}
	};
	
	
	/** The document factory registry for this document provider */
	private ExtensionsRegistry fRegistry;
	
	
	/**
	 * Creates and returns a new document provider.
	 */
	public FileDocumentProvider2(ExtensionsRegistry registry) {
		super();
		fRegistry= registry;
	}
	
	/**
	 * Checks whether the given resource is synchronized with the the local file system. 
	 * If the resource has been changed, a <code>CoreException</code> is thrown.
	 * 
	 * @param resource the resource to check
	 * @exception CoreException if resource has been changed on the file system
	 */
	protected void checkSynchronizationState(IResource resource) throws CoreException {
		if (!resource.isSynchronized(IResource.DEPTH_ZERO)) {
			Status status= new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IResourceStatus.OUT_OF_SYNC_LOCAL, "FileDocumentProvider.error.out_of_sync", null); 
			throw new CoreException(status);
		}
	}
	
	/*
	 * @see AbstractDocumentProvider#doSaveDocument(IProgressMonitor, Object, IDocument, boolean)
	 */
	protected void doSaveDocument(Object element, IDocument document, boolean overwrite) throws CoreException {
		if (element instanceof IResource) {
						
			try {
			
			String encoding= getEncoding(element);
			if (encoding == null)
				encoding= getDefaultEncoding();
			InputStream stream= new ByteArrayInputStream(document.get().getBytes(encoding));
			IFile file= (IFile) element;
									
				if (file.exists()) {
					
					ResourceInfo info= (ResourceInfo) getElementInfo(element);
					
					if (info != null && !overwrite)
						checkSynchronizationState(file);
					
					// inform about the upcoming content change
					fireElementStateChanging(element);
					try {
						
						// here the resource synchronizer should actually be removed and afterwards added again. However,
						// we are already inside an operation, so the delta is sent AFTER we have added the listener
						file.setContents(stream, overwrite, true, getProgressMonitor());
						// set modification stamp to know whether the resource synchronizer must become active
						info.fModificationStamp= file.getModificationStamp();
						
					} catch (CoreException x) {
						// inform about failure
						fireElementStateChangeFailed(element);
						throw x;
					} catch (RuntimeException x) {
						// inform about failure
						fireElementStateChangeFailed(element);
						throw x;
					}
					
					// If here, the editor state will be flipped to "not dirty".
					// Thus, the state changing flag will be reset and we don't have to do
					// it manually
					
					// if there is an annotation model update it here
					
				} else {
					IProgressMonitor monitor= getProgressMonitor();
					try {
						monitor.beginTask("FileDocumentProvider.task.saving", 2000); //$NON-NLS-1$
						ContainerGenerator generator = new ContainerGenerator(file.getWorkspace(), file.getParent().getFullPath());
						generator.generateContainer(new SubProgressMonitor(monitor, 1000));
						file.create(stream, false, new SubProgressMonitor(monitor, 1000));
					}
					finally {
						monitor.done();
					}
				}
				
			} catch (IOException x) {
				IStatus s= new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IStatus.OK, x.getMessage(), x);
				throw new CoreException(s);
			}
			
		} else {
			super.doSaveDocument(element, document, overwrite);
		}
	}
	
	/*
	 * @see org.eclipse.core.internal.filebuffers.StorageDocumentProvider2#createEmptyDocument(java.lang.Object)
	 */
	public IDocument createEmptyDocument(Object element) {
		if (element instanceof IFile) {
			IDocumentFactory factory= fRegistry.getDocumentFactory((IFile) element);
			
			IDocument document= null;
			if (factory != null)
				document= factory.createDocument();
			else
				document= super.createEmptyDocument(element);
				
			IDocumentSetupParticipant[] participants= fRegistry.getDocumentSetupParticipants((IFile) element);
			if (participants != null) {
				for (int i= 0; i < participants.length; i++)
					participants[i].setup(document);
			}
			
			return document;
		}
		return super.createEmptyDocument(element);
	}
	
	/*
	 * @see AbstractDocumentProvider#createElementInfo(Object)
	 */
	protected ElementInfo createElementInfo(Object element) throws CoreException {
		if (element instanceof IResource) {
			
			try {
				refreshResource((IResource) element);
			} catch (CoreException x) {
				handleCoreException(x, "FileDocumentProvider.createElementInfo");
			}
			
			IDocument d= null;
			IStatus s= null;
			
			try {
				d= createDocument(element);
			} catch (CoreException x) {
				s= x.getStatus();
				d= createEmptyDocument(element);
			}
			
			ResourceSynchronizer f= new ResourceSynchronizer((IResource) element);
			f.install();
			
			ResourceInfo info= new ResourceInfo(d, f);
			info.fStatus= s;
			info.fEncoding= getPersistedEncoding(element);
			
			return info;
		}
		
		return super.createElementInfo(element);
	}
	
	/*
	 * @see AbstractDocumentProvider#disposeElementInfo(ElementInfo)
	 */
	protected void disposeElementInfo(ElementInfo info) {
		if (info instanceof ResourceInfo) {
			ResourceInfo resourceInfo= (ResourceInfo) info;
			if (resourceInfo.fResourceSynchronizer != null)
				resourceInfo.fResourceSynchronizer.uninstall();
		}
		
		super.disposeElementInfo(info);
	}	
	
	/**
	 * Updates the element info to a change of the file content and sends out appropriate notifications.
	 *
	 * @param object the changed object
	 */
	protected void handleElementContentChanged(IResource resource) {
		ResourceInfo info= (ResourceInfo) getElementInfo(resource);
		if (info == null)
			return;
		
		IDocument document= createEmptyDocument(resource);
		IStatus status= null;
		
		try {
			
//			Should not be neccessary.
//
//			try {
//				refreshResource(resource);
//			} catch (CoreException x) {
//				handleCoreException(x, "FileDocumentProvider.handleElementContentChanged"); //$NON-NLS-1$
//			}
			
			if (resource instanceof IFile)  {
				IFile file= (IFile) resource;
				setDocumentContent(document, file.getContents(false), info.fEncoding);
			}
			
		} catch (CoreException x) {
			status= x.getStatus();
		}
		
		String newContent= document.get();
		
		if ( !newContent.equals(info.fDocument.get())) {
			
			// set the new content and fire content related events
			fireElementContentAboutToBeReplaced(resource);
			
			removeUnchangedElementListeners(resource, info);
			
			info.fDocument.removeDocumentListener(info);
			info.fDocument.set(newContent);
			info.fCanBeSaved= false;
			info.fStatus= status;
			
			addUnchangedElementListeners(resource, info);
			
			fireElementContentReplaced(resource);
			
		} else {
			
			removeUnchangedElementListeners(resource, info);
			
			// fires only the dirty state related event
			info.fCanBeSaved= false;
			info.fStatus= status;
			
			addUnchangedElementListeners(resource, info);
			
			fireElementDirtyStateChanged(resource, false);
		}
	}
	
	/**
	 * Sends out the notification that the file serving as document input has been moved.
	 * 
	 * @param resource the changed resource
	 * @param path the path of the new location of the file
	 */
	protected void handleElementMoved(IResource resource, IPath path) {
		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		IFile newFile= workspace.getRoot().getFile(path);
		fireElementMoved(resource, newFile);
	}
	
	/**
	 * Sends out the notification that the file serving as document input has been deleted.
	 *
	 * @param resource the deleted resource
	 */
	protected void handleElementDeleted(IResource resource) {
		fireElementDeleted(resource);
	}
	
	/*
	 * @see AbstractDocumentProvider#doValidateState(Object, Object)
	 */
	protected void doValidateState(Object element, Object computationContext) throws CoreException {
		
		if (element instanceof IResource) {
			ResourceInfo info= (ResourceInfo) getElementInfo(element);
			if (info != null) {
				IResource resource= (IResource) element;
				if (resource.isReadOnly() && resource instanceof IFile) {
					IWorkspace workspace= resource.getWorkspace();
					workspace.validateEdit(new IFile[] { (IFile) resource }, computationContext);
				}
			}
		}
		
		super.doValidateState(element, computationContext);
	}
	
	/*
	 * @see IDocumentProvider#resetDocument(Object)
	 */
	public void restoreDocument(Object element) throws CoreException {
		if (element instanceof IResource) {
			try {
				refreshResource((IResource) element);
			} catch (CoreException x) {
				handleCoreException(x, "FileDocumentProvider.resetDocument");
			}
		}
		super.restoreDocument(element);	
	}
	
	/**
	 * Refreshes the given  resource.
	 * 
	 * @param resource the resource to be refreshed
	 * @throws  a CoreException if the refresh fails
	 */
	protected void refreshResource(IResource resource) throws CoreException {
		try {
			resource.refreshLocal(IResource.DEPTH_INFINITE, getProgressMonitor());
		} catch (OperationCanceledException x) {
		}
	}
	
	// --------------- Encoding support ---------------
	
	/**
	 * Returns the persited encoding for the given element.
	 * 
	 * @param element the element for which to get the persisted encoding
	 */
	protected String getPersistedEncoding(Object element) {
		if (element instanceof IResource) {
			try {
				IResource resource= (IResource) element;
				return resource.getPersistentProperty(ENCODING_KEY);
			} catch (CoreException ex) {
				return null;
			}
		}
		return super.getPersistedEncoding(element);
	}

	/**
	 * Persists the given encoding for the given element.
	 * 
	 * @param element the element for which to store the persisted encoding
	 * @param encoding the encoding
	 */
	protected void persistEncoding(Object element, String encoding) throws CoreException {
		if (element instanceof IResource) {
			IResource resource= (IResource) element;
			resource.setPersistentProperty(ENCODING_KEY, encoding);
		} else  {
			super.persistEncoding(element, encoding);
		}
	}
}
