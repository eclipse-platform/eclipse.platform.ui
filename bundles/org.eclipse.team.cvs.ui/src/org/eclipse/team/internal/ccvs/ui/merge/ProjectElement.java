package org.eclipse.team.internal.ccvs.ui.merge;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.ICVSRemoteFolder;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class ProjectElement implements IAdaptable, IWorkbenchAdapter {
	ICVSRemoteFolder remote;
	Shell shell;
	
	public ProjectElement(ICVSRemoteFolder remote, Shell shell) {
		this.remote = remote;
		this.shell = shell;
	}
	public Object[] getChildren(Object o) {
		return new Object[] {
			new BranchesElement(remote),
			new TagElement(CVSTag.DEFAULT),
			new VersionsElement(remote, shell)
		};
	}
	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class) return this;
		return null;
	}
	public ImageDescriptor getImageDescriptor(Object object) {
		return null;
	}
	public String getLabel(Object o) {
		return null;
	}
	public Object getParent(Object o) {
		return null;
	}
}
