/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.views;

import org.eclipse.help.IContext2;
import org.eclipse.help.IHelpResource;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

public class ContextHelpSorter extends ViewerComparator {
	private IContext2 context;
	
	public ContextHelpSorter(IContext2 context) {
		super(ReusableHelpPart.SHARED_COLLATOR);
		this.context = context;
	}
	
    public int category(Object element) {
		if (element instanceof IHelpResource) {
			IHelpResource r = (IHelpResource)element;
			String c = context.getCategory(r);
			if (c!=null) {
				return -5;
			}
		}
        return super.category(element);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public int compare(Viewer viewer, Object e1, Object e2) {
	    int cat1 = category(e1);
	    int cat2 = category(e2);

	    if (cat1 != cat2)
	    	return cat1 - cat2;
	    IHelpResource r1 = (IHelpResource) e1;
	    IHelpResource r2 = (IHelpResource) e2;
		String c1 = context.getCategory(r1);
		String c2 = context.getCategory(r2);
		if (c1!=null && c2!=null) {
			int cat = super.compare(viewer, c1, c2);
			if (cat!=0) return cat;
		}
	    return 0;
	}
}
