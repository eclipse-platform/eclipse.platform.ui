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
 * The Role is a set of information about the current static
 * working state.
 */
class Role {

	String name;
	String id;
	String[] activityIds;

	/**
	 * Create a new instance of the receiver.
	 * @param newName String
	 * @param newId String
	 * @param newActivityIds String[]
	 * @param newPatternBindings String[]
	 */
	Role(String newName, String newId, String[] newActivityIds) {
		name = newName;
		id = newId;
		activityIds = newActivityIds;
	}

	/**
	 * Set the enabled state of the activities in this role.
	 * @param set boolean
	 */
	public void setEnabled(boolean set) {
		for (int i = 0; i < activityIds.length; i++) {
			Activity activity = RoleManager.getInstance().getActivity(activityIds[i]);
			if (activity != null)
				activity.setEnabled(set);

		}
	}

	/**
	 * Return the ids of the activities that this role
	 * is bound to.
	 * @return String[]
	 */
	public String[] getActivityIds() {
		return activityIds;
	}

	/**
	 * Return whether or not all activites are enabled.
	 * Return false if any are disabled.
	 * @param set boolean
	 */
	public boolean allEnabled() {
		for (int i = 0; i < activityIds.length; i++) {
			Activity activity = RoleManager.getInstance().getActivity(activityIds[i]);
			if (activity != null)
				if (!activity.isEnabled())
					return false;
		}
		return true;
	}

}
