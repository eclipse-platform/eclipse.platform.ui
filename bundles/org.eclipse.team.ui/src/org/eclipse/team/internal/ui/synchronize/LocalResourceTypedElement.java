/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize;

import java.io.*;

import org.eclipse.compare.*;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IElementStateListener;

/**
 * A resource node that is not buffered. Changes made to it are applied directly
 * to the underlying resource.
 * 
 * @since 3.0
 */
public class LocalResourceTypedElement extends ResourceNode implements IAdaptable {

	private boolean fDirty = false;
	private CountingSharedDocumentAdapter sharedDocumentAdapter;
	private long timestamp;

	/**
	 * A shared document adapter that tracks whether the element is
	 * connected to a shared document in order to ensure that
	 * any saves/commits that occur while connected are performed
	 * through the shared buffer.
	 */
	private static final class CountingSharedDocumentAdapter extends
			SharedDocumentAdapter implements IElementStateListener {
		
		private int connectionCount;
		private LocalResourceTypedElement element;

		/**
		 * Create the shared document adapter for the given element.
		 * @param element the element
		 */
		public CountingSharedDocumentAdapter(LocalResourceTypedElement element) {
			super();
			this.element = element;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.compare.SharedDocumentAdapter#connect(org.eclipse.ui.texteditor.IDocumentProvider, org.eclipse.ui.IEditorInput)
		 */
		public void connect(IDocumentProvider provider, IEditorInput documentKey)
				throws CoreException {
			super.connect(provider, documentKey);
			connectionCount++;
			if (connectionCount == 1) {
				provider.addElementStateListener(this);
				element.updateTimestamp();
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.compare.SharedDocumentAdapter#disconnect(org.eclipse.ui.texteditor.IDocumentProvider, org.eclipse.ui.IEditorInput)
		 */
		public void disconnect(IDocumentProvider provider,
				IEditorInput documentKey) {
			try {
				super.disconnect(provider, documentKey);
			} finally {
				if (connectionCount > 0)
					connectionCount--;
				if (connectionCount == 0) {
					provider.removeElementStateListener(this);
				}
			}
		}

		/**
		 * Return whether the element is connected to a shared document.
		 * @return whether the element is connected to a shared document
		 */
		public boolean isConnected() {
			return connectionCount > 0;
		}
		
		/**
		 * Save the shared document of the element of this adapter.
		 * @param input the document key of the element.
		 * @param monitor a progress monitor
		 * @return whether the save succeeded or not
		 * @throws CoreException
		 */
		public boolean saveDocument(IEditorInput input, IProgressMonitor monitor) throws CoreException {
			if (isConnected()) {
				IDocumentProvider provider = SharedDocumentAdapter.getDocumentProvider(input);
				saveDocument(provider, input, provider.getDocument(input), false, monitor);
				return true;
			}
			return false;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.texteditor.IElementStateListener#elementContentAboutToBeReplaced(java.lang.Object)
		 */
		public void elementContentAboutToBeReplaced(Object element) {
			// Nothing to do
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.texteditor.IElementStateListener#elementContentReplaced(java.lang.Object)
		 */
		public void elementContentReplaced(Object element) {
			// Nothing to do
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.texteditor.IElementStateListener#elementDeleted(java.lang.Object)
		 */
		public void elementDeleted(Object element) {
			// Nothing to do
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.texteditor.IElementStateListener#elementDirtyStateChanged(java.lang.Object, boolean)
		 */
		public void elementDirtyStateChanged(Object element, boolean isDirty) {
			if (!isDirty) {
				this.element.updateTimestamp();
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.texteditor.IElementStateListener#elementMoved(java.lang.Object, java.lang.Object)
		 */
		public void elementMoved(Object originalElement, Object movedElement) {
			// Nothing to do
		}
	}

	/**
	 * Creates an element for the given resource.
	 * @param resource the resource
	 */
	public LocalResourceTypedElement(IResource resource) {
		super(resource);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.ResourceNode#createChild(org.eclipse.core.resources.IResource)
	 */
	protected IStructureComparator createChild(IResource child) {
		return new LocalResourceTypedElement(child);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.BufferedContent#setContent(byte[])
	 */
	public void setContent(byte[] contents) {
		fDirty = true;
		super.setContent(contents);
	}

	/**
	 * Commits buffered contents to the underlying resource. Note that if the
	 * element has a shared document, the commit will not succeed since the 
	 * contents will be buffered in the shared document and will not be pushed
	 * to this element using {@link #setContent(byte[])}. Clients should check
	 * whether the element {@link #isConnected()} and, if it is, they should call
	 * {@link #saveDocument(IProgressMonitor)} to save the buffered contents to 
	 * the underlying resource.
	 * @param monitor a progress monitor
	 * @throws CoreException
	 */
	public void commit(IProgressMonitor monitor) throws CoreException {
		if (fDirty) {

			IResource resource = getResource();
			if (resource instanceof IFile) {
				ByteArrayInputStream is = new ByteArrayInputStream(getContent());
				try {
					IFile file = (IFile) resource;
					if (file.exists())
						file.setContents(is, false, true, monitor);
					else
						file.create(is, false, monitor);
					fDirty = false;
				} finally {
					fireContentChanged();
					if (is != null)
						try {
							is.close();
						} catch (IOException ex) {
						}
				}
			}
			
			updateTimestamp();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.ResourceNode#getContents()
	 */
	public InputStream getContents() throws CoreException {
		if (getResource().exists())
			return super.getContents();
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == ISharedDocumentAdapter.class) {
			return getSharedDocumentAdapter();
		}
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

	/*
	 * Returned the shared document adapter for this element. If one does not exist
	 * yet, it will be created.
	 */
	private synchronized ISharedDocumentAdapter getSharedDocumentAdapter() {
		if (sharedDocumentAdapter == null)
			sharedDocumentAdapter = new CountingSharedDocumentAdapter(this);
		return sharedDocumentAdapter;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.ResourceNode#isEditable()
	 */
	public boolean isEditable() {
		// Do not allow non-existent files to be edited
		IResource resource = getResource();
		return resource.getType() == IResource.FILE && resource.exists();
	}

	/**
	 * Return whether the element is connected to a shared document.
	 * When connected, the element can be saved using {@link #saveDocument(IProgressMonitor)}.
	 * Otherwise, {@link #commit(IProgressMonitor)} should be used to save the buffered contents.
	 * @return whether the element is connected to a shared document
	 */
	public boolean isConnected() {
		return sharedDocumentAdapter != null
				&& sharedDocumentAdapter.isConnected();
	}

	/**
	 * ZSave the shared document for this element. The save can only be performed
	 * if the element is connected to a shared document. If the element is not
	 * connected, <code>false</code> is returned.
	 * @param monitor a progress monitor
	 * @return whether the save succeeded or not
	 * @throws CoreException
	 */
	public boolean saveDocument(IProgressMonitor monitor) throws CoreException {
		if (isConnected()) {
			IEditorInput input = sharedDocumentAdapter.getDocumentKey(this);
			sharedDocumentAdapter.saveDocument(input, monitor);
			updateTimestamp();
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.ResourceNode#createStream()
	 */
	protected InputStream createStream() throws CoreException {
		InputStream inputStream = super.createStream();
		updateTimestamp();
		return inputStream;
	}
	
	/**
	 * Update the cached timestamp of the resource.
	 */
	private void updateTimestamp() {
		timestamp = getResource().getLocalTimeStamp();
	}

	/**
	 * Return the cached timestamp of the resource.
	 * @return the cached timestamp of the resource
	 */
	private long getTimestamp() {
		return timestamp;
	}
	
	/**
	 * Return whether the cached timestamp for the resource differs
	 * from the actual timestamp. If the timestamp differs, this indicates
	 * that the resource has been changed since this element obtained the
	 * contents from the resource.
	 * @return whether the cached timestamp for the resource differs
	 * from the actual timestamp
	 */
	public boolean hasSaveConflict() {
		long current = getResource().getLocalTimeStamp();
		return current != getTimestamp();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.ResourceNode#hashCode()
	 */
	public int hashCode() {
		return getResource().hashCode();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.ResourceNode#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		if (other instanceof LocalResourceTypedElement) {
			LocalResourceTypedElement otherElement = (LocalResourceTypedElement) other;
			return otherElement.getResource().equals(getResource()) && otherElement.getTimestamp() == getTimestamp();
		}
		return super.equals(other);
	}

}
