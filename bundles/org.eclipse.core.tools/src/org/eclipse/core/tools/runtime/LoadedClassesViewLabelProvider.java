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

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osgi.framework.stats.ClassStats;
import org.eclipse.swt.graphics.Image;

/**
 * LabelProvider for the LoadedClassesView
 */

public class LoadedClassesViewLabelProvider extends LabelProvider implements ITableLabelProvider {

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (!(element instanceof ClassStats))
			return "not a class info"; //$NON-NLS-1$
		ClassStats clazz = (ClassStats) element;
		VMClassloaderInfo loader = VMClassloaderInfo.getClassloader(clazz.getClassloader().getId());
		VMClassInfo classInfo = loader.getClass(clazz.getClassName());

		switch (columnIndex) {
			case 0 : //"Class name"
				return clazz.getClassName() + (clazz.isStartupClass() ? "*" : ""); //$NON-NLS-1$ //$NON-NLS-2$
			case 1 : //		"Loading order",
				return clazz.getLoadOrder() == -2 ? "<boot>" : "" + clazz.getLoadOrder(); //$NON-NLS-1$ //$NON-NLS-2$
			case 2 : //		"Memory",
				return classInfo == null ? "" : "" + (classInfo.getRAMSize() + classInfo.getROMSize()); //$NON-NLS-1$ //$NON-NLS-2$
			case 3 : //		"Plugin Id", 
				return clazz.getClassloader().getId();
			case 4 : //		"Activation time",
				return "" + clazz.getTimestamp(); //$NON-NLS-1$
			case 5 : //		"RAM",
				return classInfo == null ? "" : "" + classInfo.getRAMSize(); //$NON-NLS-1$ //$NON-NLS-2$
			case 6 : //		"ROM"
				return classInfo == null ? "" : "" + classInfo.getROMSize(); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return ""; //$NON-NLS-1$
	}

}