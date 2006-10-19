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

import org.eclipse.compare.ISharedDocumentAdapter;
import org.eclipse.compare.ResourceNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.IEditorInput;

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
	private boolean exists;

	/**
	 * Creates an element for the given resource.
	 * @param resource the resource
	 */
	public LocalResourceTypedElement(IResource resource) {
		super(resource);
		exists = resource.exists();
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
		if (exists)
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
		return resource.getType() == IResource.FILE && exists;
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
	void updateTimestamp() {
		if (getResource().exists())
			timestamp = getResource().getLocalTimeStamp();
		else
			exists = false;
	}

	/**
	 * Return the cached timestamp of the resource.
	 * @return the cached timestamp of the resource
	 */
	private long getTimestamp() {
		return timestamp;
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
			return otherElement.getResource().equals(getResource()) 
				&& (otherElement.getTimestamp() == getTimestamp() || (!exists && !otherElement.exists));
		}
		return super.equals(other);
	}

	/**
	 * Method called to update the state of this element when the compare input that
	 * contains this element is issuing a change event. It is not necessarily the
	 * case that the {@link #isSynchronized()} will return <code>true</code> after this
	 * call.
	 */
	public void update() {
		exists = getResource().exists();
	}

	/**
	 * Return whether the contents provided by this typed element are in-sync with what is on
	 * disk. This method will return <code>false</code> if the file has been changed
	 * externally since the last time the contents were obtained or saved through this
	 * element.
	 * @return whether the contents provided by this typed element are in-sync with what is on
	 * disk
	 */
	public boolean isSynchronized() {
		long current = getResource().getLocalTimeStamp();
		return current == getTimestamp();
	}

	/**
	 * Return whether the resource of this element existed at the last time the typed
	 * element was updated.
	 * @return whether the resource of this element existed at the last time the typed
	 * element was updated
	 */
	public boolean exists() {
		return exists;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.BufferedContent#fireContentChanged()
	 */
	protected void fireContentChanged() {
		super.fireContentChanged();
	}

}
