/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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

import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.base.*;
import org.eclipse.help.internal.model.*;
import org.eclipse.ui.*;
import org.eclipse.ui.activities.*;

/**
 * Wrapper for eclipse ui activity support
 */
public class HelpActivitySupport implements IHelpActivitySupport {
	private static final String PREF_KEY_SHOW_DISABLED_ACTIVITIES = "showDisabledActivityTopics"; //$NON-NLS-1$
	private static final String SHOW_DISABLED_ACTIVITIES_NEVER = "never"; //$NON-NLS-1$
	private static final String SHOW_DISABLED_ACTIVITIES_OFF = "off"; //$NON-NLS-1$
	private static final String SHOW_DISABLED_ACTIVITIES_ON = "on"; //$NON-NLS-1$
	private static final String SHOW_DISABLED_ACTIVITIES_ALWAYS = "always"; //$NON-NLS-1$

	private Preferences pref;
	private IWorkbenchActivitySupport activitySupport;
	private boolean userCanToggleFiltering;
	private boolean filteringEnabled;

	public HelpActivitySupport(IWorkbench workbench) {
		activitySupport = workbench.getActivitySupport();
		pref = HelpBasePlugin.getDefault().getPluginPreferences();

		String showDisabledActivities = pref
				.getString(PREF_KEY_SHOW_DISABLED_ACTIVITIES);
		userCanToggleFiltering = SHOW_DISABLED_ACTIVITIES_OFF
				.equalsIgnoreCase(showDisabledActivities)
				|| SHOW_DISABLED_ACTIVITIES_ON
						.equalsIgnoreCase(showDisabledActivities);
		userCanToggleFiltering = userCanToggleFiltering
				&& isWorkbenchFiltering();

		filteringEnabled = SHOW_DISABLED_ACTIVITIES_OFF
				.equalsIgnoreCase(showDisabledActivities)
				|| SHOW_DISABLED_ACTIVITIES_NEVER
						.equalsIgnoreCase(showDisabledActivities);
		filteringEnabled = filteringEnabled && isWorkbenchFiltering();
	}
	public boolean isFilteringEnabled() {
		return filteringEnabled;
	}
	public void setFilteringEnabled(boolean enabled) {
		if (userCanToggleFiltering) {
			filteringEnabled = enabled;
			if (enabled) {
				pref.setValue(PREF_KEY_SHOW_DISABLED_ACTIVITIES,
						SHOW_DISABLED_ACTIVITIES_OFF);
			} else {
				pref.setValue(PREF_KEY_SHOW_DISABLED_ACTIVITIES,
						SHOW_DISABLED_ACTIVITIES_ON);
			}
		}
	}
	public boolean isUserCanToggleFiltering() {
		return userCanToggleFiltering;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.internal.base.IHelpActivitySupport#isEnabled()
	 */
	public boolean isEnabled(String href) {
		if (!isFilteringEnabled()) {
			return true;
		}
		if (href.startsWith("/")) { //$NON-NLS-1$
			href = href.substring(1);
		}

		return activitySupport.getActivityManager().getIdentifier(href)
				.isEnabled();
	}

	/**
	 * Checks whether topic belongs to a TOC that mathes enabled activity.
	 * Enabled children TOCs are searched if linked by also enabled TOCs.
	 * Additionally topic may match description topic of a root TOC.
	 * 
	 * @return true if topic belongs to an enabled TOC
	 * @param href
	 * @param locale
	 *            locale for which TOCs are checked
	 */
	public boolean isEnabledTopic(String href, String locale) {
		if (href == null) {
			return false;
		}
		if (!isFilteringEnabled()) {
			return true;
		}
		int ix = href.indexOf("?resultof="); //$NON-NLS-1$
		if (ix >= 0) {
			href = href.substring(0, ix);
		}
		// Find out if description topic for enabled top level TOCs matches the
		// topic
		ITocElement[] tocs = HelpPlugin.getTocManager().getTocs(locale);
		for (int t = 0; t < tocs.length; t++) {
			String descriptionHref = tocs[t].getTocTopicHref();
			if (descriptionHref != null
					&& descriptionHref.length() > 0
					&& descriptionHref.equals(href)
					&& HelpBasePlugin.getActivitySupport().isEnabled(
							tocs[t].getHref())) {
				return true;
			}
		}
		// Find out if any contributed toc that is enabled contains the topic
		return isInTocSubtree(href, Arrays.asList(tocs));
	}
	/**
	 * @param href
	 *            href of a topic
	 * @param tocList
	 *            List of ITocElement
	 * @return true if given topic belongs to one of enabled ITocElements or
	 *         their children
	 */
	private boolean isInTocSubtree(String href, List tocList) {
		for (Iterator it = tocList.iterator(); it.hasNext();) {
			ITocElement toc = (ITocElement) it.next();
			if (!HelpBasePlugin.getActivitySupport().isEnabled(toc.getHref())) {
				// TOC is not enabled, check other TOCs
				continue;
			}
			// Check topics in navigation
			if (toc.getOwnedTopic(href) != null) {
				return true;
			}
			// Check extra dir
			if (toc.getOwnedExtraTopic(href) != null) {
				return true;
			}
			// check children TOCs
			if (isInTocSubtree(href, toc.getChildrenTocs())) {
				return true;
			} else {
				// try other TOCs at this level
			}
		}
		return false;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.internal.base.IHelpActivitySupport#enableActivities(java.lang.String)
	 */
	public void enableActivities(String href) {
		if (href.startsWith("/")) { //$NON-NLS-1$
			href = href.substring(1);
		}

		IIdentifier identifier = activitySupport.getActivityManager()
				.getIdentifier(href);
		Set activitityIds = identifier.getActivityIds();
		if (activitityIds.isEmpty()) { // if there are no activities that match
			// this identifier, do nothing.
			return;
		}

		Set enabledIds = new HashSet(activitySupport.getActivityManager()
				.getEnabledActivityIds());
		enabledIds.addAll(activitityIds);
		activitySupport.setEnabledActivityIds(enabledIds);
	}

	/**
	 * @return whether the UI is set up to filter contributions (has defined
	 *         activity categories).
	 */
	private static boolean isWorkbenchFiltering() {
		return !PlatformUI.getWorkbench().getActivitySupport()
				.getActivityManager().getDefinedActivityIds().isEmpty();
	}
}
