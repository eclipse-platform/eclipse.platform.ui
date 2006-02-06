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
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;



public class FlatFileSorter implements IFileSorter {

    private Collator fCollator;
	private Comparator fComparator;

	public FlatFileSorter(Collator collator) {
        fCollator= collator;
        fComparator= new Comparator() {
    		public int compare(Object o1, Object o2) {
    			return compareResources((IResource) o1, (IResource) o2);
    		}
    		private int compareResources(IResource r1, IResource r2) {
    			if (r1 == r2)
    				return 0;
    			if (r1 == null)
    				return -1;
    			if (r2 == null)
    				return 1;

    			int cmp= fCollator.compare(r1.getName(), r2.getName());
    			if (cmp == 0) {
    				cmp= compare(r1.getParent(), r2.getParent());
    			}
    			return cmp;
    		}
        };
    }

    public void sortFiles(IFile[] files) {
        Collections.sort(Arrays.asList(files), fComparator);
    }

}
