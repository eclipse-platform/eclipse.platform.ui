package org.eclipse.jface.tests.viewers.interactive;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
import java.util.ArrayList;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class Filter extends ViewerFilter {

	public Object[] filter(Viewer viewer, Object parent, Object[] elements) {
		ArrayList result = new ArrayList();
		for (int i = 0; i < elements.length; ++i) {
			 // toss every second item
			if (i % 2 == 1) {
				result.add(elements[i]);
			}
		}
		return result.toArray();
	}
	public boolean isFilterProperty(Object element, Object aspect) {
		return false;
	}
/* (non-Javadoc)
 * Method declared on ViewerFilter
 */
public boolean select(Viewer viewer, Object parentElement, Object element) {
	// not used
	return false;
}
}
