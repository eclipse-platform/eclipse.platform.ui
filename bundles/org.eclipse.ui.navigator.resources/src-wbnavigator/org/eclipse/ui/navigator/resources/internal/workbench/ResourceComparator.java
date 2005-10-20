/*
 * Created on Jan 22, 2005
 * 
 * TODO To change the template for this generated file go to Window - Preferences - Java - Code
 * Style - Code Templates
 */
package org.eclipse.ui.navigator.resources.internal.workbench;

import java.util.Comparator;

import org.eclipse.ui.views.navigator.ResourceSorter;


class ResourceComparator implements Comparator {

	private ResourceSorter sorter = new ResourceSorter(ResourceSorter.NAME);

	/**
	 * The following compare will sort items based on their type (in the order of: ROOT, PROJECT,
	 * FOLDER, FILE) and then based on their String representation
	 * 
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object o1, Object o2) {
		return this.sorter.compare(null, o1, o2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {

		return obj instanceof ResourceComparator;
	}
}