package org.eclipse.team.internal.ccvs.ui.merge;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.ICVSFolder;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class ProjectElement implements IAdaptable, IWorkbenchAdapter {
	ICVSFolder project;
	boolean includeHeadTag;
	TagRootElement branches;
	TagRootElement versions;
	
	public ProjectElement(ICVSFolder project, boolean includeHeadTag) {
		this.project = project;
		this.includeHeadTag = includeHeadTag;		
		branches = new TagRootElement(project, CVSTag.BRANCH);
		versions = new TagRootElement(project, CVSTag.VERSION);
	}
	
	public Object[] getChildren(Object o) {
		if(includeHeadTag) {
			return new Object[] { branches, 
								   new TagElement(CVSTag.DEFAULT),
								   versions
								  			 };
		} else {
				return new Object[] {branches, versions};
		}
	}
	public TagRootElement getBranches() {
		return branches;
	}
	public TagRootElement getVersions() {
		return versions;
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
