package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.ui.model.BranchTag;

public class RepositorySorter extends ViewerSorter {
	public int category(Object element) {
		if (element instanceof ICVSRemoteFolder) {
			return 1;
		}
		if (element instanceof ICVSRemoteFile) {
			return 2;
		}
		return 0;
	}

	public int compare(Viewer viewer, Object o1, Object o2) {
		int cat1 = category(o1);
		int cat2 = category(o2);
		if (cat1 != cat2) return cat1 - cat2;
		
		if (o1 instanceof BranchTag && o2 instanceof BranchTag) {
			CVSTag tag1 = ((BranchTag)o1).getTag();
			CVSTag tag2 = ((BranchTag)o2).getTag();
			return tag1.compareTo(tag2);
		}
		if (o1 instanceof ICVSRepositoryLocation && o2 instanceof ICVSRepositoryLocation) {
			return ((ICVSRepositoryLocation)o1).getLocation().compareTo(((ICVSRepositoryLocation)o2).getLocation());
		}
		return super.compare(viewer, o1, o2);
	}
}

