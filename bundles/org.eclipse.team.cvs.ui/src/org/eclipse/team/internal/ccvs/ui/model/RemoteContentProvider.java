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

import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.RemoteResource;
import org.eclipse.team.internal.ccvs.ui.jobs.*;
import org.eclipse.ui.IWorkingSet;

/**
 * Extension to the generic workbench content provider mechanism
 * to lazily determine whether an element has children.  That is,
 * children for an element aren't fetched until the user clicks
 * on the tree expansion box.
 */
public class RemoteContentProvider extends DeferredWorkbenchContentProvider {

	IWorkingSet workingSet;

	public boolean hasChildren(Object element) {
		// the + box will always appear, but then disappear
		// if not needed after you first click on it.
		if (element instanceof ICVSRemoteResource) {
			if (element instanceof ICVSRemoteFolder) {
				return ((ICVSRemoteFolder) element).isExpandable();
			}
			return ((ICVSRemoteResource) element).isContainer();
		} else if (element instanceof CVSResourceElement) {
			ICVSResource r = ((CVSResourceElement) element).getCVSResource();
			if (r instanceof RemoteResource) {
				return r.isFolder();
			}
		} else if (element instanceof VersionCategory) {
			return true;
		} else if (element instanceof BranchCategory) {
			return true;
		} else if (element instanceof ModulesCategory) {
			return true;
		} else if (element instanceof CVSTagElement) {
			return true;
		} else if (element instanceof RemoteModule) {
			return true;
		}
		return super.hasChildren(element);
	}

	/**
	 * Sets the workingSet.
	 * @param workingSet The workingSet to set
	 */
	public void setWorkingSet(IWorkingSet workingSet) {
		this.workingSet = workingSet;
	}

	/**
	 * Returns the workingSet.
	 * @return IWorkingSet
	 */
	public IWorkingSet getWorkingSet() {
		return workingSet;
	}
}
