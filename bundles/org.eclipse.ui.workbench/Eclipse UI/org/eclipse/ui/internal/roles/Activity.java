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
package org.eclipse.ui.internal.roles;

/**
 * An Activity is a definition of a class of operations
 * within the workbench. It is defined with respect to 
 * a role.
 */
public class Activity {

	private String id;
	private String name;
	private String parent;
	boolean enabled;

	/**
	 * Create a new activity with the suppled id and name.
	 * This will be a top level Activity with no parent.
	 * @param newId
	 * @param newName
	 */
	Activity(String newId, String newName) {
		id = newId;
		name = newName;
	}

	/**
	 * Create a new instance of activity with a parent.
	 * @param newId
	 * @param newName
	 * @param newParent
	 */
	Activity(String newId, String newName, String newParent) {
		this(newId, newName);
		parent = newParent;
	}

	/**
	 * Return the id of the receiver.
	 * @return String
	 */
	public String getId() {
		return id;
	}

	/**
	 * Return the name of the receiver.
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Return the id of the parent of the receiver.
	 * @return String
	 */
	public String getParent() {
		return parent;
	}

	/**
	 * Return whether or not this activity is enabled.
	 * @return
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Set the enabled state of this activity.
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
