/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.views;

import java.util.LinkedList;

import org.eclipse.help.IContext2;
import org.eclipse.help.IHelpResource;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

public class ContextHelpSorter extends ViewerComparator {
	private IContext2 context;
	private LinkedList list;
	
	public ContextHelpSorter(IContext2 context) {
		super(ReusableHelpPart.SHARED_COLLATOR);
		list = new LinkedList();
		this.context = context;
	}
	
	public void sort(Viewer viewer, Object[] elements) {
		for (int i = 0; i < elements.length; i++) {
			IHelpResource r1 = (IHelpResource) elements[i];
			String c1 = context.getCategory(r1);
			if (!list.contains(c1)) {
			    list.add(c1);
			}
		}
		super.sort(viewer, elements);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (!(e2 instanceof IHelpResource)) {
			return -1;
		}
		if (!(e1 instanceof IHelpResource)) {
			return 1;
		}
	    IHelpResource r1 = (IHelpResource) e1;
	    IHelpResource r2 = (IHelpResource) e2;
		String c1 = context.getCategory(r1);
		String c2 = context.getCategory(r2);
		int i1 = list.indexOf(c1);
		int i2 = list.indexOf(c2);
		return i1 - i2;
	}
}
