/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui;

import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * Sorts the search result viewer by the match as 
 * delivered by the viewers label provider.
 */
public class MatchSorter extends ViewerSorter {
	/*
	 * Overrides method from ViewerSorter
	 */
	public int compare(Viewer viewer, Object e1, Object e2) {
		String name1;
		String name2;
		
		if (!(viewer instanceof ContentViewer)) {
			name1= e1.toString();
			name2= e2.toString();
		} else {
			IBaseLabelProvider prov= ((ContentViewer) viewer).getLabelProvider();
			if (prov instanceof ILabelProvider) {
				ILabelProvider lprov= (ILabelProvider) prov;
				name1= lprov.getText(e1);
				name2= lprov.getText(e2);
			} else {
				name1= e1.toString();
				name2= e2.toString();
			}
		}
		if (name1 == null)
			name1= ""; //$NON-NLS-1$
		if (name2 == null)
			name2= ""; //$NON-NLS-1$

		return name1.toLowerCase().compareTo(name2.toLowerCase());
	}
	/*
	 * Overrides method from ViewerSorter
	 */
	public boolean isSorterProperty(Object element, String property) {
		return true;
	}
}

