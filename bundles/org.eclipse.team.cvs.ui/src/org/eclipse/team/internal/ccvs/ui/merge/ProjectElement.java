/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.merge;


import java.util.ArrayList;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class ProjectElement implements IAdaptable, IWorkbenchAdapter {
	ICVSFolder project;
	TagRootElement branches;
	TagRootElement versions;
	int includeFlags;
	
	public static final int INCLUDE_HEAD_TAG = 1;
	public static final int INCLUDE_BASE_TAG = 2;
	public static final int INCLUDE_BRANCHES = 4;
	public static final int INCLUDE_VERSIONS = 8;
	public static final int INCLUDE_ALL_TAGS = INCLUDE_HEAD_TAG | INCLUDE_BASE_TAG | INCLUDE_BRANCHES | INCLUDE_VERSIONS;

	public static class ProjectElementSorter extends ViewerSorter {
		/*
		 * The order in the diaog should be HEAD, Branches, Versions, BASE
		 */
		public int category(Object element) {
			if (element instanceof TagElement) {
				CVSTag tag = ((TagElement)element).getTag();
				if (tag == CVSTag.DEFAULT) return 1;
				if (tag == CVSTag.BASE) return 4;
				if (tag.getType() == CVSTag.BRANCH) return 2;
				if (tag.getType() == CVSTag.VERSION) return 3;
			} else if (element instanceof TagRootElement) {
				return ((TagRootElement)element).getTypeOfTagRoot() == CVSTag.BRANCH ? 2 : 3;
			}
			return 0;
		}
		public int compare(Viewer viewer, Object e1, Object e2) {
			int cat1 = category(e1);
			int cat2 = category(e2);
			if (cat1 != cat2) return cat1 - cat2;
			// Sort version tags in reverse order
			if (e1 instanceof TagElement && ((TagElement)e1).getTag().getType() == CVSTag.VERSION) {
				return -1 * super.compare(viewer, e1, e2);
			}
			return super.compare(viewer, e1, e2);
		}
	}
		
	public ProjectElement(ICVSFolder project, int includeFlags) {
		this.project = project;
		this.includeFlags = includeFlags;
		if (this.includeFlags == 0) this.includeFlags = INCLUDE_ALL_TAGS;
		if ((includeFlags & INCLUDE_BRANCHES) > 0) {	
			branches = new TagRootElement(project, CVSTag.BRANCH);
		}
		if ((includeFlags & INCLUDE_VERSIONS) > 0) {
			versions = new TagRootElement(project, CVSTag.VERSION);
		}
	}
	
	public Object[] getChildren(Object o) {
		ArrayList children = new ArrayList(4);
		if ((includeFlags & INCLUDE_HEAD_TAG) > 0) {
			children.add(new TagElement(CVSTag.DEFAULT));
		}
		if ((includeFlags & INCLUDE_BASE_TAG) > 0) {
			children.add(new TagElement(CVSTag.BASE));
		}
		if ((includeFlags & INCLUDE_BRANCHES) > 0) {
			children.add(branches);
		}
		if ((includeFlags & INCLUDE_VERSIONS) > 0) {
			children.add(versions);
		}
		return (Object[]) children.toArray(new Object[children.size()]);
	}
	public int getIncludeFlags() {
		return includeFlags;
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
