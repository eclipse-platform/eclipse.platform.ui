package org.eclipse.team.internal.ccvs.ui.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class BranchTag extends CVSModelElement implements IAdaptable {
	String tag;
	ICVSRepositoryLocation root;
	
	/**
	 * Create a branch tag
	 */
	public BranchTag(String tag, ICVSRepositoryLocation root) {
		this.tag = tag;
		this.root = root;
	}
	public ICVSRepositoryLocation getRoot() {
		return root;
	}
	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class) return this;
		return null;
	}
	public String getTag() {
		return tag;
	}
	public boolean equals(Object o) {
		if (!(o instanceof BranchTag)) return false;
		BranchTag t = (BranchTag)o;
		if (!tag.equals(t.tag)) return false;
		return root.equals(t.root);
	}
	/**
	 * Return children of the root with this tag.
	 */
	public Object[] getChildren(Object o) {
		if (!(o instanceof BranchTag)) return null;
		// Return the remote elements for the tag
		try {
			return root.members(tag, new NullProgressMonitor());
		} catch (TeamException e) {
			return null;
		}
	}
	public ImageDescriptor getImageDescriptor(Object object) {
		if (!(object instanceof BranchTag)) return null;
		return CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_TAG);
	}
	public String getLabel(Object o) {
		if (!(o instanceof BranchTag)) return null;
		return ((BranchTag)o).tag;
	}
	public Object getParent(Object o) {
		if (!(o instanceof BranchTag)) return null;
		return ((BranchTag)o).root;
	}
}

