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
import org.eclipse.osgi.framework.stats.BundleStats;

/**
 * Sorter used in the ActivePluginsView
 */
public class ActivePluginsViewSorter extends ViewerSorter implements ISorter {
	private boolean reversed = true;
	private int columnNumber;
	private Collator collator = Collator.getInstance();

	// column headings: "Plug-in", "Classes", "Alloc", "Used", "Startup time", "Order", "Timestamp", "Class load time", "Startup method time", "RAM Alloc", "RAM Used", "ROM Alloc", "ROM Used" };
	private int[][] SORT_ORDERS_BY_COLUMN = { //
	{0}, /* Plugin */{1, 0}, /* Count */{2, 0}, /* Alloc */{3, 0}, /* Used */{4, 0}, /* Startup time */{5, 0}, /* Order */{6, 0}, /* timestamp */{7, 0}, /* Class load time */{8, 0}, /* Startup method time */{9, 0}, /* RAM Alloc */{10, 0}, /* RAM Used */{11, 0}, /*	ROM Alloc */{12, 0} /* ROM Used */
	};

	public ActivePluginsViewSorter(int columnNumber) {
		this.columnNumber = columnNumber;
		if (columnNumber == 0)
			reversed = false;
	}

	/**
	 * Compares two stats objects, sorting first by the main column of this sorter,
	 * then by subsequent columns, depending on the column sort order.
	 */
	public int compare(Viewer viewer, Object o1, Object o2) {
		BundleStats plugin1 = (BundleStats) o1;
		BundleStats plugin2 = (BundleStats) o2;
		int[] columnSortOrder = SORT_ORDERS_BY_COLUMN[columnNumber];
		int result = 0;
		for (int i = 0; i < columnSortOrder.length; ++i) {
			result = compareColumnValue(columnSortOrder[i], plugin1, plugin2);
			if (result != 0)
				break;
		}
		return reversed ? -result : result;
	}

	/**
	 * Compares two markers, based only on the value of the specified column.
	 */
	int compareColumnValue(int column, BundleStats plugin1, BundleStats plugin2) {
		VMClassloaderInfo pluginInfo1 = VMClassloaderInfo.getClassloader(plugin1.getId());
		VMClassloaderInfo pluginInfo2 = VMClassloaderInfo.getClassloader(plugin2.getId());
		switch (column) {
			case 0 : /* Plugin ID */
				return collator.compare(plugin1.getId(), plugin2.getId());
			case 1 : /*Number Of classes loaded */
				return plugin1.getClassLoadCount() - plugin2.getClassLoadCount();
			case 2 : /* Total Mem Alloc */
				return (pluginInfo1.getAllocRAM() + pluginInfo1.getAllocROM()) - (pluginInfo2.getAllocRAM() + pluginInfo2.getAllocROM());
			case 3 : //Total Mem Used
				return (pluginInfo1.getUsedRAM() + pluginInfo1.getUsedROM()) - (pluginInfo2.getUsedRAM() + pluginInfo2.getUsedROM());
			case 4 : /* startup time */
				return (int) (plugin1.getStartupTime() - plugin2.getStartupTime());
			case 5 : /* actionvation order */
				return plugin1.getActivationOrder() - plugin2.getActivationOrder();
			case 6 : //Activation time
				return (int) (plugin1.getTimestamp() - plugin2.getTimestamp());
			case 7 : /*Class load time*/
				return (int) (plugin1.getClassLoadTime() - plugin2.getClassLoadTime());
			case 8 : /* Startup method time */
				return (int) (plugin1.getStartupMethodTime() - plugin2.getStartupMethodTime());
			case 9 : /* RAM Alloc */
				return pluginInfo1.getAllocRAM() - pluginInfo2.getAllocRAM();
			case 10 : /* RAM Alloc */
				return pluginInfo1.getUsedRAM() - pluginInfo2.getUsedRAM();
			case 11 :
				return pluginInfo1.getAllocROM() - pluginInfo2.getAllocROM();
			case 12 :
				return pluginInfo1.getUsedROM() - pluginInfo2.getUsedROM();
		}
		return 0;
	}

	public int getColumnNumber() {
		return columnNumber;
	}

	public boolean isReversed() {
		return reversed;
	}

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