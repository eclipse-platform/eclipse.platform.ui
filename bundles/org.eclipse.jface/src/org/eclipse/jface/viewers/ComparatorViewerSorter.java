/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.viewers;

import java.text.Collator;
import java.util.Comparator;

import org.eclipse.jface.util.Policy;

/**
 * A viewer comparator that uses a {@link Comparator} to sort the elements 
 * of a viewer.
 * 
 * @since 3.2
 *
 */
public class ComparatorViewerSorter extends ViewerSorter {
	/**
	 * The comparator to use to sort a viewer's contents.
	 */
	private Comparator comparator;

	/**
     * Creates a new {@link ComparatorViewerSorter}, which uses the default comparator
     * to sort strings.
	 *
	 */
	public ComparatorViewerSorter(){
		this(Policy.getComparator());
	}
	
	/**
     * Creates a new {@link ComparatorViewerSorter}, which uses the given comparator
     * to sort strings.
     * 
	 * @param comparator
	 */
	public ComparatorViewerSorter(Comparator comparator){
		this.comparator = comparator;
	}

	/**
	 * Returns the comparator used to sort strings.
	 * 
	 * @return the comparator used to sort strings
	 */
	protected Comparator getComparator() {
		return comparator;
	}

	/**
	 * Method overridden from superclass as {@link Collator} is not 
	 * supported in a {@link ComparatorViewerSorter}.
	 * 
	 * @exception UnsupportedOperationException
	 * @deprecated this method is not supported in this class
	 * @see #getComparator()
	 */
	public Collator getCollator() {
		throw new UnsupportedOperationException();
	}
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
    public int compare(Viewer viewer, Object e1, Object e2) {
        int cat1 = category(e1);
        int cat2 = category(e2);

        if (cat1 != cat2)
            return cat1 - cat2;

        String name1;
        String name2;

        if (viewer == null || !(viewer instanceof ContentViewer)) {
            name1 = e1.toString();
            name2 = e2.toString();
        } else {
            IBaseLabelProvider prov = ((ContentViewer) viewer)
                    .getLabelProvider();
            if (prov instanceof ILabelProvider) {
                ILabelProvider lprov = (ILabelProvider) prov;
                name1 = lprov.getText(e1);
                name2 = lprov.getText(e2);
            } else {
                name1 = e1.toString();
                name2 = e2.toString();
            }
        }
        if (name1 == null)
            name1 = "";//$NON-NLS-1$
        if (name2 == null)
            name2 = "";//$NON-NLS-1$
        
        // use comparator instead of collator
        return comparator.compare(name1, name2);
    }
	
}
