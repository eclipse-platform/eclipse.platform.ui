/**********************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tools.runtime;

import java.text.Collator;
import org.eclipse.core.tools.ISorter;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.osgi.framework.stats.ClassStats;

public class LoadedClassesViewSorter extends ViewerSorter implements ISorter {
	private boolean reversed = true;
	private int columnNumber;
	private Collator collator = Collator.getInstance();

	//	columnHeaders = new String[]{ "Class", "Order", "Memory", "Plugin", "Timestamp", "RAM", "ROM" };
	private int[][] SORT_ORDERS_BY_COLUMN = { //
		{ 0, 3 }, /* Class */ {
			1, 0, 3 }, /* Order */ {
			2, 0, 3 }, /* Memory*/ {
			3, 0 }, /* Plugin */ {
			4, 0, 3 }, /* Timestamp */ {
			5, 0, 3 }, /* RAM */ {
			6, 0, 3 }, /* ROM */
	};

	public LoadedClassesViewSorter(int columnNumber) {
		this.columnNumber = columnNumber;
		if (columnNumber == 0)
			reversed = false;
	}
	/**
	 * Compares two stats objects, sorting first by the main column of this sorter,
	 * then by subsequent columns, depending on the column sort order.
	 */
	public int compare(Viewer viewer, Object o1, Object o2) {
		ClassStats class1 = (ClassStats) o1;
		ClassStats class2 = (ClassStats) o2;

		int[] columnSortOrder;
		columnSortOrder = SORT_ORDERS_BY_COLUMN[columnNumber];

		int result = 0;
		for (int i = 0; i < columnSortOrder.length; ++i) {
			result = compareColumnValue(columnSortOrder[i], class1, class2);
			if (result != 0)
				break;
		}
		if (reversed)
			result = -result;
		return result;
	}
	/**
	 * Compares two markers, based only on the value of the specified column.
	 */
	private int compareColumnValue(int columnNumber, ClassStats class1, ClassStats class2) {
		VMClassloaderInfo loader = VMClassloaderInfo.getClassloader(class1.getClassloader().getId());
		VMClassInfo classInfo1 = loader.getClass(class1.getClassName());
		loader = VMClassloaderInfo.getClassloader(class2.getClassloader().getId());
		VMClassInfo classInfo2 = loader.getClass(class2.getClassName());
		switch (columnNumber) {
			case 0 :
				{ /* class name */
					String package1 = getPackageName(class1.getClassName());
					String package2 = getPackageName(class2.getClassName());
					int result = collator.compare(package1, package2);
					return result != 0 ? result : collator.compare(class1.getClassName(), class2.getClassName());
				}
			case 1 : /*Loading order */
				return class1.getLoadOrder() - class2.getLoadOrder();
			case 2 : /* Memory */
				return (classInfo1.getRAMSize() + classInfo1.getROMSize()) - (classInfo2.getRAMSize() + classInfo2.getROMSize());
			case 3 : /* Plugin Id */
				return collator.compare(class1.getClassloader().getId(), class2.getClassloader().getId());
			case 4 : /* Activation time */
				return (int) (class1.getTimestamp() - class2.getTimestamp());
			case 5 : /* RAM */
				return classInfo1.getRAMSize() - classInfo2.getRAMSize();
			case 6 : /* ROM */
				return classInfo1.getROMSize() - classInfo2.getROMSize();
		}
		return 0;
	}

	private String getPackageName(String className) {
		int index = className.lastIndexOf('.');
		return index == -1 ? "" : className.substring(0, index); //$NON-NLS-1$
	}

	/**
	 * Returns the number of the column by which this is sorting.
	 */
	public int getColumnNumber() {
		return columnNumber;
	}
	/**
	 * Returns true for descending, or false for ascending sorting order.
	 */
	public boolean isReversed() {
		return reversed;
	}
	/**
	 * Sets the sorting order.
	 */
	public void setReversed(boolean value) {
		reversed = value;
	}
	/**
	 * @see org.eclipse.core.tools.ISorter#states()
	 */
	public int states() {
		return 3;
	}
}
