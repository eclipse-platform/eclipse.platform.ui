/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel - <Lars.Vogel@vogella.com> - Bug 402464
 *******************************************************************************/
package org.eclipse.jface.viewers;

import java.text.Collator; // can't use ICU - Collator used in public API

/**
 * A viewer sorter is used by a {@link StructuredViewer} to reorder the elements
 * provided by its content provider.
 * <p>
 * The default <code>compare</code> method compares elements using two steps.
 * The first step uses the values returned from <code>category</code>. By
 * default, all elements are in the same category. The second level is based on
 * a case insensitive compare of the strings obtained from the content viewer's
 * label provider via <code>ILabelProvider.getText</code>.
 * </p>
 * <p>
 * Subclasses may implement the <code>isSorterProperty</code> method; they may
 * reimplement the <code>category</code> method to provide categorization; and
 * they may override the <code>compare</code> methods to provide a totally
 * different way of sorting elements.
 * </p>
 *
 * @deprecated use <code>ViewerComparator</code> instead.
 *
 * @see IStructuredContentProvider
 * @see StructuredViewer
 */
@Deprecated
public class ViewerSorter extends ViewerComparator {
	/**
	 * The collator used to sort strings.
	 *
	 * @deprecated as of 3.3 Use {@link ViewerComparator#getComparator()}
	 */
	@Deprecated
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
		super(collator);
		this.collator = collator;
	}

	/**
	 * Returns the collator used to sort strings.
	 *
	 * @return the collator used to sort strings
	 * @deprecated as of 3.3 Use {@link ViewerComparator#getComparator()}
	 */
	@Deprecated
	public Collator getCollator() {
		return collator;
	}

}
