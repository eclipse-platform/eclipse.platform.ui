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
 * An instance of <code>RoleEvent</code> describes changes to an instance of
 * <code>IRole</code>.
 * </p>
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see IRole
 * @see IRoleListener#roleChanged
 */
public final class RoleEvent {

	private boolean activityBindingsChanged;
	private boolean definedChanged;
	private boolean descriptionChanged;
	private boolean nameChanged;
	private IRole role;

	/**
	 * TODO javadoc
	 * 
	 * @param role
	 * @param activityBindingsChanged
	 * @param definedChanged
	 * @param descriptionChanged
	 * @param nameChanged
	 */
	public RoleEvent(
		IRole role,
		boolean activityBindingsChanged,
		boolean definedChanged,
		boolean descriptionChanged,
		boolean nameChanged) {
		if (role == null)
			throw new NullPointerException();

		this.role = role;
		this.activityBindingsChanged = activityBindingsChanged;
		this.definedChanged = definedChanged;
		this.descriptionChanged = descriptionChanged;
		this.nameChanged = nameChanged;
	}

	/**
	 * Returns the instance of <code>IRole</code> that has changed.
	 * 
	 * @return the instance of <code>IRole</code> that has changed.
	 *         Guaranteed not to be <code>null</code>.
	 */
	public IRole getRole() {
		return role;
	}

	/**
	 * TODO javadoc
	 */
	public boolean hasDefinedChanged() {
		return definedChanged;
	}

	/**
	 * TODO javadoc
	 */
	public boolean hasDescriptionChanged() {
		return descriptionChanged;
	}

	/**
	 * TODO javadoc
	 */
	public boolean hasNameChanged() {
		return nameChanged;
	}

	/**
	 * TODO javadoc
	 */
	public boolean haveActivityBindingsChanged() {
		return activityBindingsChanged;
	}
}
