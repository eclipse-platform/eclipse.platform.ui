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
import org.eclipse.core.resources.*;
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
public class LocalResourceTypedElement extends ResourceNode implements
		IAdaptable {

	private boolean fDirty = false;
	private IFile fDeleteFile;
	private CountingSharedDocumentAdapter sharedDocumentAdapter;
	private long timestamp;

	private static final class CountingSharedDocumentAdapter extends
			SharedDocumentAdapter implements IElementStateListener {
		private int connectionCount;

		private LocalResourceTypedElement element;

		public CountingSharedDocumentAdapter(LocalResourceTypedElement element) {
			super();
			this.element = element;
		}

		public void connect(IDocumentProvider provider, IEditorInput documentKey)
				throws CoreException {
			super.connect(provider, documentKey);
			connectionCount++;
			if (connectionCount == 1) {
				provider.addElementStateListener(this);
			}
		}

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

		public boolean isConnected() {
			return connectionCount > 0;
		}
		
		public boolean saveDocument(IEditorInput input, IProgressMonitor monitor) throws CoreException {
			if (isConnected()) {
				IDocumentProvider provider = SharedDocumentAdapter
						.getDocumentProvider(input);
				saveDocument(provider, input, provider
						.getDocument(input), false, monitor);
				return true;
			}
			return false;
		}

		public void elementContentAboutToBeReplaced(Object element) {
			// Nothing to do
		}

		public void elementContentReplaced(Object element) {
			// Nothing to do
		}

		public void elementDeleted(Object element) {
			// Nothing to do
		}

		public void elementDirtyStateChanged(Object element, boolean isDirty) {
			if (!isDirty) {
				this.element.updateTimestamp();
			}
		}

		public void elementMoved(Object originalElement, Object movedElement) {
			// Nothing to do
		}
	}

	/**
	 * Creates a <code>ResourceNode</code> for the given resource.
	 * 
	 * @param resource
	 *            the resource
	 */
	public LocalResourceTypedElement(IResource resource) {
		super(resource);
	}

	protected IStructureComparator createChild(IResource child) {
		return new LocalResourceTypedElement(child);
	}

	public void setContent(byte[] contents) {
		fDirty = true;
		super.setContent(contents);
	}

	/**
	 * Commits buffered contents to resource.
	 * 
	 * @param monitor
	 *            a progress monitor
	 * @throws CoreException
	 */
	public void commit(IProgressMonitor monitor) throws CoreException {
		if (fDirty) {

			if (fDeleteFile != null) {
				fDeleteFile.delete(true, true, monitor);
				return;
			}

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
		}
	}

	public ITypedElement replace(ITypedElement child, ITypedElement other) {

		if (child == null) { // add resource
			// create a node without a resource behind it!
			IResource resource = getResource();
			if (resource instanceof IFolder) {
				IFolder folder = (IFolder) resource;
				IFile file = folder.getFile(other.getName());
				child = (ITypedElement) createChild(file);
			}
		}

		if (other == null) { // delete resource
			IResource resource = getResource();
			if (resource instanceof IFolder) {
				IFolder folder = (IFolder) resource;
				IFile file = folder.getFile(child.getName());
				if (file != null && file.exists()) {
					fDeleteFile = file;
					fDirty = true;
				}
			}
			return null;
		}

		if (other instanceof IStreamContentAccessor
				&& child instanceof IEditableContent) {
			IEditableContent dst = (IEditableContent) child;

			try {
				InputStream is = ((IStreamContentAccessor) other).getContents();
				byte[] bytes = readBytes(is);
				if (bytes != null)
					dst.setContent(bytes);
			} catch (CoreException ex) {
			}
		}
		fireContentChanged();
		return child;
	}

	public static byte[] readBytes(InputStream in) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			while (true) {
				int c = in.read();
				if (c == -1)
					break;
				bos.write(c);
			}

		} catch (IOException ex) {
			return null;

		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException x) {
				}
			}
			try {
				bos.close();
			} catch (IOException x) {
			}
		}
		return bos.toByteArray();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.compare.ResourceNode#getContents()
	 */
	public InputStream getContents() throws CoreException {
		if (getResource().exists())
			return super.getContents();
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == ISharedDocumentAdapter.class) {
			synchronized (this) {
				if (sharedDocumentAdapter == null)
					sharedDocumentAdapter = new CountingSharedDocumentAdapter(
							this);
				return sharedDocumentAdapter;
			}
		}
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.compare.ResourceNode#isEditable()
	 */
	public boolean isEditable() {
		if (!getResource().exists() && isOutgoingDeletion()) {
			return false;
		}
		return super.isEditable();
	}

	protected boolean isOutgoingDeletion() {
		return false;
	}

	public boolean isConnected() {
		return sharedDocumentAdapter != null
				&& sharedDocumentAdapter.isConnected();
	}

	public void saveDocument(IProgressMonitor monitor) throws CoreException {
		if (isConnected()) {
			IEditorInput input = sharedDocumentAdapter.getDocumentKey(this);
			sharedDocumentAdapter.saveDocument(input, monitor);
			updateTimestamp();
		}
	}

	public void updateTimestamp() {
		timestamp = getResource().getLocalTimeStamp();
	}

	public long getTimestamp() {
		return timestamp;
	}
	
	public boolean hasSaveConflict() {
		long current = getResource().getLocalTimeStamp();
		return current != getTimestamp();
	}

}
