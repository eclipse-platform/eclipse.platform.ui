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
package org.eclipse.ui.internal.roles.ui;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.roles.IRole;
import org.eclipse.ui.roles.NotDefinedException;

/**
 * The RoleLabelProvider is a class that supplies the labels for the viewer in
 * the RolePreferencePage.
 */
public class RoleLabelProvider extends LabelProvider {

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @since 3.0
	 */
	public RoleLabelProvider() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		return null;
	}

	/**
	 * @param role
	 * @return @since 3.0
	 */
	private String getRoleText(IRole role) {
		try {
			return role.getName();
		} catch (NotDefinedException e) {
			return role.getId();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		if (element instanceof String) {
			return getRoleText(
				PlatformUI.getWorkbench().getRoleManager().getRole(
					(String) element));
		} else if (element instanceof IRole) {
			return getRoleText((IRole) element);
		} else {
			throw new IllegalArgumentException();
		}
	}
}
