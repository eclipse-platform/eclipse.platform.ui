/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.parts;


public abstract class Sorter {

public abstract boolean compare(Object elementOne, Object elementTwo);

private Object[] quickSort(Object[] sortedCollection, int left, int right) {
	int originalLeft = left;
	int originalRight = right;
	Object mid = sortedCollection[ (left + right) / 2];
	do {
		while (compare(sortedCollection[left], mid))
			left++;
		while (compare(mid, sortedCollection[right]))
			right--;
		if (left <= right) {
			Object tmp = sortedCollection[left];
			sortedCollection[left] = sortedCollection[right];
			sortedCollection[right] = tmp;
			left++;
			right--;
		}
	} while (left <= right);
	if (originalLeft < right)
		sortedCollection = quickSort(sortedCollection, originalLeft, right);
	if (left < originalRight)
		sortedCollection = quickSort(sortedCollection, left, originalRight);
	return sortedCollection;
}

public Object[] sort(Object[] unSortedCollection) {
	int size = unSortedCollection.length;
	Object[] sortedCollection = new Object[size];
	//copy the array so can return a new sorted collection	
	System.arraycopy(unSortedCollection, 0, sortedCollection, 0, size);
	if (size > 1)
		quickSort(sortedCollection, 0, size - 1);
	return sortedCollection;
}

public void sortInPlace(Object[] unSortedCollection) {
	int size = unSortedCollection.length;
	if (size > 1)
		quickSort(unSortedCollection, 0, size - 1);
}

}
