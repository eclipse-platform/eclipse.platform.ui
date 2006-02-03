/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.navigator.internal;

import java.text.Collator;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ui.navigator.TreeViewerSorter;

/**
 * @since 3.2
 *
 */
public class DelegateTreeViewerSorter extends TreeViewerSorter {
 
	private ViewerSorter delegateSorter;

	public DelegateTreeViewerSorter(ViewerSorter aSorter) {
		super();
		Assert.isNotNull(aSorter);
		delegateSorter = aSorter;
	}

	public int category(Object element) {
		return delegateSorter.category(element);
	}

	public int compare(Viewer viewer, Object e1, Object e2) {
		return delegateSorter.compare(viewer, e1, e2);
	}

	public boolean equals(Object obj) {
		return delegateSorter.equals(obj);
	}

	public Collator getCollator() {
		return delegateSorter.getCollator();
	}

	public int hashCode() {
		return delegateSorter.hashCode();
	}

	public boolean isSorterProperty(Object element, String property) {
		return delegateSorter.isSorterProperty(element, property);
	}

	public void sort(Viewer viewer, Object[] elements) {
		delegateSorter.sort(viewer, elements);
	}

	public String toString() {
		return delegateSorter.toString();
	}

}
