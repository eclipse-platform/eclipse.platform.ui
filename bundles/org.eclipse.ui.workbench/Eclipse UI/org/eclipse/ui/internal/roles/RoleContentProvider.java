/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.roles;

import java.util.Collection;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * The RoleContentProvider is a class that supplies the contents for the
 * viewer in the RolePreferencePage.
 */
public class RoleContentProvider implements IStructuredContentProvider {

	/**
	 * Create a new instance of the receiver.
	 */
	public RoleContentProvider() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		Object [] roles = new Object[0];
		if (inputElement instanceof RoleManager) {
			roles = ((RoleManager)inputElement).getRoles();
		}
        else if (inputElement instanceof Collection) {
            roles = ((Collection)inputElement).toArray();
        }        
		return roles;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}
