package org.eclipse.team.internal.ccvs.ui;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.team.ccvs.core.IRemoteRoot;
import org.eclipse.team.internal.ccvs.ui.model.Tag;

public class RepositorySorter extends ViewerSorter {
	public int compare(Viewer viewer, Object o1, Object o2) {
		if (o1 instanceof Tag && o2 instanceof Tag) {
			String tag1 = ((Tag)o1).getTag();
			if (tag1.equals("HEAD")) return -1;
			String tag2 = ((Tag)o2).getTag();
			if (tag2.equals("HEAD")) return 1;
			return tag1.compareTo(tag2);
		}
		if (o1 instanceof IRemoteRoot && o2 instanceof IRemoteRoot) {
			return ((IRemoteRoot)o1).getName().compareTo(((IRemoteRoot)o2).getName());
		}
		return super.compare(viewer, o1, o2);
	}
}

