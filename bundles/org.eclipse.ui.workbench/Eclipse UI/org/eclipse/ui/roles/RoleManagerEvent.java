/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.roles;

/**
 * <p>
 * An instance of <code>RoleManagerEvent</code> describes changes to an
 * instance of <code>IRoleManager</code>.
 * </p>
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see IRoleManager
 * @see IRoleManagerListener#roleManagerChanged
 */
public final class RoleManagerEvent {

	private boolean definedRoleIdsChanged;
	private IRoleManager roleManager;

	/**
	 * TODO javadoc
	 * 
	 * @param roleManager
	 * @param definedRoleIdsChanged
	 */
	public RoleManagerEvent(IRoleManager roleManager, boolean definedRoleIdsChanged) {
		if (roleManager == null)
			throw new NullPointerException();

		this.roleManager = roleManager;
		this.definedRoleIdsChanged = definedRoleIdsChanged;
	}

	/**
	 * Returns the instance of <code>IRoleManager</code> that has changed.
	 * 
	 * @return the instance of <code>IRoleManager</code> that has changed.
	 *         Guaranteed not to be <code>null</code>.
	 */
	public IRoleManager getRoleManager() {
		return roleManager;
	}

	/**
	 * TODO javadoc
	 */
	public boolean haveDefinedRoleIdsChanged() {
		return definedRoleIdsChanged;
	}
}
