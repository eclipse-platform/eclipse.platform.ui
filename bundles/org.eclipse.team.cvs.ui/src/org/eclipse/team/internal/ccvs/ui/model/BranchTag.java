package org.eclipse.team.internal.ccvs.ui.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class BranchTag extends CVSModelElement implements IAdaptable {
	CVSTag tag;
	ICVSRepositoryLocation root;
	
	/**
	 * Create a branch tag
	 */
	public BranchTag(CVSTag tag, ICVSRepositoryLocation root) {
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
	public CVSTag getTag() {
		return tag;
	}
	public boolean equals(Object o) {
		if (!(o instanceof BranchTag)) return false;
		BranchTag t = (BranchTag)o;
		if (!tag.equals(t.tag)) return false;
		return root.equals(t.root);
	}
	public int hashCode() {
		return root.hashCode() ^ tag.hashCode();
	}
	/**
	 * Return children of the root with this tag.
	 */
	public Object[] getChildren(Object o) {
		// Return the remote elements for the tag
		final Object[][] result = new Object[1][];
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			public void run() {
				try {
					IPreferenceStore store = CVSUIPlugin.getPlugin().getPreferenceStore();
					result[0] = root.members(tag, store.getBoolean(ICVSUIConstants.PREF_SHOW_MODULES), new NullProgressMonitor());
				} catch (TeamException e) {
					handle(e);
				}
			}
		});
		return result[0];
	}
	public ImageDescriptor getImageDescriptor(Object object) {
		if (!(object instanceof BranchTag)) return null;
		return CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_TAG);
	}
	public String getLabel(Object o) {
		if (!(o instanceof BranchTag)) return null;
		return ((BranchTag)o).tag.getName();
	}
	public Object getParent(Object o) {
		if (!(o instanceof BranchTag)) return null;
		return ((BranchTag)o).root;
	}
}