package org.eclipse.search.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import org.eclipse.search.ui.ISearchResultViewEntry;

/**
 * Sorts the search result viewer by the resource path.
 */
public class ResourcenameSorter extends ViewerSorter {
	/*
	 * Overrides method from ViewerSorter
	 */
	public int compare(Viewer viewer, Object e1, Object e2) {
		String name1= null;
		String name2= null;
		if (e1 instanceof ISearchResultViewEntry)
			name1= ((ISearchResultViewEntry)e1).getResource().getName();
		if (e2 instanceof ISearchResultViewEntry)
			name2= ((ISearchResultViewEntry)e2).getResource().getName();
		if (name1 == null)
			name1= "";
		if (name2 == null)
			name2= "";
		return name1.toLowerCase().compareTo(name2.toLowerCase());
	}
	/*
	 * Overrides method from ViewerSorter
	 */
	public boolean isSorterProperty(Object element, String property) {
		return true;
	}
}
	