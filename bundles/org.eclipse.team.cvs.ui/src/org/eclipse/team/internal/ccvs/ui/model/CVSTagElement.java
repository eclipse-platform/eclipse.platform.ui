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
package org.eclipse.team.internal.ccvs.ui.model;
 
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class CVSTagElement extends CVSModelElement implements IAdaptable {
	CVSTag tag;
	ICVSRepositoryLocation root;
	
	/**
	 * Create a branch tag
	 */
	public CVSTagElement(CVSTag tag, ICVSRepositoryLocation root) {
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
		if (!(o instanceof CVSTagElement)) return false;
		CVSTagElement t = (CVSTagElement)o;
		if (!tag.equals(t.tag)) return false;
		return root.equals(t.root);
	}
	public int hashCode() {
		return root.hashCode() ^ tag.hashCode();
	}
	
	/**
	 * @see org.eclipse.team.internal.ccvs.ui.model.CVSModelElement#internalGetChildren(java.lang.Object, org.eclipse.ui.IWorkingSet, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public Object[] internalGetChildren(Object o, IWorkingSet set, IProgressMonitor monitor) throws TeamException {
		ICVSRemoteResource[] children = CVSUIPlugin.getPlugin().getRepositoryManager().getFoldersForTag(root, tag, monitor);
		if (set != null)	
			children = CVSUIPlugin.getPlugin().getRepositoryManager().filterResources(set, children);
		return children;
	}
	
	/**
	 * Return children of the root with this tag.
	 */
	public Object[] internalGetChildren(Object o, IProgressMonitor monitor) throws TeamException {
		return internalGetChildren(o, null, monitor);
	}
	
	/**
	 * @see org.eclipse.team.internal.ccvs.ui.model.CVSModelElement#isRemoteElement()
	 */
	public boolean isRemoteElement() {
		return true;
	}
	public ImageDescriptor getImageDescriptor(Object object) {
		if (!(object instanceof CVSTagElement)) return null;
		if (tag.getType() == CVSTag.BRANCH || tag.getType() == CVSTag.HEAD) {
			return CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_TAG);
		} else if (tag.getType() == CVSTag.VERSION) {
			return CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_PROJECT_VERSION);
		} else {
			// This could be a Date tag
			return null;
		}
	}
	public String getLabel(Object o) {
		if (!(o instanceof CVSTagElement)) return null;
		return ((CVSTagElement)o).tag.getName();
	}
	public Object getParent(Object o) {
		if (!(o instanceof CVSTagElement)) return null;
		return ((CVSTagElement)o).root;
	}
}
