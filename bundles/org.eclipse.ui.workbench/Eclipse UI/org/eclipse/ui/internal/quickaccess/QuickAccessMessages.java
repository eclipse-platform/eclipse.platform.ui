/*******************************************************************************
 * Copyright (c) 2006, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 488926, 459989
 *******************************************************************************/

package org.eclipse.ui.internal.quickaccess;

import org.eclipse.osgi.util.NLS;

/**
 * @since 3.2
 *
 */
public class QuickAccessMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ui.internal.quickaccess.messages"; //$NON-NLS-1$
	public static String QuickAccess_TooltipDescription;
	public static String QuickAccess_TooltipDescription_Empty;
	public static String QuickAccess_Perspectives;
	public static String QuickAccess_Commands;
	public static String QuickAccess_Properties;
	public static String QuickAccess_Editors;
	public static String QuickAccess_Menus;
	public static String QuickAccess_New;
	public static String QuickAccess_Preferences;
	public static String QuickAccess_Previous;
	public static String QuickAccess_Views;
	public static String QuickAccess_PressKeyToShowAllMatches;
	public static String QuickAccess_StartTypingToFindMatches;
	public static String QuickAccess_AvailableCategories;
	public static String QuickAccess_EnterSearch;
	public static String QuickAccess_SelectedString;
	public static String QuickAccess_ViewWithCategory;
	public static String QuickAccessContents_NoMatchingResults;
	public static String QuickAccessContents_PressKeyToLimitResults;
	public static String QuickAccessContents_QuickAccess;
	public static String QuickAccessContents_SearchInHelpLabel;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, QuickAccessMessages.class);
	}

	private QuickAccessMessages() {
	}
}
