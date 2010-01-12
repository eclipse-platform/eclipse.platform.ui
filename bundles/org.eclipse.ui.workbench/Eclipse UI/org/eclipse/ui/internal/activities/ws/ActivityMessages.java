/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.activities.ws;

import org.eclipse.osgi.util.NLS;


/**
 * The ActivtyMessages are the messages used by the activities
 * support.
 *
 */
public class ActivityMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ui.internal.activities.ws.messages";//$NON-NLS-1$
	
	public static String ActivityEnabler_description;
	public static String ActivityEnabler_activities;
    public static String ActivityEnabler_categories;
	public static String ActivityEnabler_selectAll;
	public static String ActivityEnabler_deselectAll;
    public static String ActivitiesPreferencePage_advancedDialogTitle;
    public static String ActivitiesPreferencePage_advancedButton;
    public static String ActivitiesPreferencePage_lockedMessage;
    public static String ActivitiesPreferencePage_captionMessage;
    public static String ActivitiesPreferencePage_requirements;
	public static String ManagerTask;
	public static String ManagerWindowSubTask;
	public static String ManagerViewsSubTask;
	public static String Perspective_showAll;
	public static String activityPromptButton;
	public static String activityPromptToolTip;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, ActivityMessages.class);
	}
}
