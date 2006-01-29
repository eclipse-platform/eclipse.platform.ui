/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Markus Schorn - initial API and implementation 
 *******************************************************************************/

package org.eclipse.search2.internal.ui.text2;

import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.resources.IResource;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

public class RetrieverViewerSorter extends ViewerSorter {
	private final ILabelProvider fLabelProvider;

	public RetrieverViewerSorter(ILabelProvider labelProvider) {
		super(null); // lazy initialization
		fLabelProvider= labelProvider;
	}

	public int compare(Viewer viewer, Object e1, Object e2) {
		if (e1 instanceof RetrieverLine && e2 instanceof RetrieverLine) {
			return ((RetrieverLine) e1).getLineNumber() - ((RetrieverLine) e2).getLineNumber();
		}

		int diff= category(e1) - category(e2);
		if (diff != 0) {
			return diff;
		}

		String name1= fLabelProvider.getText(e1);
		String name2= fLabelProvider.getText(e2);
		if (name1 == null)
			name1= "";//$NON-NLS-1$
		if (name2 == null)
			name2= "";//$NON-NLS-1$
		return getCollator().compare(name1, name2);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerSorter#getCollator()
	 */
	public final Collator getCollator() {
		if (collator == null) {
			collator= Collator.getInstance();
		}
		return collator;
	}

	public int category(Object element) {
		if (element instanceof IResource) {
			return -((IResource) element).getType();
		}
		return 0;
	}

	public void sort(Viewer viewer, Object[] elements) {
		if (elements.length > 0) {
			Object o= elements[0];
			if (o instanceof RetrieverLine) {
				sortLines(viewer, elements);
				return;
			}
			super.sort(viewer, elements);
		}
	}

	private void sortLines(Viewer viewer, Object[] elements) {
		Arrays.sort(elements, new Comparator() {
			public int compare(Object a, Object b) {
				RetrieverLine l1= (RetrieverLine) a;
				RetrieverLine l2= (RetrieverLine) b;
				return l1.getLineNumber() - l2.getLineNumber();
			}
		});
	}


}
