package org.eclipse.ui.internal.csm.roles.api;

import org.eclipse.ui.internal.csm.roles.RoleManager;

public final class RoleManagerFactory {

	private static IRoleManager roleManager;

	public static IRoleManager getRoleManager() {
		if (roleManager == null)
			roleManager = new RoleManager();
		
		return roleManager;
	}
	
	private RoleManagerFactory() {		
	}
}
