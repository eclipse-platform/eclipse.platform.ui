package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.ui.model.BranchTag;

public class RepositorySorter extends ViewerSorter {
	public int compare(Viewer viewer, Object o1, Object o2) {
		if (o1 instanceof BranchTag && o2 instanceof BranchTag) {
			String tag1 = ((BranchTag)o1).getTag();
			String tag2 = ((BranchTag)o2).getTag();
			return tag1.compareTo(tag2);
		}
		if (o1 instanceof ICVSRepositoryLocation && o2 instanceof ICVSRepositoryLocation) {
			return ((ICVSRepositoryLocation)o1).getLocation().compareTo(((ICVSRepositoryLocation)o2).getLocation());
		}
		return super.compare(viewer, o1, o2);
	}
}

