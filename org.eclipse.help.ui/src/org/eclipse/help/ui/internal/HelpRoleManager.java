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

import java.util.*;

import org.eclipse.help.internal.*;
import org.eclipse.ui.*;
import org.eclipse.ui.activities.*;
import org.eclipse.ui.commands.*;

/**
 * Wrapper for eclipe ui role manager
 */
public class HelpRoleManager implements IHelpRoleManager {
	private IWorkbench workbench;
	private IActivityManager activityManager;
	private ICommandManager commandManager;
	public HelpRoleManager(IWorkbench workbench) {
		this.workbench = workbench;
		activityManager = workbench.getActivityManager();
		commandManager = workbench.getCommandManager();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.internal.IHelpRoleManager#isEnabled()
	 */
	public boolean isEnabled(String href) {
		if (activityManager == null) {
			return true;
		}

		// For the time being, only look at plugin id filtering
		if (href.startsWith("/"))
			href = href.substring(1);
		int i = href.indexOf("/");
		if (i > 0)
			href = href.substring(0, i);

        
        return activityManager.getIdentifier(href).isEnabled();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.internal.IHelpRoleManager#enabledActivities(java.lang.String)
	 */
	public void enabledActivities(String href) {
		if (activityManager == null) {
			return;
		}

		// For the time being, only look at plugin id filtering
		if (href.startsWith("/"))
			href = href.substring(1);
		int i = href.indexOf("/");
		if (i > 0)
			href = href.substring(0, i);

        
        IIdentifier identifier = activityManager.getIdentifier(href);
        Set activitityIds = identifier.getActivityIds();
        if (activitityIds.isEmpty()) { // if there are no activities that match this identifier, do nothing.
            return;
        }
        
        Set enabledIds = new HashSet(activityManager.getEnabledActivityIds());
        enabledIds.addAll(activitityIds);
        workbench.setEnabledActivityIds(enabledIds);
	}

}
