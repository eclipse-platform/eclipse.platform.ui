package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.InputStream;

import org.eclipse.compare.BufferedContent;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;

/**
 * A class for comparing ICVSRemoteResource objects
 */
public class ResourceEditionNode extends BufferedContent implements IStructureComparator, ITypedElement {
	private ICVSRemoteResource resource;
	private ResourceEditionNode[] children;
	
	/**
	 * Creates a new ResourceEditionNode on the given resource edition.
	 */
	public ResourceEditionNode(ICVSRemoteResource resourceEdition) {
		this.resource = resourceEdition;
	}
		
	/**
	 * @see BufferedContent#createStream
	 */
	public InputStream createStream() throws CoreException {
		if (resource == null) {
			return null;
		}
		try {
			return resource.getContents(new NullProgressMonitor());
		} catch (TeamException e) {
			throw new CoreException(e.getStatus());
		}
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
			if (resource == null) {
				children = new ResourceEditionNode[0];
			} else {
				try {
					IRemoteResource[] members = resource.members(new NullProgressMonitor());
					children = new ResourceEditionNode[members.length];
					for (int i = 0; i < members.length; i++) {
						children[i] = new ResourceEditionNode((ICVSRemoteResource)members[i]);
					}
				} catch (TeamException e) {
					CVSUIPlugin.log(e.getStatus());
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
		//show busy cursor if this is happening in the UI thread
		Display display = Display.getCurrent();
		if (display != null) {
			final InputStream[] stream = new InputStream[1];
			final TeamException[] exception = new TeamException[1];
			BusyIndicator.showWhile(display, new Runnable() {
				public void run() {
					try {
						stream[0] = resource.getContents(new NullProgressMonitor());
					} catch (TeamException e) {
						exception[0] = e;
					}
				}
			});
			if (exception[0] != null) {
				throw new CoreException(exception[0].getStatus());
			}
			return stream[0];
		} else {
			//we're not in the UI thread, just get the contents.
			try {
				return resource.getContents(new NullProgressMonitor());
			} catch (TeamException e) {
				throw new CoreException(e.getStatus());
			}
		}
	}
	
	public Image getImage() {
		return CompareUI.getImage(resource);
	}
	
	/**
	 * Returns the name of this node.
	 */
	public String getName() {
		return resource == null ? "" : resource.getName();
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
