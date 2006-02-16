/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers;

import java.text.Collator;
import java.util.Comparator;

/**
 * A viewer sorter is used by a {@link StructuredViewer} to reorder the elements 
 * provided by its content provider.
 * <p>
 * The default <code>compare</code> method compares elements using two steps. 
 * The first step uses the values returned from <code>category</code>. 
 * By default, all elements are in the same category. 
 * The second level is based on a case insensitive compare of the strings obtained 
 * from the content viewer's label provider via <code>ILabelProvider.getText</code>.
 * </p>
 * <p>
 * Subclasses may implement the <code>isSorterProperty</code> method;
 * they may reimplement the <code>category</code> method to provide 
 * categorization; and they may override the <code>compare</code> methods
 * to provide a totally different way of sorting elements.
 * </p>
 * @see IStructuredContentProvider
 * @see StructuredViewer
 */
public class ViewerSorter extends ViewerComparator{
    /**
     * The collator used to sort strings.
     */
    protected Collator collator;

    /**
     * Creates a new viewer sorter, which uses the default collator
     * to sort strings.
     */
    public ViewerSorter() {
        this(Collator.getInstance());
    }

    /**
     * Creates a new viewer sorter, which uses the given collator
     * to sort strings.
     *
     * @param collator the collator to use to sort strings
     */
    public ViewerSorter(Collator collator) {
        this.collator = collator;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
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
        
        // use a collator to compare the strings
        return collator.compare(name1, name2);
    }

    /**
     * Returns the collator used to sort strings.
     *
     * @return the collator used to sort strings
     */
    public Collator getCollator() {
        return collator;
    }

	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerComparator#getComparator()
	 */
	protected Comparator getComparator() {
		return getCollator();
	}
}
