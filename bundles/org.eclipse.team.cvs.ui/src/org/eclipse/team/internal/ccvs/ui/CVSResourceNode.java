package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;

/**
 * This class is a wrapper for a CVSResource. We use it instead of the standard
 * ResourceNode because it can more accurately get its local children taking
 * into account ignored resources, CVS/ directories, etc.
 */
public class CVSResourceNode extends ResourceNode {
	ArrayList fChildren;
	
	public CVSResourceNode(IResource resource) {
		super(resource);
	}
	
	public Object[] getChildren() {
		if (fChildren == null) {
			fChildren= new ArrayList();
			IResource resource = getResource();
			if (resource instanceof IContainer) {
				try {
					ICVSFolder cvsFolder = CVSWorkspaceRoot.getCVSFolderFor((IContainer) resource);
					ICVSResource[] resources = cvsFolder.members(ICVSFolder.FILE_MEMBERS | ICVSFolder.FOLDER_MEMBERS);
					for (int i= 0; i < resources.length; i++) {
						IResource child;
						if (resources[i].isFolder()) {
							child = getFolder((IContainer)resource, resources[i].getName());
						} else {
							child = getFile((IContainer)resource, resources[i].getName());
						}
						if (child.exists()) {
							IStructureComparator childNode = createChild(child);
							if (childNode != null) {
								fChildren.add(childNode);
							}
						}
					}
				} catch (TeamException e) {
					CVSUIPlugin.log(e.getStatus());
				}
			}
		}
		return fChildren.toArray();
	}
	
	private IResource getFile(IContainer container, String name) {
		if (container instanceof IProject) {
			return ((IProject)container).getFile(name);
		}
		if (container instanceof IFolder) {
			return ((IFolder)container).getFile(name);
		}
		return null;
	}
	
	private IResource getFolder(IContainer container, String name) {
		if (container instanceof IProject) {
			return ((IProject)container).getFolder(name);
		}
		if (container instanceof IFolder) {
			return ((IFolder)container).getFolder(name);
		}
		return null;
	}
	
	protected IStructureComparator createChild(IResource child) {
		return new CVSResourceNode(child);
	}
	/*
	 * @see BufferedContent#setContent(byte[])
	 */
	public void setContent(byte[] contents) {
		try {
			((IFile)getResource()).setContents(new ByteArrayInputStream(contents), false, true, new NullProgressMonitor());
		} catch (CoreException e) {
			CVSUIPlugin.log(e.getStatus());
		}
	}
}
