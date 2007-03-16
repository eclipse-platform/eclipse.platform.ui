/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp.data;
import javax.servlet.*;
import javax.servlet.http.*;

import org.eclipse.help.internal.base.*;
/**
 * Helper for pages in navigation frames. Used enabling/disabling activity
 * filtering
 */
public class ActivitiesData extends RequestData {
	/**
	 * Constructs the data for a request.
	 * 
	 * @param context
	 * @param request
	 */
	public ActivitiesData(ServletContext context, HttpServletRequest request,
			HttpServletResponse response) {
		super(context, request, response);
		String changeShowAll = request.getParameter("showAll"); //$NON-NLS-1$
		if (changeShowAll != null) {
			if ("off".equalsIgnoreCase(changeShowAll)) { //$NON-NLS-1$
				HelpBasePlugin.getActivitySupport().setFilteringEnabled(true);
			} else if ("on".equalsIgnoreCase(changeShowAll)) { //$NON-NLS-1$
				HelpBasePlugin.getActivitySupport().setFilteringEnabled(false);
			} else {
				// not supported value
			}
		} else {
			// no change to afilter
		}
		String confirmShowAll = request.getParameter("showconfirm"); //$NON-NLS-1$
		if ("false".equalsIgnoreCase(confirmShowAll)) { //$NON-NLS-1$
			preferences.setDontConfirmShowAll(true);
		}
	}
	/**
	 * @return Checks if filtering is enabled.
	 */
	public boolean isActivityFiltering() {
		return HelpBasePlugin.getActivitySupport().isFilteringEnabled();
	}
	/**
	 * Gives state of show all topics button
	 * 
	 * @return "hidden", "off", or "on"
	 */
	public String getButtonState() {
		if (!HelpBasePlugin.getActivitySupport().isUserCanToggleFiltering())
			return "hidden"; //$NON-NLS-1$
		else if (HelpBasePlugin.getActivitySupport().isFilteringEnabled())
			return "off"; //$NON-NLS-1$
		else
			return "on"; //$NON-NLS-1$
	}
}
