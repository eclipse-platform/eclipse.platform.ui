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

import java.util.*;
import org.eclipse.core.tools.IFlattable;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.framework.stats.*;

/**
 * Content provider for the LoadedClassesView
 */
public class LoadedClassesViewContentProvider implements ITreeContentProvider, IFlattable {
	private boolean flat;
	private String inputNames[] = new String[0];

	public void setFlat(boolean mode) {
		flat = mode;
	}

	public Object[] getChildren(Object element) {
		if (flat || !(element instanceof ClassStats))
			return new Object[0];
		ArrayList result = new ArrayList(10);
		for (Iterator i = ((ClassStats) element).getLoadedClasses().iterator(); i.hasNext();) {
			ClassStats child = (ClassStats) i.next();
			if (filterChildren(child, inputNames))
				result.add(child);
		}
		return result.toArray(new Object[result.size()]);
	}

	private boolean filterChildren(ClassStats element, String[] filters) {
		String name = element.getClassloader().getId();
		for (int i = 0; i < filters.length; i++)
			if (filters[i].equals(name))
				return true;
		ArrayList children = element.getLoadedClasses();
		for (Iterator i = children.iterator(); i.hasNext();)
			if (filterChildren((ClassStats) i.next(), filters))
				return true;
		return false;
	}

	public Object getParent(Object element) {
		if (flat || !(element instanceof ClassStats))
			return null;
		return ((ClassStats) element).getLoadedBy();
	}

	public boolean hasChildren(Object element) {
		if (flat || !(element instanceof ClassStats))
			return false;
		for (Iterator i = ((ClassStats) element).getLoadedClasses().iterator(); i.hasNext();)
			if (filterChildren((ClassStats) i.next(), inputNames))
				return true;
		return false;
	}

	public Object[] getElements(Object inputElement) {
		if (!StatsManager.MONITOR_CLASSES)
			return null;
		if (inputElement instanceof Object[]) {
			Object[] elements = (Object[]) inputElement;
			if (elements.length == 0 || !(elements[0] instanceof BundleStats))
				return null;
			Set result = new HashSet(51);
			for (int i = 0; i < elements.length; i++) {
				ClassStats[] classes = getClasses(((BundleStats) elements[i]).getSymbolicName());
				result.addAll(Arrays.asList(classes));
			}
			return result.toArray(new Object[result.size()]);
		}
		return null;
	}

	private ClassStats[] getClasses(String id) {
		ClassloaderStats loader = ClassloaderStats.getLoader(id);
		if (loader == null)
			return new ClassStats[0];
		ClassStats[] classes = loader.getClasses();
		if (flat)
			return classes;
		ArrayList result = new ArrayList();
		for (int i = 0; i < classes.length; i++) {
			ClassStats target = classes[i];
			while (target.getLoadedBy() != null)
				target = target.getLoadedBy();
			result.add(target);
		}
		return (ClassStats[]) result.toArray(new ClassStats[result.size()]);
	}

	public void dispose() {
		// do nothing
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (oldInput == newInput)
			return;
		if (newInput == null) {
			inputNames = new String[0];
			return;
		}
		Object[] list = (Object[]) newInput;
		inputNames = new String[list.length];
		for (int i = 0; i < list.length; i++)
			inputNames[i] = ((BundleStats) list[i]).getSymbolicName();
	}

}