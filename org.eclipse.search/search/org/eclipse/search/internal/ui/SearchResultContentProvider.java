/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui;

import java.util.ArrayList;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

class SearchResultContentProvider implements IStructuredContentProvider {
	
	private static final Object[] fgEmptyArray= new Object[0];
	
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// Do nothing since the viewer listens to resource deltas
	}
	
	public void dispose() {
	}
	
	public boolean isDeleted(Object element) {
		return false;
	}
	
	public Object[] getElements(Object element) {
		if (element instanceof ArrayList)
			return ((ArrayList)element).toArray();
		else
			return fgEmptyArray;
	}
}
