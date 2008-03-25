/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.model;

import java.util.Comparator;

import org.eclipse.jface.util.Policy;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

/**
 * A ContributionComparator is capable of ordering
 * {@link IComparableContribution} instances, either as a
 * {@link ViewerComparator} (for {@link StructuredViewer}s) or as a traditional
 * {@link Comparator}.
 * 
 * This class orders contributions by first grouping by priority ({@link IComparableContribution#getPriority()})
 * and then by utilizing the JFace policy comparator to order by label ({@link IComparableContribution#getLabel()}).
 * 
 * @see IComparableContribution
 * 
 * @since 3.4
 */
public class ContributionComparator extends ViewerComparator implements
		Comparator {

	/**
	 * This implementation of {@link Comparator#compare(Object, Object)} does a
	 * blind cast on each element to {@link IComparableContribution}.
	 */
	public int compare(Object o1, Object o2) {
		return compare((IComparableContribution) o1,
				(IComparableContribution) o2);
	}

	/**
	 * Returns a negative, zero, or positive number depending on whether the
	 * first element is less than, equal to, or greater than the second element.
	 * <p>
	 * The default implementation of this method is based on comparing the
	 * elements' categories as computed by the <code>category</code> framework
	 * method. Elements within the same category are further subjected to a case
	 * insensitive compare of their label strings. Subclasses may override.
	 * </p>
	 * 
	 * @param c1
	 *            the first element
	 * @param c2
	 *            the second element
	 * @return a negative number if the first element is less than the second
	 *         element; the value <code>0</code> if the first element is equal
	 *         to the second element; and a positive number if the first element
	 *         is greater than the second element
	 */
	public int compare(IComparableContribution c1, IComparableContribution c2) {
		int cat1 = category(c1);
		int cat2 = category(c2);

		if (cat1 != cat2) {
			return cat1 - cat2;
		}

		String name1 = c1.getLabel();
		String name2 = c2.getLabel();

		if (name1 == null) {
			name1 = "";//$NON-NLS-1$
		}
		if (name2 == null) {
			name2 = "";//$NON-NLS-1$
		}

		// use the comparator to compare the strings
		return Policy.getComparator().compare(name1, name2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object)
	 */
	public int compare(Viewer viewer, Object e1, Object e2) {
		return compare(e1, e2);
	}

	/**
	 * Returns the category of the given element. The category is a number used
	 * to allocate elements to bins; the bins are arranged in ascending numeric
	 * order. The elements within a bin are arranged via a second level sort
	 * criterion.
	 * <p>
	 * The default implementation of this framework method returns the result of
	 * {@link IComparableContribution#getPriority()}. Subclasses may
	 * re-implement this method to provide non-trivial categorization.
	 * </p>
	 * 
	 * @param c
	 *            the element
	 * @return the category
	 */
	public int category(IComparableContribution c) {
		return c.getPriority();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ViewerComparator#category(java.lang.Object)
	 */
	public int category(Object element) {
		return category((IComparableContribution) element);
	}
}
