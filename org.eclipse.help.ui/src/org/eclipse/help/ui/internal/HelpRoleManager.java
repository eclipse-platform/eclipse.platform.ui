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
	private IWorkbenchActivitySupport activitySupport;
	private ICommandManager commandManager;
	public HelpRoleManager(IWorkbench workbench) {
		this.workbench = workbench;
		
		activitySupport = (IWorkbenchActivitySupport) workbench.getAdapter(IWorkbenchActivitySupport.class);
		commandManager = workbench.getCommandManager();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.internal.IHelpRoleManager#isEnabled()
	 */
	public boolean isEnabled(String href) {
		if (activitySupport == null) {
			return true;
		}

		// For the time being, only look at plugin id filtering
		if (href.startsWith("/"))
			href = href.substring(1);
		int i = href.indexOf("/");
		if (i > 0)
			href = href.substring(0, i);

        
        return activitySupport.getActivityManager().getIdentifier(href).isEnabled();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.internal.IHelpRoleManager#enabledActivities(java.lang.String)
	 */
	public void enabledActivities(String href) {
		if (activitySupport == null) {
			return;
		}

		// For the time being, only look at plugin id filtering
		if (href.startsWith("/"))
			href = href.substring(1);
		int i = href.indexOf("/");
		if (i > 0)
			href = href.substring(0, i);

        
        IIdentifier identifier = activitySupport.getActivityManager().getIdentifier(href);
        Set activitityIds = identifier.getActivityIds();
        if (activitityIds.isEmpty()) { // if there are no activities that match this identifier, do nothing.
            return;
        }
        
        Set enabledIds = new HashSet(activitySupport.getActivityManager().getEnabledActivityIds());
        enabledIds.addAll(activitityIds);
        activitySupport.setEnabledActivityIds(enabledIds);
	}

}
