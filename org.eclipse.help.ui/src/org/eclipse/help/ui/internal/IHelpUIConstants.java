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
/**
 * Interface for holding Help UI plug-in constants
 */
public interface IHelpUIConstants {
	// Help UI pluging id with a "." for convenience.
	public static final String HELP_UI_PLUGIN_ID = HelpUIPlugin.PLUGIN_ID + "."; //$NON-NLS-1$
	// F1 ids
	public static final String F1_SHELL = HELP_UI_PLUGIN_ID + "f1Shell"; //$NON-NLS-1$
	public static final String PREF_PAGE_BROWSERS = HELP_UI_PLUGIN_ID
			+ "prefPageBrowsers"; //$NON-NLS-1$
	public static final String PREF_PAGE_APPSERVER = HELP_UI_PLUGIN_ID
			+ "prefPageAppServer"; //$NON-NLS-1$
	public static final String PREF_PAGE_CUSTOM_BROWSER_PATH = HELP_UI_PLUGIN_ID
			+ "prefPageCustomBrowserPath"; //$NON-NLS-1$
	public static final String IMAGE_KEY_F1TOPIC = "f1_topic_icon"; //$NON-NLS-1$
	public static final String IMAGE_FILE_F1TOPIC = "topic.gif"; //$NON-NLS-1$
}
