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

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.viewers.LabelProvider;

import org.eclipse.ui.roles.IRole;
import org.eclipse.ui.roles.IRoleManager;
import org.eclipse.ui.roles.NotDefinedException;

/**
 * Provides labels for <code>IRole</code> objects. They may be passed
 * directly or as <code>String</code> identifiers that are matched against
 * the role manager.
 * 
 * @since 3.0
 */
public class RoleLabelProvider extends LabelProvider {

	private IRoleManager roleManager;

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param roleManager
	 * @since 3.0
	 */
	public RoleLabelProvider(IRoleManager roleManager) {
		this.roleManager = roleManager;
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
			return getRoleText(roleManager.getRole((String) element));
		} else if (element instanceof IRole) {
			return getRoleText((IRole) element);
		} else {
			throw new IllegalArgumentException();
		}
	}
}
