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

import org.eclipse.jface.viewers.*;
import org.eclipse.osgi.framework.stats.BundleStats;
import org.eclipse.swt.graphics.Image;

/**
 * Label provider for the ActivePluginsView
 *
 */
public class ActivePluginsViewLabelProvider extends LabelProvider implements ITableLabelProvider {

	public void addListener(ILabelProviderListener listener) {
		// do nothing
	}

	public void dispose() {
		// do nothing
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		// do nothing
	}

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (!(element instanceof BundleStats))
			return "not a plug-in"; //$NON-NLS-1$
		BundleStats info = (BundleStats) element;
		if (info == null)
			return "no info for plug-in"; //$NON-NLS-1$
		VMClassloaderInfo loaderInfo = VMClassloaderInfo.getClassloader(info.getSymbolicName());
		switch (columnIndex) {
			case 0 : /* id */
				return info.getSymbolicName() + (info.isStartupBundle() ? "*" : ""); //$NON-NLS-1$ //$NON-NLS-2$
			case 1 : /* class load count */
				return "" + info.getClassLoadCount(); //$NON-NLS-1$
			case 2 : /* Total Mem Alloc */
				return "" + (loaderInfo.getAllocRAM() + loaderInfo.getAllocROM()); //$NON-NLS-1$
			case 3 : /* Total Mem Used */
				return "" + (loaderInfo.getUsedRAM() + loaderInfo.getUsedROM()); //$NON-NLS-1$
			case 4 : /* startup time */
				return "" + (info.getStartupTime()); //$NON-NLS-1$
			case 5 : /* activation order */
				return "" + info.getActivationOrder(); //$NON-NLS-1$
			case 6 : /* activation time */
				return "" + (int) (info.getTimestamp()); //$NON-NLS-1$
			case 7 : /* class load time*/
				return "" + (int) (info.getClassLoadTime()); //$NON-NLS-1$
			case 8 : /* startup method time */
				return "" + (int) (info.getStartupMethodTime()); //$NON-NLS-1$
			case 9 : /* RAM alloc */
				return "" + loaderInfo.getAllocRAM(); //$NON-NLS-1$
			case 10 : /* RAM used */
				return "" + loaderInfo.getUsedRAM(); //$NON-NLS-1$
			case 11 : /* ROM alloc */
				return "" + loaderInfo.getAllocROM(); //$NON-NLS-1$
			case 12 : /* ROM used */
				return "" + loaderInfo.getUsedROM(); //$NON-NLS-1$
		}
		return null;
	}

}