/*
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2000, 2003.
 * All Rights Reserved. 
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP  schedule Contract with IBM Corp. 
*/

package org.eclipse.ui.internal.cheatsheets.registry;

import java.text.Collator;

import org.eclipse.jface.viewers.*;

/**
 *	A Viewer element sorter that sorts Elements by their name attribute.
 *	Note that capitalization differences are not considered by this
 *	sorter, so a < B < c.
 */
public class CheatSheetCollectionSorter extends ViewerSorter {
	public final static CheatSheetCollectionSorter INSTANCE = new CheatSheetCollectionSorter();
	private Collator collator = Collator.getInstance();

	/**
	 * Creates an instance of <code>NewWizardCollectionSorter</code>.  Since this
	 * is a stateless sorter, it is only accessible as a singleton; the private
	 * visibility of this constructor ensures this.
	 */
	private CheatSheetCollectionSorter() {
		super();
	}

	/**
	 * The 'compare' method of the sort operation.
	 *
	 * @return  the value <code>0</code> if the argument o1 is equal to o2;
	 * 			a value less than <code>0</code> if o1 is less than o2;
	 *			and a value greater than <code>0</code> if o1 is greater than o2.
	 */
	public int compare(Viewer viewer, Object o1, Object o2) {
		String name1 = ((CheatSheetCollectionElement) o1).getLabel(o1);
		String name2 = ((CheatSheetCollectionElement) o2).getLabel(o2);

		if (name1.equals(name2))
			return 0;

		return collator.compare(name1, name2);
	}

	/**
	 *	Return true if this sorter is affected by a property 
	 *	change of propertyName on the specified element.
	 */
	public boolean isSorterProperty(Object object, String propertyId) {
		return propertyId.equals(IBasicPropertyConstants.P_TEXT);
	}
}
