package org.eclipse.team.internal.ccvs.ui.model;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.ccvs.core.IRemoteRoot;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class Tag extends CVSModelElement implements IAdaptable {
	String tag;
	IRemoteRoot root;
	
	public Tag(String tag, IRemoteRoot root) {
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
			return root.getMembers(tag, new NullProgressMonitor());
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

