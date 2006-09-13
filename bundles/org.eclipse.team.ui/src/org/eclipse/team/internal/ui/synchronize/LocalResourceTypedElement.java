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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;

/**
 * A resource node that is not buffered. Changes made to it are applied directly 
 * to the underlying resource.
 * 
 * @since 3.0
 */
public class LocalResourceTypedElement extends ResourceNode implements ISharedDocumentAdapter {

		private boolean fDirty= false;
		private IFile fDeleteFile;
		
		/**
		 * Creates a <code>ResourceNode</code> for the given resource.
		 *
		 * @param resource the resource
		 */
		public LocalResourceTypedElement(IResource resource) {
			super(resource);
		}
			
		protected IStructureComparator createChild(IResource child) {
			return new LocalResourceTypedElement(child);
		}
		
		public void setContent(byte[] contents) {
			fDirty= true;
			super.setContent(contents);
		}
		
		/**
		 * Commits buffered contents to resource.
		 * @param monitor a progress monitor
		 * @throws CoreException 
		 */
		public void commit(IProgressMonitor monitor) throws CoreException {
			if (fDirty) {
			
				if (fDeleteFile != null) {
					fDeleteFile.delete(true, true, monitor);
					return;
				}
			
				IResource resource= getResource();
				if (resource instanceof IFile) {
					ByteArrayInputStream is= new ByteArrayInputStream(getContent());
					try {
						IFile file= (IFile) resource;
						if (file.exists())
							file.setContents(is, false, true, monitor);
						else
							file.create(is, false, monitor);
						fDirty= false;
					} finally {
						fireContentChanged();
						if (is != null)
							try {
								is.close();
							} catch(IOException ex) {
							}
					}
				}
			}
		}
	
		public ITypedElement replace(ITypedElement child, ITypedElement other) {
		
			if (child == null) {	// add resource
				// create a node without a resource behind it!
				IResource resource= getResource();
				if (resource instanceof IFolder) {
					IFolder folder= (IFolder) resource;
					IFile file= folder.getFile(other.getName());
					child= new LocalResourceTypedElement(file);
				}
			}
		
			if (other == null) {	// delete resource
				IResource resource= getResource();
				if (resource instanceof IFolder) {
					IFolder folder= (IFolder) resource;
					IFile file= folder.getFile(child.getName());
					if (file != null && file.exists()) {
						fDeleteFile= file;
						fDirty= true;
					}
				}
				return null;
			}
		
			if (other instanceof IStreamContentAccessor && child instanceof IEditableContent) {
				IEditableContent dst= (IEditableContent) child;
			
				try {
					InputStream is= ((IStreamContentAccessor)other).getContents();
					byte[] bytes= readBytes(is);
					if (bytes != null)
						dst.setContent(bytes);
				} catch (CoreException ex) {
				}
			}
			fireContentChanged();
			return child;
		}
		
	public static byte[] readBytes(InputStream in) {
			ByteArrayOutputStream bos= new ByteArrayOutputStream();
			try {		
				while (true) {
					int c= in.read();
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.ResourceNode#getContents()
	 */
	public InputStream getContents() throws CoreException {
		if(getResource().exists())
			return super.getContents();
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.ISharedDocumentAdapter#getDocumentKey(java.lang.Object)
	 */
	public IEditorInput getDocumentKey(Object element) {
		if (element == this && getResource() instanceof IFile) {
			IFile file = (IFile) getResource();
			// Only return the input if the file exists since file buffers doesn't seem to handle the create case
			if (file.exists())
				return new FileEditorInput(file);
		}
		return null;
	}
    
}
