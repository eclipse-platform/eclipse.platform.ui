package org.eclipse.team.internal.ccvs.ui.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.ccvs.core.IRemoteFolder;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class Tag extends CVSModelElement implements IAdaptable {
	String tag;
	ICVSRepositoryLocation root;
	
	public Tag(String tag, ICVSRepositoryLocation root) {
		this.tag = tag;
		this.root = root;
	}
	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class) return this;
		return null;
	}
	public String getTag() {
		return tag;
	}
	public boolean equals(Object o) {
		if (!(o instanceof Tag)) return false;
		Tag t = (Tag)o;
		return tag.equals(t.tag) && root.equals(t.root);
	}
	/**
	 * Return children of the root with this tag
	 */
	public Object[] getChildren(Object o) {
		if (!(o instanceof Tag)) return null;
		try {
			IRemoteFolder folder = root.getRemoteFolder(Path.EMPTY, tag);
			return folder.getMembers(new NullProgressMonitor());
		} catch (TeamException e) {
			return null;
		}
	}
	public ImageDescriptor getImageDescriptor(Object object) {
		if (!(object instanceof Tag)) return null;
		return CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_TAG);
	}
	public String getLabel(Object o) {
		if (!(o instanceof Tag)) return null;
		return ((Tag)o).tag;
	}
	public Object getParent(Object o) {
		if (!(o instanceof Tag)) return null;
		return ((Tag)o).root;
	}
}

