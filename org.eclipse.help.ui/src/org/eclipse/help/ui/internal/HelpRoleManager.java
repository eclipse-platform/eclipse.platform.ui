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

package org.eclipse.help.ui.internal;

import org.eclipse.help.internal.*;
import org.eclipse.ui.internal.roles.*;

/**
 * Wrapper for eclipe ui role manager
 */
public class HelpRoleManager implements IHelpRoleManager {
	private RoleManager roleManager;
	public HelpRoleManager(RoleManager roleManager) {
		this.roleManager = roleManager;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.help.internal.IHelpRoleManager#isEnabled()
	 */
	public boolean isEnabled(String href) {
		if (roleManager == null || !roleManager.isFiltering())
			return true;

		// For the time being, only look at plugin id filtering
		if (href.startsWith("/"))
			href = href.substring(1);
		int i = href.indexOf("/");
		if (i > 0)
			href = href.substring(0,i);
		return roleManager.isEnabledId(href);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.internal.IHelpRoleManager#enabledActivities(java.lang.String)
	 */
	public void enabledActivities(String href) {
		if (roleManager == null || !roleManager.isFiltering())
			return;
		// For the time being, only look at plugin id filtering
		if (href.startsWith("/"))
			href = href.substring(1);
		int i = href.indexOf("/");
		if (i > 0)
			href = href.substring(0,i);
		if (!roleManager.isEnabledId(href))
			roleManager.enableActivities(href);
	}

}
