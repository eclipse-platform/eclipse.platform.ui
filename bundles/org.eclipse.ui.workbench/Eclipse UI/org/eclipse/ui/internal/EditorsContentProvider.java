/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * A content provider which provides the list of editors in an editor workbook.
 * The viewer's input must be set to an editor workbook.
 */
class EditorsContentProvider implements IStructuredContentProvider{
	
	private EditorWorkbook workbook;
	
	public EditorsContentProvider() {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		workbook = (EditorWorkbook) newInput;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider
	 */
	public Object[] getElements(Object inputElement) {
		if (workbook == null) {
			return new EditorPane[0];
		}
		return workbook.getEditors();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider
	 */
	public void dispose() {
	}
}
