package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareUI;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;

/**
 * A class for comparing ICVSRemoteResource objects
 */
public class ResourceEditionNode implements IStructureComparator, ITypedElement, IStreamContentAccessor {
	private ICVSRemoteResource resource;
	private ResourceEditionNode[] children;
	
	/**
	 * Creates a new ResourceEditionNode on the given resource edition.
	 */
	public ResourceEditionNode(ICVSRemoteResource resourceEdition) {
		this.resource = resourceEdition;
	}
		
	/**
	 * Returns true if both resources names are identical.
	 * The content is not considered.
	 * @see IComparator#equals
	 */
	public boolean equals(Object other) {
		if (other instanceof ITypedElement) {
			String otherName = ((ITypedElement)other).getName();
			return getName().equals(otherName);
		}
		return super.equals(other);
	}
	
	/**
	 * Enumerate children of this node (if any).
	 */
	public Object[] getChildren() {
		if (children == null) {
			children = new ResourceEditionNode[0];
			if (resource != null) {
				try {
					CVSUIPlugin.runWithProgress(null, true /*cancelable*/, new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							try {
								IRemoteResource[] members = resource.members(monitor);
								children = new ResourceEditionNode[members.length];
								for (int i = 0; i < members.length; i++) {
									children[i] = new ResourceEditionNode((ICVSRemoteResource)members[i]);
								}
							} catch (TeamException e) {
								throw new InvocationTargetException(e);
							}
						}
					});
				} catch (InterruptedException e) {
					// operation canceled
				} catch (InvocationTargetException e) {
					Throwable t = e.getTargetException();
					if (t instanceof TeamException) {
						CVSUIPlugin.log(((TeamException) t).getStatus());
					}
				}
			}
		}
		return children;
	}

	/**
	 * @see IStreamContentAccessor#getContents()
	 */
	public InputStream getContents() throws CoreException {
		if (resource == null) {
			return null;
		}
		try {
			final InputStream[] holder = new InputStream[1];
			CVSUIPlugin.runWithProgress(null, true /*cancelable*/, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						holder[0] = resource.getContents(monitor);
					} catch (TeamException e) {
						throw new InvocationTargetException(e);
					}
				}
			});
			return holder[0];
		} catch (InterruptedException e) {
			// operation canceled
		} catch (InvocationTargetException e) {
			Throwable t = e.getTargetException();
			if (t instanceof TeamException) {
				throw new CoreException(((TeamException) t).getStatus());
			}
			// should not get here
		}
		return new ByteArrayInputStream(new byte[0]);
	}
	
	public Image getImage() {
		return CompareUI.getImage(resource);
	}
	
	/**
	 * Returns the name of this node.
	 */
	public String getName() {
		return resource == null ? "" : resource.getName(); //$NON-NLS-1$
	}
	
	public ICVSRemoteResource getRemoteResource() {
		return resource;
	}
	
	/**
	 * Returns the comparison type for this node.
	 */
	public String getType() {
		if (resource == null) {
			return UNKNOWN_TYPE;
		}
		if (resource.isContainer()) {
			return FOLDER_TYPE;
		}
		String name = resource.getName();
		name = name.substring(name.lastIndexOf('.') + 1);
		return name.length() == 0 ? UNKNOWN_TYPE : name;
	}
	
	/**
	 * @see IComparator#equals
	 */
	public int hashCode() {
		return getName().hashCode();
	}
}
