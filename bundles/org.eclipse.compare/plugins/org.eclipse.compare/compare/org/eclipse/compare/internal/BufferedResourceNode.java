/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal;

import java.io.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.compare.*;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;

public class BufferedResourceNode extends ResourceNode {
	
	private boolean fDirty= false;
		
	/**
	 * Creates a <code>ResourceNode</code> for the given resource.
	 *
	 * @param resource the resource
	 */
	BufferedResourceNode(IResource resource) {
		super(resource);
	}
			
	protected IStructureComparator createChild(IResource child) {
		return new BufferedResourceNode(child);
	}
		
	public void setContent(byte[] contents) {
		fDirty= true;
		super.setContent(contents);
	}	

	/**
	 * Commits buffered contents to resource.
	 */
	public void commit(IProgressMonitor pm) throws CoreException {
		if (fDirty) {
			IResource resource= getResource();
			if (resource instanceof IFile) {
				ByteArrayInputStream is= new ByteArrayInputStream(getContent());
				try {
					IFile file= (IFile) resource;
					if (file.exists())
						file.setContents(is, false, true, pm);
					else
						file.create(is, false, pm);
					fDirty= false;
				} finally {
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
		
		if (child == null) {
			// create a node without a resource behind it!
			IResource resource= getResource();
			if (resource instanceof IFolder) {
				IFolder folder= (IFolder) resource;
				IFile file= folder.getFile(other.getName());
				child= new BufferedResourceNode(file);
			}
		}
		
		if (other instanceof IStreamContentAccessor && child instanceof IEditableContent) {
			IEditableContent dst= (IEditableContent) child;
			IStreamContentAccessor src= (IStreamContentAccessor) other;
			
			try {
				InputStream is= ((IStreamContentAccessor)other).getContents();
				byte[] bytes= Utilities.readBytes(is);
				if (bytes != null)
					dst.setContent(bytes);
			} catch (CoreException ex) {
			}
		}
		return child;
	}
}

