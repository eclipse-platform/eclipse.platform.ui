/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.viewers.Viewer;

/**
 * The GroupedPreferenceContentProvider is the content provider
 * for showing preferences using groups instead of just categories.
 */
public class GroupedPreferenceContentProvider extends FilteredPreferenceContentProvider{

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferenceContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		if(inputElement instanceof WorkbenchPreferenceGroup)
			return ((WorkbenchPreferenceGroup) inputElement).getPreferenceNodes();
		return new Object[0];
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferenceContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if(newInput != null)
			viewer.refresh();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.dialogs.FilteredPreferenceContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		return null;
	}
	
}
