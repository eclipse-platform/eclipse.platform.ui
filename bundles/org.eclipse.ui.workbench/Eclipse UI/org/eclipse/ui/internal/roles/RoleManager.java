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
 * RoleManager is the type that defines and filters based on
 * role.
 */
public class RoleManager {

	private static RoleManager singleton;
	private boolean filterRoles = false;

	private Role[] roles;

	//The patterns for the mappings

	public static String JDT_PATTERN = "org.eclipse.jdt.*";
	public static String DEBUG_PATTERN = "org.eclipse.debug.*";
	public static String PDE_PATTERN = "org.eclipse.pde.*";
	public static String TEAM_PATTERN = "org.eclipse.team.*";
	public static String ANT_PATTERN = "org.eclipse.ant.*";
	public static String EXTERNAL_TOOLS_PATTERN = "org.eclipse.ui.externaltools";

	public static RoleManager getInstance() {
		if (singleton == null)
			singleton = new RoleManager();
		return singleton;

	}

	private RoleManager() {
		createDefaultRoles();
	}

	/**
	 * Create the hardcoded roles for the reciever.
	 * NOTE: These will be replaced by a proper sdk based
	 * extension system.
	 *
	 */
	private void createDefaultRoles() {
		roles = new Role[4];
		roles[0] =
			new Role(
				"Java Role",
				"org.eclipse.roles.javaRole",
				new String[] { JDT_PATTERN, DEBUG_PATTERN });
		roles[1] = new Role("PDE Role", "org.eclipse.roles.pdeRole", new String[] { PDE_PATTERN });
		roles[2] =
			new Role("Team Role", "org.eclipse.roles.teamRole", new String[] { TEAM_PATTERN });
		roles[3] =
			new Role(
				"External Tools Role",
				"org.eclipse.roles.externalToolsRole",
				new String[] { EXTERNAL_TOOLS_PATTERN, ANT_PATTERN });
	}

	/**
	 * Return whether or not the id is enabled. If there is a role
	 * whose pattern matches the id return whether or not the role is
	 * enabled. If there is no match return true;
	 * @param id
	 * @return
	 */
	public boolean isEnabledId(String id) {

		if (!filterRoles)
			return true;
		for (int i = 0; i < roles.length; i++) {
			if (roles[i].patternMatches(id))
				return roles[i].enabled;
		}
		return true;
	}

	/**
	 * Enable the roles that satisfy pattern.
	 * @param pattern
	 */
	public void enableRoles(String pattern) {
		if (!filterRoles)
			return;
		if(pattern == null)
			return;
		for (int i = 0; i < roles.length; i++) {
			if (roles[i].patternMatches(pattern))
				roles[i].setEnabled(true);
		}
	}

}
