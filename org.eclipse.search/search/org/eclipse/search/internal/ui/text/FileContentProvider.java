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
package org.eclipse.search.internal.ui.text;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.search.ui.text.AbstractTextSearchResult;
public abstract class FileContentProvider implements IStructuredContentProvider {
	protected final Object[] EMPTY_ARR= new Object[0];
	protected AbstractTextSearchResult fResult;
	public void dispose() {
		// nothing to do
	}
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof FileSearchResult) {
			initialize((FileSearchResult) newInput);
		}
	}
	protected void initialize(AbstractTextSearchResult result) {
		fResult= result;
	}
	
	public abstract void elementsChanged(Object[] updatedElements);
	public abstract void clear();
}
