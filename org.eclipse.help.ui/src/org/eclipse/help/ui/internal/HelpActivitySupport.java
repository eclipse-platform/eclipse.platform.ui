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

import org.eclipse.help.internal.base.*;
import org.eclipse.ui.*;
import org.eclipse.ui.activities.*;

/**
 * Wrapper for eclipse ui activity support
 */
public class HelpActivitySupport implements IHelpActivitySupport {
	private IWorkbenchActivitySupport activitySupport;
	
	public HelpActivitySupport(IWorkbench workbench) {
		activitySupport = (IWorkbenchActivitySupport) workbench.getActivitySupport();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.internal.base.IHelpActivitySupport#isEnabled()
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
	 * @see org.eclipse.help.internal.base.IHelpActivitySupport#enableActivities(java.lang.String)
	 */
	public void enableActivities(String href) {
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
