/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.repo;


import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.ui.model.BranchCategory;
import org.eclipse.team.internal.ccvs.ui.model.CVSTagElement;
import org.eclipse.team.internal.ccvs.ui.model.DateTagCategory;
import org.eclipse.team.internal.ccvs.ui.model.RemoteModule;
import org.eclipse.team.internal.ccvs.ui.model.VersionCategory;

public class RepositorySorter extends ViewerSorter {
	public int category(Object element) {
		if (element instanceof ICVSRemoteFolder) {
			if (((ICVSRemoteFolder)element).isDefinedModule()) {
				return 7;
			}
			return 1;
		}
		if (element instanceof RemoteModule) {
			ICVSRemoteResource resource = ((RemoteModule)element).getCVSResource();
			if (resource instanceof ICVSRemoteFolder) {
				ICVSRemoteFolder folder = (ICVSRemoteFolder) resource;
				if (folder.isDefinedModule()) {
					return 7;
				}
			}
			return 1;
		}
		if (element instanceof ICVSRemoteFile) {
			return 2;
		}
		if (element instanceof CVSTagElement) {
			CVSTagElement tagElement = (CVSTagElement)element;
			if (tagElement.getTag().getType() == CVSTag.HEAD) {
				return 0;
			} else if (tagElement.getTag().getType() == CVSTag.BRANCH) {
				return 4;
			} else if (tagElement.getTag().getType() == CVSTag.VERSION) {
				return 5;
			} else if (tagElement.getTag().getType() == CVSTag.DATE){
				return 6;
			}else{
				return 7;
			}
		}
		if (element instanceof BranchCategory) {
			return 4;
		}
		if (element instanceof VersionCategory) {
			return 5;
		} 
		if (element instanceof DateTagCategory){
			return 6;
		}
		return 0;
	}

	public int compare(Viewer viewer, Object o1, Object o2) {
		int cat1 = category(o1);
		int cat2 = category(o2);
		if (cat1 != cat2) return cat1 - cat2;
		
		if (o1 instanceof CVSTagElement && o2 instanceof CVSTagElement) {
			CVSTag tag1 = ((CVSTagElement)o1).getTag();
			CVSTag tag2 = ((CVSTagElement)o2).getTag();
			if (tag1.getType() == CVSTag.BRANCH) {
				return tag1.compareTo(tag2);
			} else {
				return -1 * tag1.compareTo(tag2);
			}
		}
		
		// Sort versions in reverse alphabetical order
		if (o1 instanceof ICVSRemoteFolder && o2 instanceof ICVSRemoteFolder) {
			ICVSRemoteFolder f1 = (ICVSRemoteFolder)o1;
			ICVSRemoteFolder f2 = (ICVSRemoteFolder)o2;
			if (f1.getName().equals(f2.getName())) {
				return compare(f1, f2);
			}
		}
		
		if (o1 instanceof ICVSRepositoryLocation && o2 instanceof ICVSRepositoryLocation) {
			return ((ICVSRepositoryLocation)o1).getLocation(false).compareTo(((ICVSRepositoryLocation)o2).getLocation(false));
		}
		
		return super.compare(viewer, o1, o2);
	}

	/*
	 * Compare to remote folders whose names are the same.
	 */
	private int compare(ICVSRemoteFolder f1, ICVSRemoteFolder f2) {
		CVSTag tag1 = f1.getTag();
		CVSTag tag2 = f2.getTag();
		if (tag1 == null) return 1;
		if (tag2 == null) return -1;
		return tag2.compareTo(tag1);
	}
}

