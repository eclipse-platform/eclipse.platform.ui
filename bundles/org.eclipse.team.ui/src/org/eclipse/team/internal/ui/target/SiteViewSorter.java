package org.eclipse.team.internal.ui.target;

import java.net.URL;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.team.core.target.Site;

public class SiteViewSorter extends ViewerSorter {
	
	public int category(Object element) {
		if (element instanceof Site) {
			return ((Site)element).getType().hashCode();
		}
		return 0;
	}

	public int compare(Viewer viewer, Object o1, Object o2) {
		int cat1 = category(o1);
		int cat2 = category(o2);
		if (cat1 != cat2) return cat1 - cat2;
		
		if (o1 instanceof Site && o2 instanceof Site) {
			URL site1 = ((Site)o1).getURL();
			URL site2 = ((Site)o2).getURL();
			return site1.toExternalForm().compareTo(site2.toExternalForm());
		}
		return super.compare(viewer, o1, o2);
	}
}
